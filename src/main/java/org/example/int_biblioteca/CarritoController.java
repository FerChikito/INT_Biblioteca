package org.example.int_biblioteca;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class CarritoController {

    @FXML private TableView<Libro> tablaCarrito;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, Void>   colAccion;
    @FXML private Label etiquetaTotal;

    @FXML
    private void initialize() {
        // Bind de datos a la lista del servicio (observable)
        ObservableList<Libro> items = CarritoService.getItems();
        tablaCarrito.setItems(items);

        colTitulo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitulo()));
        colAutor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAutor()));
        colIsbn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIsbn()));

        colAccion.setCellFactory(quitarFactory());

        // Total siempre actualizado
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
        int n = CarritoService.total();
        if (n == 0) {
            info("Préstamo", "No hay libros en el carrito.");
            return;
        }
        // Folio mock (Fase 2 lo haremos con la BD)
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String rand  = String.format("%04d", new Random().nextInt(10000));
        String folio = "PR-" + fecha + "-" + rand;

        info("Préstamo confirmado",
                "Folio: " + folio + "\n" +
                        "Libros: " + n + "\n\n" +
                        "Presenta este folio en la biblioteca para recoger tu(s) libro(s).");

        // Si quieres vaciar tras confirmar:
        // CarritoService.vaciar();

        // Si abriste el carrito como ventana aparte, puedes cerrarlo aquí:
        // cerrarVentana();
    }

    private void info(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(t);
        a.showAndWait();
    }

    // Útil si el carrito se abre en un Stage modal
    @SuppressWarnings("unused")
    private void cerrarVentana() {
        Stage st = (Stage) tablaCarrito.getScene().getWindow();
        if (st != null) st.close();
    }
}
