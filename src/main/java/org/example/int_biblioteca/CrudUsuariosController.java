package org.example.int_biblioteca;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.example.int_biblioteca.dao.UsuarioDAO;

import java.sql.SQLException;

public class CrudUsuariosController {

    // Tabla y columnas
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> columnaNombre;
    @FXML private TableColumn<Usuario, Rol>    columnaRol;

    // Formulario
    @FXML private TextField     campoNombre;
    @FXML private PasswordField campoContrasenia;
    @FXML private ComboBox<Rol> comboRol;

    @FXML
    private void initialize() {
        // Mapeo columnas -> getters del POJO Usuario
        columnaNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        columnaRol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getRol()));

        // Cargar opciones de rol desde tu enum
        comboRol.setItems(FXCollections.observableArrayList(Rol.values()));

        // Cargar datos iniciales
        recargarTabla();
    }

    private void recargarTabla() {
        try {
            tablaUsuarios.setItems(FXCollections.observableArrayList(UsuarioDAO.listar()));
        } catch (SQLException e) {
            mostrarAlerta("Base de datos", "No se pudo cargar la lista de usuarios:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleInsertarUsuario() {
        String nombre = campoNombre.getText();
        String contrasenia = campoContrasenia.getText();
        Rol rol = comboRol.getValue();

        if (nombre == null || nombre.isBlank() || contrasenia == null || contrasenia.isBlank() || rol == null) {
            mostrarAlerta("Validación", "Nombre, contraseña y rol son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            boolean insertado = UsuarioDAO.insertar(new Usuario(nombre, contrasenia, rol));
            if (insertado) {
                recargarTabla();
                handleLimpiarUsuario();
                mostrarAlerta("Éxito", "Usuario insertado correctamente.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Base de datos", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleActualizarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección", "Selecciona un usuario de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        String nombreOriginal = seleccionado.getNombre();
        String nuevoNombre = campoNombre.getText();
        String nuevaContrasenia = campoContrasenia.getText();
        Rol nuevoRol = comboRol.getValue();

        if (nuevoNombre == null || nuevoNombre.isBlank() || nuevaContrasenia == null || nuevaContrasenia.isBlank() || nuevoRol == null) {
            mostrarAlerta("Validación", "Nombre, contraseña y rol son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            boolean actualizado = UsuarioDAO.actualizar(nombreOriginal, new Usuario(nuevoNombre, nuevaContrasenia, nuevoRol));
            if (actualizado) {
                recargarTabla();
                mostrarAlerta("Éxito", "Usuario actualizado correctamente.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Base de datos", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEliminarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección", "Selecciona un usuario de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el usuario \"" + seleccionado.getNombre() + "\"?");
        confirmacion.setTitle("Confirmar eliminación");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            boolean eliminado = UsuarioDAO.eliminar(seleccionado.getNombre());
            if (eliminado) {
                recargarTabla();
                handleLimpiarUsuario();
                mostrarAlerta("Éxito", "Usuario eliminado.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Base de datos", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarUsuario() {
        campoNombre.clear();
        campoContrasenia.clear();
        comboRol.getSelectionModel().clearSelection();
        tablaUsuarios.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSeleccionarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            campoNombre.setText(seleccionado.getNombre());
            campoContrasenia.setText(seleccionado.getContrasenia());
            comboRol.setValue(seleccionado.getRol());
        }
    }



    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.showAndWait();
    }
}
