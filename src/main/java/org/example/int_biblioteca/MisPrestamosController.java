package org.example.int_biblioteca;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import org.example.int_biblioteca.dao.PrestamoDAO;
import org.example.int_biblioteca.dao.ConfigDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MisPrestamosController {

    // Tabla
    @FXML private TableView<PrestamoListado> tablaPrestamos;
    @FXML private TableColumn<PrestamoListado, Number> colId;
    @FXML private TableColumn<PrestamoListado, String> colTitulo;
    @FXML private TableColumn<PrestamoListado, String> colIsbn;
    @FXML private TableColumn<PrestamoListado, String> colFechaPrestamo;
    @FXML private TableColumn<PrestamoListado, String> colFechaDevol;
    @FXML private TableColumn<PrestamoListado, String> colEstado;
    @FXML private TableColumn<PrestamoListado, String> colFolio;
    @FXML private TableColumn<PrestamoListado, String> colLimite;
    @FXML private TableColumn<PrestamoListado, String> colMulta; // <- calcula con tarifa de BD

    // UI
    @FXML private Label  etiquetaResultados;
    @FXML private Button btnMarcarDevuelto;

    // Buscador
    @FXML private TextField buscarPrestamosField;

    // Estado
    private Usuario usuarioActual;
    private Rol rolActual = Rol.USUARIO;

    public void setUsuarioActual(Usuario u) { this.usuarioActual = u; }
    public void setRolActual(Rol r) { this.rolActual = r; }

    private final DateTimeFormatter FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter FMT_D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObservableList<PrestamoListado> base = FXCollections.observableArrayList();
    private FilteredList<PrestamoListado> filtrado;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getIdPrestamo()));
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitulo()));
        colIsbn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIsbn()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(formatearEstado(c.getValue().getEstado())));
        colFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        colLimite.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getFechaLimite() == null ? "-" : FMT_D.format(c.getValue().getFechaLimite())
                )
        );
        colFechaPrestamo.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getFechaPrestamo() == null ? "" : FMT.format(c.getValue().getFechaPrestamo())
                )
        );
        colFechaDevol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getFechaDevolucion() == null ? "-" : FMT.format(c.getValue().getFechaDevolucion())
                )
        );

        // Multa: tarifa dinámica desde BD (ConfigDAO.getTarifaMulta) * días de atraso
        colMulta.setCellValueFactory(c -> {
            PrestamoListado p = c.getValue();
            String est = p.getEstado();
            LocalDate limite = p.getFechaLimite();
            if (limite == null) return new SimpleStringProperty("-");
            boolean aplica = ("A".equals(est) || "M".equals(est)) && LocalDate.now().isAfter(limite);
            if (!aplica) return new SimpleStringProperty("$0");

            long dias = java.time.temporal.ChronoUnit.DAYS.between(limite, LocalDate.now());
            if (dias < 0) dias = 0;

            double tarifaDia = leerTarifaDesdeBD(); // <- aquí tomamos la tarifa actual
            long total = Math.round(tarifaDia * dias); // entero simple; si quieres decimales, formatea diferente
            return new SimpleStringProperty("$" + total);
        });

        // (Opcional) resaltar filas vencidas
        tablaPrestamos.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(PrestamoListado item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    boolean vencido = item.getFechaLimite() != null
                            && LocalDate.now().isAfter(item.getFechaLimite())
                            && !"D".equals(item.getEstado());
                    setStyle(vencido ? "-fx-background-color: #ffecec;" : "");
                }
            }
        });

        // Botón según permisos (usuario NO ve acciones de gestión)
        boolean puedeDevolver = (rolActual == Rol.BIBLIOTECARIO || rolActual == Rol.ADMIN || rolActual == Rol.SUPER_ADMIN);
        btnMarcarDevuelto.setVisible(puedeDevolver);
        btnMarcarDevuelto.managedProperty().bind(btnMarcarDevuelto.visibleProperty());

        // Filtro
        filtrado = new FilteredList<>(base, p -> true);
        if (buscarPrestamosField != null) {
            buscarPrestamosField.textProperty().addListener((o, a, q) -> {
                String s = (q == null) ? "" : q.trim().toLowerCase();
                filtrado.setPredicate(p ->
                        s.isEmpty()
                                || (p.getTitulo() != null && p.getTitulo().toLowerCase().contains(s))
                                || (p.getIsbn() != null && p.getIsbn().toLowerCase().contains(s))
                                || (p.getFolio() != null && p.getFolio().toLowerCase().contains(s))
                                || (p.getCorreoUsuario() != null && p.getCorreoUsuario().toLowerCase().contains(s))
                                || (formatearEstado(p.getEstado()).toLowerCase().contains(s)));
            });
        }
        SortedList<PrestamoListado> ordenado = new SortedList<>(filtrado);
        ordenado.comparatorProperty().bind(tablaPrestamos.comparatorProperty());
        tablaPrestamos.setItems(ordenado);
    }

    // Lee la tarifa actual desde BD; si falla, usa 15 como respaldo
    private double leerTarifaDesdeBD() {
        try {
            return ConfigDAO.getTarifaMulta();
        } catch (Exception e) {
            return 15.0; // fallback
        }
    }

    private String formatearEstado(String e) {
        if (e == null) return "";
        return switch (e) {
            case "A" -> "Activo";
            case "D" -> "Devuelto";
            case "M" -> "Multado";
            default  -> e;
        };
    }

    // Llamar DESPUÉS de setUsuarioActual/setRolActual
    public void cargarDatos() {
        boolean puedeDevolver = (rolActual == Rol.BIBLIOTECARIO || rolActual == Rol.ADMIN || rolActual == Rol.SUPER_ADMIN);
        btnMarcarDevuelto.setVisible(puedeDevolver);

        try {
            List<PrestamoListado> datos;
            if (rolActual == Rol.USUARIO && usuarioActual != null) {
                datos = PrestamoDAO.listarPorUsuario(usuarioActual.getCorreo());
            } else {
                datos = PrestamoDAO.listarActivos();
            }
            base.setAll(datos);
            etiquetaResultados.setText("Resultados (" + datos.size() + ")");
        } catch (SQLException e) {
            mostrar("Base de datos", "No se pudo cargar la lista:\n" + e.getMessage(), Alert.AlertType.ERROR);
            base.clear();
            etiquetaResultados.setText("Resultados (0)");
        }

        // Forzar refresco de celdas por si cambió la tarifa mientras la vista estaba abierta
        tablaPrestamos.refresh();
    }

    @FXML
    private void handleRefrescar() { cargarDatos(); }

    @FXML
    private void handleMarcarDevuelto() {
        PrestamoListado sel = tablaPrestamos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrar("Selección", "Selecciona un préstamo de la tabla.", Alert.AlertType.WARNING);
            return;
        }
        if (!(rolActual == Rol.BIBLIOTECARIO || rolActual == Rol.ADMIN || rolActual == Rol.SUPER_ADMIN)) {
            mostrar("Permisos", "No tienes permisos para marcar devoluciones.", Alert.AlertType.WARNING);
            return;
        }
        try {
            boolean ok = PrestamoDAO.marcarDevuelto(sel.getIdPrestamo());
            if (ok) {
                mostrar("Éxito", "Préstamo marcado como devuelto.", Alert.AlertType.INFORMATION);
                cargarDatos();
            } else {
                mostrar("Estado", "Ese préstamo no está activo o ya fue devuelto.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrar("Base de datos", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrar(String t, String m, Alert.AlertType tipo) {
        Alert a = new Alert(tipo, m, ButtonType.OK);
        a.setTitle(t);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
