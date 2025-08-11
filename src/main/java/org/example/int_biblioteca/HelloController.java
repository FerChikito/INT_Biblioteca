package org.example.int_biblioteca;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.util.Optional;
import org.example.int_biblioteca.dao.UsuarioDAO;

public class HelloController {
    @FXML private Label welcomeText;

    // Login
    @FXML private Button ojo;
    @FXML private PasswordField passwordField;
    @FXML private TextField verPass; // visible cuando “ojo” activo
    @FXML private TextField usernameField;

    // Registro
    @FXML private TextField fullNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordFieldReg;
    @FXML private PasswordField confirmPasswordField;

    private boolean isPasswordVisible = false;

    @FXML
    public void verPass(ActionEvent event) {
        String passNormal = passwordField.getText();
        if (!isPasswordVisible) {
            passwordField.setVisible(false);
            verPass.setVisible(true);
            verPass.setText(passNormal);
        } else {
            passwordField.setVisible(true);
            verPass.setVisible(false);
        }
        isPasswordVisible = !isPasswordVisible;
    }

    // ===== LOGIN contra BD (correo + contraseña) =====
    @FXML
    protected void handleLogin(ActionEvent event) {
        String correo = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String pass   = isPasswordVisible ? verPass.getText() : passwordField.getText();

        if (correo.isEmpty() || pass == null || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos vacíos", "Ingresa correo y contraseña.");
            return;
        }
        try {
            Optional<Usuario> opt = UsuarioDAO.autenticar(correo, pass);
            if (!opt.isPresent()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Usuario o contraseña incorrectos");
                return;
            }
            Usuario usuario = opt.get();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/int_biblioteca/menu-principal.fxml"));
            Scene scene = new Scene(loader.load());

            MenuPrincipal controller = loader.getController();
            controller.setUsuario(usuario);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo iniciar sesión:\n" + e.getMessage());
        }
    }

    // ===== Registro: inserta en USUARIOS usando UsuarioDAO =====
    @FXML
    protected void handleRegisterFromForm(ActionEvent event) {
        String nombre = safe(fullNameField.getText());
        String correo = safe(emailField.getText());
        String tel    = safe(phoneField.getText());
        String dir    = safe(addressField.getText());
        String pass   = passwordFieldReg.getText() == null ? "" : passwordFieldReg.getText();
        String confirm= confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Nombre, correo y contraseña son obligatorios.");
            return;
        }
        if (!pass.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Las contraseñas no coinciden");
            return;
        }

        try {
            boolean ok = UsuarioDAO.insertar(new Usuario(nombre, correo, tel, dir, pass, Rol.USUARIO));
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Registro", "Usuario registrado con éxito");
                cambiarEscena("hello-view.fxml", event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Registro", "No se pudo registrar (revisa si el correo ya existe).");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Registro", "Error al registrar:\n" + e.getMessage());
        }
    }

    @FXML
    protected void handleRegister(ActionEvent event) {
        cambiarEscena("Registro.fxml", event);
    }

    @FXML
    protected void handleCancel(ActionEvent event) {
        cambiarEscena("hello-view.fxml", event);
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
}
