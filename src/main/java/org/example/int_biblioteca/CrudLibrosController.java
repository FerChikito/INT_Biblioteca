package org.example.int_biblioteca;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import org.example.int_biblioteca.dao.LibroDAO;


public class CrudLibrosController {

    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, String> colISBN;

    @FXML private TextField tituloField;
    @FXML private TextField autorField;
    @FXML private TextField isbnField;

    private final ObservableList<Libro> listaLibros = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Asocia columnas con las propiedades del objeto Libro (usando sus getters)
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colISBN.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        recargarTabla();
    }

    private void recargarTabla() {
        try {
            tablaLibros.setItems(FXCollections.observableArrayList(LibroDAO.listar()));
        } catch (Exception e) {
            mostrarAlerta("BD", "No se pudo cargar la lista: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleAgregarLibro() {
        String titulo = tituloField.getText();
        String autor = autorField.getText();
        String isbn = isbnField.getText();

        if (titulo.isBlank() || autor.isBlank() || isbn.isBlank()) {
            mostrarAlerta("Validación", "Título, Autor e ISBN son obligatorios", Alert.AlertType.WARNING);
            return;
        }
        try {
            boolean ok = LibroDAO.insertar(new Libro(titulo, autor, isbn));
            if (ok) { recargarTabla(); limpiarCampos(); }
        } catch (Exception e) {
            mostrarAlerta("BD", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditarLibro() {
        Libro seleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección", "Selecciona un libro de la tabla", Alert.AlertType.WARNING);
            return;
        }

        String isbnOriginal = seleccionado.getIsbn();
        String t = tituloField.getText();
        String a = autorField.getText();
        String i = isbnField.getText();
        if (t.isBlank() || a.isBlank() || i.isBlank()) {
            mostrarAlerta("Validación", "Título, Autor e ISBN son obligatorios", Alert.AlertType.WARNING);
            return;
        }
        try {
            boolean ok = LibroDAO.actualizarPorIsbn(isbnOriginal, new Libro(t, a, i));
            if (ok) { recargarTabla(); }
        } catch (Exception e) {
            mostrarAlerta("BD", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEliminarLibro() {
        Libro seleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección", "Selecciona un libro de la tabla", Alert.AlertType.WARNING);
            return;
        }
        if (new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el libro seleccionado?")
                .showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            boolean ok = LibroDAO.eliminarPorIsbn(seleccionado.getIsbn());
            if (ok) { recargarTabla(); limpiarCampos(); }
        } catch (Exception e) {
            mostrarAlerta("BD", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSubirLibro() {
        int index = tablaLibros.getSelectionModel().getSelectedIndex();
        if (index > 0) return;
        ObservableList<Libro> items = tablaLibros.getItems();
        Libro a = items.get(index - 1), b = items.get(index);
        items.set(index - 1, b);
        items.set(index, a);
        tablaLibros.getSelectionModel().select(index - 1);
    }

    @FXML
    private void handleBajarLibro() {
        int index = tablaLibros.getSelectionModel().getSelectedIndex();
        ObservableList<Libro> items = tablaLibros.getItems();
        if (index < 0|| index >= items.size()) return;
        Libro a = items.get(index), b = items.get(index + 1);
        items.set(index, b);
        items.set(index + 1, a);
        tablaLibros.getSelectionModel().select(index + 1);
    }

    private void limpiarCampos() {
        tituloField.clear();
        autorField.clear();
        isbnField.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
