package org.example.int_biblioteca;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.example.int_biblioteca.dao.UsuarioDAO;

import java.sql.SQLException;
import java.util.List;

public class CrudUsuariosController {

    // Tabla y columnas
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> columnaNombre;
    @FXML private TableColumn<Usuario, Rol>    columnaRol;

    // Formulario
    @FXML private TextField     campoNombre;
    @FXML private PasswordField campoContrasenia;
    @FXML private ComboBox<Rol> comboRol;

    // *** NUEVO: rol de quien está gestionando esta pantalla (lo setea el menú)
    private Rol rolActual = Rol.USUARIO;

    /** Lo llama MenuPrincipal al abrir esta vista. */
    public void setRolActual(Rol rol) {
        this.rolActual = (rol != null) ? rol : Rol.USUARIO;
        configurarRolesPorPermisos();   // llena combo con roles asignables
        recargarTabla();                // filtra la tabla según permisos
    }

    @FXML
    private void initialize() {
        // Mapeo columnas -> getters del POJO Usuario
        columnaNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        columnaRol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getRol()));

        // Por defecto (antes de setRolActual) no ponemos todos los roles para evitar dar permisos por error
        comboRol.setItems(FXCollections.observableArrayList()); // se llena en configurarRolesPorPermisos()

    }

    // ===================== Permisos por rol (ver/asignar/gestionar) =====================

    /** Qué roles puede ver en la tabla el rolActual. */
    private List<Rol> rolesQuePuedeVer() {
        return switch (rolActual) {
            case SUPER_ADMIN   -> List.of(Rol.SUPER_ADMIN, Rol.ADMIN, Rol.BIBLIOTECARIO, Rol.USUARIO);
            case ADMIN         -> List.of(Rol.ADMIN, Rol.BIBLIOTECARIO, Rol.USUARIO);
            case BIBLIOTECARIO -> List.of(Rol.USUARIO);
            default            -> List.of(); // USUARIO no debería abrir esta vista
        };
    }

    /** Qué roles puede asignar (crear/editar) el rolActual. */
    private List<Rol> rolesQuePuedeAsignar() {
        return switch (rolActual) {
            case SUPER_ADMIN   -> List.of(Rol.SUPER_ADMIN, Rol.ADMIN, Rol.BIBLIOTECARIO, Rol.USUARIO);
            case ADMIN         -> List.of(Rol.ADMIN, Rol.BIBLIOTECARIO); // NO puede asignar SUPER_ADMIN ni USUARIO? (tu regla decía que admin puede agregar admins y bibliotecarios; usuarios los puede crear el bibliotecario)
            case BIBLIOTECARIO -> List.of(Rol.USUARIO);
            default            -> List.of();
        };
    }

    /** ¿Puedo gestionar (editar/eliminar) un usuario con este rol objetivo? */
    private boolean puedeGestionarRol(Rol objetivo) {
        return rolesQuePuedeVer().contains(objetivo) && (
                rolesQuePuedeAsignar().contains(objetivo) || rolActual == Rol.SUPER_ADMIN
        );
    }

    /** ¿Puedo asignar este rol en crear/editar? (estricto) */
    private boolean validarAsignacion(Rol rolSeleccionado) {
        if (!rolesQuePuedeAsignar().contains(rolSeleccionado)) {
            mostrarAlerta("Permisos", "No puedes asignar el rol: " + rolSeleccionado, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    /** Llena el combo con roles que sí puede asignar el rolActual. */
    private void configurarRolesPorPermisos() {
        var asignables = rolesQuePuedeAsignar();
        comboRol.setItems(FXCollections.observableArrayList(asignables));
        if (!asignables.isEmpty()) comboRol.getSelectionModel().selectFirst();
    }

    // ================================== Datos ==================================

    private void recargarTabla() {
        try {
            var todos = UsuarioDAO.listar();
            var visibles = rolesQuePuedeVer();
            var filtrados = todos.stream()
                    .filter(u -> visibles.contains(u.getRol()))
                    .toList();
            tablaUsuarios.setItems(FXCollections.observableArrayList(filtrados));
        } catch (SQLException e) {
            mostrarAlerta("Base de datos", "No se pudo cargar la lista de usuarios:\n" + e.getMessage(), Alert.AlertType.ERROR);
            if (tablaUsuarios != null) tablaUsuarios.getItems().clear();
        } catch (IllegalStateException e) {
            // Por ejemplo, si Database no tiene config cargada
            mostrarAlerta("Base de datos", e.getMessage(), Alert.AlertType.ERROR);
            if (tablaUsuarios != null) tablaUsuarios.getItems().clear();
        }
    }

    // =============================== Handlers CRUD ===============================

    @FXML
    private void handleInsertarUsuario() {
        String nombre = campoNombre.getText();
        String contrasenia = campoContrasenia.getText();
        Rol rol = comboRol.getValue();

        if (nombre == null || nombre.isBlank() || contrasenia == null || contrasenia.isBlank() || rol == null) {
            mostrarAlerta("Validación", "Nombre, contraseña y rol son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        // *** NUEVO: permisos de asignación
        if (!validarAsignacion(rol)) return;

        try {
            boolean insertado = UsuarioDAO.insertar(new Usuario(nombre, contrasenia, rol));
            if (insertado) {
                recargarTabla();
                handleLimpiarUsuario();
                mostrarAlerta("Éxito", "Usuario insertado correctamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Información", "No se insertó el usuario (sin cambios).", Alert.AlertType.INFORMATION);
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

        // *** NUEVO: no permitir gestionar roles que no te tocan
        if (!puedeGestionarRol(seleccionado.getRol())) {
            mostrarAlerta("Permisos", "No puedes editar un usuario con rol: " + seleccionado.getRol(), Alert.AlertType.WARNING);
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

        // *** NUEVO: no permitir promocionar a un rol que no puedes asignar
        if (!validarAsignacion(nuevoRol)) return;

        try {
            boolean actualizado = UsuarioDAO.actualizar(nombreOriginal, new Usuario(nuevoNombre, nuevaContrasenia, nuevoRol));
            if (actualizado) {
                recargarTabla();
                mostrarAlerta("Éxito", "Usuario actualizado correctamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Información", "No hubo cambios para actualizar.", Alert.AlertType.INFORMATION);
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

        // *** NUEVO: permisos al eliminar
        if (!puedeGestionarRol(seleccionado.getRol())) {
            mostrarAlerta("Permisos", "No puedes eliminar un usuario con rol: " + seleccionado.getRol(), Alert.AlertType.WARNING);
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
            } else {
                mostrarAlerta("Información", "No se eliminó (usuario inexistente).", Alert.AlertType.INFORMATION);
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
        // *** opcional: resetear a primer rol asignable:
        configurarRolesPorPermisos();
    }

    @FXML
    private void handleSeleccionarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        // *** NUEVO: si no puedes gestionarlo, no lo cargues al formulario
        if (!puedeGestionarRol(seleccionado.getRol())) {
            mostrarAlerta("Permisos", "No puedes gestionar usuarios con rol: " + seleccionado.getRol(), Alert.AlertType.WARNING);
            tablaUsuarios.getSelectionModel().clearSelection();
            return;
        }

        campoNombre.setText(seleccionado.getNombre());
        campoContrasenia.setText(seleccionado.getContrasenia());
        // Solo permite elegir roles asignables; si el rol del seleccionado no es asignable por ti,
        // mostramos el combo con el primer asignable para evitar “promociones” indebidas.
        if (rolesQuePuedeAsignar().contains(seleccionado.getRol())) {
            comboRol.setValue(seleccionado.getRol());
        } else if (!comboRol.getItems().isEmpty()) {
            comboRol.getSelectionModel().selectFirst();
        }
    }

    // =============================== Utilidad ===============================

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
