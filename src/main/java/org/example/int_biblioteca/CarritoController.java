package org.example.int_biblioteca;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.int_biblioteca.dao.PrestamoDAO;

public class CarritoController {

    @FXML private TableView<Libro> tablaCarrito;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, Void>   colAccion;
    @FXML private Label etiquetaTotal;

    // <<< NUEVO: el usuario logueado >>>
    private Usuario usuarioActual;
    public void setUsuarioActual(Usuario u) { this.usuarioActual = u; }

    @FXML
    private void initialize() {
        ObservableList<Libro> items = CarritoService.getItems();
        tablaCarrito.setItems(items);

        colTitulo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitulo()));
        colAutor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAutor()));
        colIsbn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIsbn()));

        colAccion.setCellFactory(quitarFactory());

        etiquetaTotal.textProperty().bind(Bindings.convert(CarritoService.totalProperty()));
    }

    private Callback<TableColumn<Libro, Void>, TableCell<Libro, Void>> quitarFactory() {
        return col -> new TableCell<>() {
            private final Button btn = new Button("X");
            {
                btn.setOnAction(e -> {
                    Libro l = getTableView().getItems().get(getIndex());
                    CarritoService.eliminarPorIsbn(l.getIsbn());
                });
                btn.setStyle("-fx-background-color: #B00020; -fx-text-fill: white;");
                btn.setMaxWidth(Double.MAX_VALUE);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };
    }

    @FXML
    private void handleVaciar() {
        if (CarritoService.total() == 0) {
            info("Carrito", "El carrito ya está vacío.");
            return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Vaciar el carrito?");
        conf.setHeaderText(null);
        if (conf.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            CarritoService.vaciar();
        }
    }

    @FXML
    private void handleConfirmar() {
        if (usuarioActual == null) {
            alerta("Sesión", "Debes iniciar sesión para confirmar el préstamo.", Alert.AlertType.WARNING);
            return;
        }
        var isbns = CarritoService.getIsbns();
        if (isbns.isEmpty()) {
            alerta("Carrito", "Tu carrito está vacío.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            // Pre-chequeos
            if (PrestamoDAO.tieneVencidos(usuarioActual.getCorreo())) {
                alerta("Préstamos", "Tienes préstamos vencidos. Devuélvelos para poder solicitar más.", Alert.AlertType.WARNING);
                return;
            }
            int activos = PrestamoDAO.contarActivos(usuarioActual.getCorreo());
            if (activos >= PrestamoDAO.MAX_ACTIVOS) {
                alerta("Préstamos", "Límite de " + PrestamoDAO.MAX_ACTIVOS + " préstamos activos alcanzado.", Alert.AlertType.WARNING);
                return;
            }

            int creados = PrestamoDAO.crearPrestamos(usuarioActual.getCorreo(), isbns);
            if (creados > 0) {
                CarritoService.vaciar();
                alerta("Préstamos",
                        "Se generaron " + creados + " préstamos.\n" +
                                "Revisa Menú → Mis Préstamos para ver estado y fechas.",
                        Alert.AlertType.INFORMATION);
            } else {
                alerta("Préstamos", "Ningún préstamo creado (libros ocupados o límite alcanzado).", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            alerta("Base de datos", "No se pudo crear el préstamo:\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void info(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(t);
        a.showAndWait();
    }

    // <<< NUEVO: helper de alerta >>>
    private void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert a = new Alert(tipo, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(titulo);
        a.showAndWait();
    }

    @SuppressWarnings("unused")
    private void cerrarVentana() {
        Stage st = (Stage) tablaCarrito.getScene().getWindow();
        if (st != null) st.close();
    }
}
