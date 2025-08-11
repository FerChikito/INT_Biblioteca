package org.example.int_biblioteca;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.example.int_biblioteca.dao.LibroDAO;

import java.sql.SQLException;
import java.util.List;

public class CatalogoLibrosController {

    @FXML private TextField  campoBusqueda;
    @FXML private TableView<Libro> tablaCatalogo;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, Void>   colAcciones;
    @FXML private Label etiquetaResultados;

    private final ObservableList<Libro> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colTitulo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitulo()));
        colAutor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAutor()));
        colIsbn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIsbn()));
        colAcciones.setCellFactory(botonAgregarFactory());

        tablaCatalogo.setItems(datos);

        // Doble click: agregar al carrito
        tablaCatalogo.setRowFactory(tv -> {
            TableRow<Libro> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    agregarAlCarrito(row.getItem());
                }
            });
            return row;
        });

        recargarListaCompleta();
    }

    private void recargarListaCompleta() {
        try {
            List<Libro> lista = LibroDAO.listar();
            datos.setAll(lista);
            actualizarEtiquetaResultados();
        } catch (SQLException e) {
            error("Catálogo", "No se pudo cargar el catálogo:\n" + e.getMessage());
            datos.clear();
            actualizarEtiquetaResultados();
        }
    }

    private void actualizarEtiquetaResultados() {
        etiquetaResultados.setText(datos.size() + " resultados");
    }

    @FXML
    private void handleBuscar() {
        String q = (campoBusqueda.getText() == null) ? "" : campoBusqueda.getText().trim();
        if (q.isEmpty()) {
            recargarListaCompleta();
            return;
        }
        try {
            List<Libro> encontrados = LibroDAO.buscar(q);
            datos.setAll(encontrados);
            actualizarEtiquetaResultados();
        } catch (SQLException e) {
            error("Búsqueda", "No se pudo buscar:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleVerCarrito() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("carrito.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage dlg = new javafx.stage.Stage();
            dlg.setTitle("Carrito");
            dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dlg.setScene(new javafx.scene.Scene(root));
            dlg.showAndWait();
        } catch (Exception e) {
            error("Carrito", "No se pudo abrir el carrito:\n" + e.getMessage());
        }
    }

    private Callback<TableColumn<Libro, Void>, TableCell<Libro, Void>> botonAgregarFactory() {
        return col -> new TableCell<>() {
            private final Button btn = new Button("Agregar");
            {
                btn.setOnAction(evt -> {
                    Libro libro = getTableView().getItems().get(getIndex());
                    agregarAlCarrito(libro);
                });
                btn.setMaxWidth(Double.MAX_VALUE);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };
    }

    private void agregarAlCarrito(Libro libro) {
        boolean ok = CarritoService.agregar(libro);
        Alert.AlertType tipo = ok ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING;
        String msg = ok ? "Se agregó al carrito:\n" : "Ya estaba en el carrito:\n";
        Alert a = new Alert(tipo, msg + formatear(libro));
        a.setHeaderText(null);
        a.setTitle("Carrito");
        a.showAndWait();
    }

    private String formatear(Libro l) {
        return l.getTitulo() + " | " + l.getAutor() + " | ISBN: " + l.getIsbn();
    }

    private void error(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(titulo);
        a.showAndWait();
    }

    public void buscarDesdeExterno(String q) {
        if (q == null) q = "";
        campoBusqueda.setText(q);
        handleBuscar();              // reutiliza tu método de buscar
        campoBusqueda.requestFocus();
        campoBusqueda.selectAll();
    }
}
