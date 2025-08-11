package org.example.int_biblioteca;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.example.int_biblioteca.dao.UsuarioDAO;

public class CrudUsuariosController {

    // Tabla
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> columnaNombre;
    @FXML private TableColumn<Usuario, String> columnaCorreo;
    @FXML private TableColumn<Usuario, Rol>    columnaRol;

    // Formulario
    @FXML private TextField     campoNombre;
    @FXML private TextField     campoCorreo;
    @FXML private TextField     campoTelefono;
    @FXML private TextField     campoDireccion;
    @FXML private PasswordField campoContrasenia;
    @FXML private ComboBox<Rol> comboRol;

    // Estado
    private Rol rolActual = Rol.USUARIO;
    private String correoOriginalSeleccionado;

    @FXML
    private void initialize() {
        columnaNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        columnaCorreo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorreo()));
        columnaRol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getRol()));
        comboRol.setItems(FXCollections.observableArrayList()); // se llena en setRolActual
    }

    // Lo llama el menú al abrir esta vista
    public void setRolActual(Rol r) {
        this.rolActual = r;
        comboRol.setItems(FXCollections.observableArrayList(rolesQuePuedeCrear(r)));
        recargarTabla();
    }

    private static List<Rol> rolesQuePuedeCrear(Rol r) {
        List<Rol> roles = new ArrayList<>();
        switch (r) {
            case SUPER_ADMIN -> { roles.add(Rol.SUPER_ADMIN); roles.add(Rol.ADMIN); roles.add(Rol.BIBLIOTECARIO); roles.add(Rol.USUARIO); }
            case ADMIN       -> { roles.add(Rol.ADMIN); roles.add(Rol.BIBLIOTECARIO); roles.add(Rol.USUARIO); }
            case BIBLIOTECARIO -> roles.add(Rol.USUARIO);
            default -> {}
        }
        return roles;
    }

    private static boolean puedeVer(Rol actual, Rol delListado) {
        return switch (actual) {
            case SUPER_ADMIN -> true;
            case ADMIN -> delListado != Rol.SUPER_ADMIN;
            case BIBLIOTECARIO -> delListado == Rol.USUARIO;
            default -> false;
        };
    }

    private void recargarTabla() {
        try {
            List<Usuario> todos = UsuarioDAO.listar();
            List<Usuario> filtrados = new ArrayList<>();
            for (Usuario u : todos) if (puedeVer(rolActual, u.getRol())) filtrados.add(u);
            tablaUsuarios.setItems(FXCollections.observableArrayList(filtrados));
        } catch (SQLException e) {
            alerta("Base de datos", "No se pudo cargar la lista de usuarios:\n" + e.getMessage(), Alert.AlertType.ERROR);
            tablaUsuarios.getItems().clear();
        }
    }

    @FXML
    private void handleInsertarUsuario() {
        if (comboRol.getValue() == null) {
            alerta("Validación", "Selecciona un rol.", Alert.AlertType.WARNING);
            return;
        }
        if (!rolesQuePuedeCrear(rolActual).contains(comboRol.getValue())) {
            alerta("Permisos", "No puedes crear usuarios con el rol " + comboRol.getValue(), Alert.AlertType.WARNING);
            return;
        }
        Usuario u = leerFormulario();
        if (u == null) return;

        try {
            if (UsuarioDAO.insertar(u)) {
                recargarTabla();
                handleLimpiarUsuario();
                alerta("Éxito", "Usuario insertado correctamente.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            alerta("Base de datos", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleActualizarUsuario() {
        if (tablaUsuarios.getSelectionModel().getSelectedItem() == null || correoOriginalSeleccionado == null) {
            alerta("Selección", "Selecciona un usuario de la tabla.", Alert.AlertType.WARNING);
            return;
        }
        if (!rolesQuePuedeCrear(rolActual).contains(comboRol.getValue())) {
            alerta("Permisos", "No puedes asignar el rol " + comboRol.getValue(), Alert.AlertType.WARNING);
            return;
        }
        Usuario u = leerFormulario();
        if (u == null) return;

        try {
            if (UsuarioDAO.actualizarPorCorreo(correoOriginalSeleccionado, u)) {
                recargarTabla();
                alerta("Éxito", "Usuario actualizado correctamente.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            alerta("Base de datos", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEliminarUsuario() {
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerta("Selección", "Selecciona un usuario de la tabla.", Alert.AlertType.WARNING);
            return;
        }
        if (!puedeVer(rolActual, sel.getRol())) {
            alerta("Permisos", "No puedes eliminar usuarios de rol " + sel.getRol(), Alert.AlertType.WARNING);
            return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el usuario \"" + sel.getCorreo() + "\"?");
        if (conf.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            if (UsuarioDAO.eliminarPorCorreo(sel.getCorreo())) {
                recargarTabla();
                handleLimpiarUsuario();
                alerta("Éxito", "Usuario eliminado.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            alerta("Base de datos", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSeleccionarUsuario() {
        Usuario u = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (u != null) {
            correoOriginalSeleccionado = u.getCorreo();
            campoNombre.setText(u.getNombre());
            campoCorreo.setText(u.getCorreo());
            campoTelefono.setText(u.getNumeroTelefonico());
            campoDireccion.setText(u.getDireccion());
            campoContrasenia.setText(u.getContrasenia());
            comboRol.setValue(u.getRol());
        }
    }

    @FXML
    private void handleLimpiarUsuario() {
        correoOriginalSeleccionado = null;
        campoNombre.clear();
        campoCorreo.clear();
        campoTelefono.clear();
        campoDireccion.clear();
        campoContrasenia.clear();
        comboRol.getSelectionModel().clearSelection();
        tablaUsuarios.getSelectionModel().clearSelection();
    }

    private Usuario leerFormulario() {
        String nombre = safe(campoNombre.getText());
        String correo = safe(campoCorreo.getText());
        String tel    = safe(campoTelefono.getText());
        String dir    = safe(campoDireccion.getText());
        String pass   = campoContrasenia.getText() == null ? "" : campoContrasenia.getText();
        Rol rol       = comboRol.getValue();

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || rol == null) {
            alerta("Validación", "Nombre, correo, contraseña y rol son obligatorios.", Alert.AlertType.WARNING);
            return null;
        }
        return new Usuario(nombre, correo, tel, dir, pass, rol);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert a = new Alert(tipo, mensaje, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
