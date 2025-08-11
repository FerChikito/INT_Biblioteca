package org.example.int_biblioteca;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import org.example.int_biblioteca.dao.PrestamoDAO;

import java.sql.SQLException;
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

    // UI
    @FXML private Label  etiquetaResultados;
    @FXML private Button btnMarcarDevuelto;

    // Estado
    private Usuario usuarioActual;
    private Rol rolActual = Rol.USUARIO;

    public void setUsuarioActual(Usuario u) { this.usuarioActual = u; }
    public void setRolActual(Rol r) { this.rolActual = r; }

    private final DateTimeFormatter FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter FMT_D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getIdPrestamo()));
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitulo()));
        colIsbn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIsbn()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(
                "D".equals(c.getValue().getEstado()) ? "Devuelto" :
                        esVencido(c.getValue()) ? "Vencido" : "Activo"
        ));
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

        // (Opcional) marcar filas vencidas en un rosita suave
        tablaPrestamos.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(PrestamoListado item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (esVencido(item) && !"D".equals(item.getEstado())) {
                    setStyle("-fx-background-color: #ffecec;");
                } else {
                    setStyle("");
                }
            }
        });

        btnMarcarDevuelto.setVisible(false);
        btnMarcarDevuelto.setManaged(false);
    }

    private boolean esVencido(PrestamoListado p) {
        if (p == null || p.getFechaPrestamo() == null) return false;
        if (!"A".equals(p.getEstado())) return false;
        return p.getFechaPrestamo().plusDays(PrestamoDAO.DIAS_LIMITE)
                .isBefore(java.time.LocalDateTime.now());
    }


    private String formatearEstado(String e) {
        if (e == null) return "";
        return switch (e) {
            case "A" -> "Activo";
            case "D" -> "Devuelto";
            default  -> e;
        };
    }

    // Llamar DESPUÉS de setUsuarioActual/setRolActual
    public void cargarDatos() {
        boolean puedeDevolver = (rolActual == Rol.BIBLIOTECARIO || rolActual == Rol.ADMIN || rolActual == Rol.SUPER_ADMIN);
        btnMarcarDevuelto.setVisible(puedeDevolver);
        btnMarcarDevuelto.setManaged(puedeDevolver);

        try {
            List<PrestamoListado> datos;
            if (rolActual == Rol.USUARIO && usuarioActual != null) {
                datos = PrestamoDAO.listarPorUsuario(usuarioActual.getCorreo());
            } else {
                // Gestión para bibliotecario/admin/super: ver activos
                datos = PrestamoDAO.listarActivos();
            }
            tablaPrestamos.setItems(FXCollections.observableArrayList(datos));
            etiquetaResultados.setText("Resultados (" + datos.size() + ")");
        } catch (SQLException e) {
            mostrar("Base de datos", "No se pudo cargar la lista:\n" + e.getMessage(), Alert.AlertType.ERROR);
            tablaPrestamos.getItems().clear();
            etiquetaResultados.setText("Resultados (0)");
        }
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
