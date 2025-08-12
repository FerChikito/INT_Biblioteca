package org.example.int_biblioteca;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.int_biblioteca.dao.UsuarioDAO;
import javafx.scene.Scene;

import java.io.IOException;

public class RegistroController {

    @FXML private TextField nombreField;
    @FXML private DatePicker fechaNacimientoPicker;
    @FXML private TextField correoField;
    @FXML private TextField confirmarCorreoField;

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    // Campos de texto “visibles” para mostrar la contraseña
    @FXML private TextField verPass;
    @FXML private TextField verPassConfi;

    @FXML private CheckBox terminosCheck;
    @FXML private Button registrarBtn;
    @FXML private Button ojito;
    @FXML private Button ojitoconfi;

    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    @FXML
    private void initialize() {
        // Sincroniza el contenido: lo que escribas en uno, se refleja en el otro
        verPass.textProperty().bindBidirectional(passwordField.textProperty());
        verPassConfi.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        // Asegura estado inicial: mostrar PasswordField y ocultar TextField
        setPasswordVisible(false);
        setConfirmPasswordVisible(false);

        // (Opcional) tooltips
        if (ojito != null) ojito.setTooltip(new Tooltip("Mostrar/Ocultar contraseña"));
        if (ojitoconfi != null) ojitoconfi.setTooltip(new Tooltip("Mostrar/Ocultar confirmación"));
    }

    // ---- Toggle #1: contraseña principal
    @FXML
    public void giño(ActionEvent event) {
        setPasswordVisible(!passwordVisible);
    }

    private void setPasswordVisible(boolean visible) {
        passwordVisible = visible;
        // cuando visible = true -> mostrar verPass (TextField), ocultar passwordField (PasswordField)
        if (visible) {
            verPass.setVisible(true);
            verPass.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            if (ojito != null) ojito.setText("👀");
        } else {
            verPass.setVisible(false);
            verPass.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            if (ojito != null) ojito.setText("👀");
        }
    }

    // ---- Toggle #2: confirmación de contraseña
    @FXML
    public void verPassConfi(ActionEvent event) {
        setConfirmPasswordVisible(!confirmPasswordVisible);
    }

    private void setConfirmPasswordVisible(boolean visible) {
        confirmPasswordVisible = visible;
        if (visible) {
            verPassConfi.setVisible(true);
            verPassConfi.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            if (ojitoconfi != null) ojitoconfi.setText("👀");
        } else {
            verPassConfi.setVisible(false);
            verPassConfi.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            if (ojitoconfi != null) ojitoconfi.setText("👀");
        }
    }

    @FXML
    protected void handleRegisterFromForm(ActionEvent event) {
        String nombre  = safe(nombreField.getText());
        String correo  = safe(correoField.getText());
        String correo2 = safe(confirmarCorreoField.getText());
        String pass    = passwordField.getText() == null ? "" : passwordField.getText();
        String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Nombre, correo y contraseña son obligatorios.");
            return;
        }
        if (!correo.equalsIgnoreCase(correo2)) {
            showAlert(Alert.AlertType.WARNING, "Validación", "El correo y su confirmación no coinciden.");
            return;
        }
        if (!pass.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Las contraseñas no coinciden.");
            return;
        }
        if (terminosCheck != null && !terminosCheck.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Términos", "Debes aceptar los términos y condiciones.");
            return;
        }

        try {
            boolean ok = UsuarioDAO.insertar(new Usuario(nombre, correo, pass, Rol.USUARIO));
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Registro", "Usuario registrado con éxito.");
                cambiarEscena("hello-view.fxml", event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Registro", "No se pudo registrar (¿correo ya existe?).");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Registro", "Error al registrar:\n" + e.getMessage());
        }
    }

    private void cambiarEscena(String fxml, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            if (loader.getLocation() == null) throw new IOException("No se encontró el archivo FXML: " + fxml);
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la pantalla: " + fxml);
        }
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void volverLogin(ActionEvent event) {
        // Si ya escribió algo, confirmamos para no perderlo sin querer
        boolean hayDatos =
                !safe(nombreField.getText()).isEmpty() ||
                        !safe(correoField.getText()).isEmpty() ||
                        !safe(confirmarCorreoField.getText()).isEmpty() ||
                        !safe(passwordField.getText()).isEmpty() ||
                        !safe(confirmPasswordField.getText()).isEmpty();

        if (hayDatos) {
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                    "Tienes campos con información. ¿Seguro que quieres volver al inicio de sesión?",
                    ButtonType.YES, ButtonType.NO);
            conf.setHeaderText(null);
            conf.setTitle("Confirmar");
            if (conf.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }
        }

        cambiarEscena("hello-view.fxml", event);
    }

}
