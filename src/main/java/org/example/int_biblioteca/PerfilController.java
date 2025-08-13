package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Node;
import java.util.function.Consumer;

public class PerfilController {

    @FXML private Label etiquetaNombreRol;
    @FXML private TextField campoNombre, campoCorreo, campoDireccion, campoTelefono, campoNacimiento;

    // Cambio de contraseña (mostrar/ocultar)
    @FXML private PasswordField newPasswordField;
    @FXML private TextField     newPasswordVisibleField;
    @FXML private Button        toggleNewPasswordButton;

    private boolean newPassVisible = false;
    private Usuario usuarioActual;
    private Runnable onRegresar;

    public void setUsuarioActual(Usuario u) {
        this.usuarioActual = u;
        if (u != null) {
            if (etiquetaNombreRol != null) etiquetaNombreRol.setText(u.getNombre() + " (" + u.getRol() + ")");
            if (campoNombre != null)    campoNombre.setText(u.getNombre());
            if (campoCorreo != null)    campoCorreo.setText(u.getCorreo()); // CORREGIDO
            if (campoDireccion != null) campoDireccion.setText(u.getDireccion() == null ? "" : u.getDireccion());
            if (campoTelefono != null)  campoTelefono.setText(u.getNumeroTelefonico() == null ? "" : u.getNumeroTelefonico());
            // campoNacimiento: placeholder (Usuario no tiene fechaNacimiento en el modelo aún)
        }
    }

    public void setOnRegresar(Runnable r) { this.onRegresar = r; }

    @FXML
    private void initialize() {
        if (newPasswordVisibleField != null)
            newPasswordVisibleField.managedProperty().bind(newPasswordVisibleField.visibleProperty());
        if (newPasswordField != null) newPasswordField.setVisible(true);
        if (newPasswordVisibleField != null) newPasswordVisibleField.setVisible(false);
        if (toggleNewPasswordButton != null) toggleNewPasswordButton.setText("\uD83D\uDC40");
    }

    @FXML
    private void toggleNewPassword() {
        newPassVisible = !newPassVisible;
        if (newPassVisible) {
            newPasswordVisibleField.setText(newPasswordField.getText());
            newPasswordVisibleField.setVisible(true);
            newPasswordField.setVisible(false);
            toggleNewPasswordButton.setText("🙈");
        } else {
            newPasswordField.setText(newPasswordVisibleField.getText());
            newPasswordField.setVisible(true);
            newPasswordVisibleField.setVisible(false);
            toggleNewPasswordButton.setText("\uD83D\uDC40");
        }
    }

    private Consumer<Parent> onMostrarVista;  // lo setea MenuPrincipal

    public void setOnMostrarVista(Consumer<Parent> c) { this.onMostrarVista = c; }

    @FXML
    private void abrirCambioContrasenia(javafx.event.ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("contrasenia.fxml"));
            Parent vista = loader.load();

            // Si tu controlador se llama ContraseniaController y expone setUsuarioActual, se lo pasamos:
            try {
                Object ctrl = loader.getController();
                // Evita ClassNotFound si el nombre/clase cambia:
                ctrl.getClass().getMethod("setUsuarioActual", Usuario.class).invoke(ctrl, usuarioActual);
            } catch (Throwable ignore) {}

            if (onMostrarVista != null) {
                onMostrarVista.accept(vista);  // mostrar dentro del centro del menú
            } else {
                // Fallback: abrir modal
                Stage stage = new Stage();
                stage.setTitle("Cambio de contraseña");
                stage.setScene(new Scene(vista));
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(((Node) e.getSource()).getScene().getWindow());
                stage.show();
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo abrir la vista de cambio de contraseña:\n" + ex.getMessage()).showAndWait();
            ex.printStackTrace();
        }
    }
}
