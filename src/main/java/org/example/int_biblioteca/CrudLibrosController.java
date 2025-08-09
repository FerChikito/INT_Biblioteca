package org.example.int_biblioteca;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;


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

        // Carga datos iniciales
        listaLibros.addAll(
                new Libro("1984", "George Orwell", "978-0-452-28423-4"),
                new Libro("El principito", "Antoine de Saint-Exupéry", "978-84-206-5352-9")
        );

        tablaLibros.setItems(listaLibros);
    }


    @FXML
    private void handleAgregarLibro() {
        String titulo = tituloField.getText().trim();
        String autor = autorField.getText().trim();
        String isbn = isbnField.getText().trim();

        if (titulo.isEmpty() || autor.isEmpty() || isbn.isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.", Alert.AlertType.ERROR);
            return;
        }

        listaLibros.add(new Libro(titulo, autor, isbn));
        limpiarCampos();
    }

    @FXML
    private void handleEditarLibro() {
        Libro seleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Advertencia", "Selecciona un libro para editar.", Alert.AlertType.WARNING);
            return;
        }

        seleccionado.setTitulo(tituloField.getText().trim());
        seleccionado.setAutor(autorField.getText().trim());
        seleccionado.setIsbn(isbnField.getText().trim());
        tablaLibros.refresh();
        limpiarCampos();
    }

    @FXML
    private void handleEliminarLibro() {
        Libro seleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            listaLibros.remove(seleccionado);
        } else {
            mostrarAlerta("Advertencia", "Selecciona un libro para eliminar.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleSubirLibro() {
        int index = tablaLibros.getSelectionModel().getSelectedIndex();
        if (index > 0) {
            Libro libro = listaLibros.remove(index);
            listaLibros.add(index - 1, libro);
            tablaLibros.getSelectionModel().select(index - 1);
        }
    }

    @FXML
    private void handleBajarLibro() {
        int index = tablaLibros.getSelectionModel().getSelectedIndex();
        if (index < listaLibros.size() - 1) {
            Libro libro = listaLibros.remove(index);
            listaLibros.add(index + 1, libro);
            tablaLibros.getSelectionModel().select(index + 1);
        }
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
