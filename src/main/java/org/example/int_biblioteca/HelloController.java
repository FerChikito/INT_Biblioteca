package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HelloController {
    // Elementos comunes
    @FXML private Label welcomeText;
    @FXML private Button togglePasswordButton;

    // Campos para recuperación de contraseña
    @FXML private TextField matriculaField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private TextField correoField;
    @FXML private TextField confirmarCorreoField;
    @FXML private Button confirmarButton;

    @FXML
    private void initialize() {
        if (togglePasswordButton != null) {
            togglePasswordButton.setText("👁");
        }
        if (passwordVisibleField != null) {
            passwordVisibleField.setVisible(false);
        }
    }

    @FXML
    private void onHelloButtonClick() {
        if (welcomeText != null) {
            welcomeText.setText("¡Bienvenido a la Biblioteca!");
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        if (passwordField == null || passwordVisibleField == null) return;

        if (passwordVisibleField.isVisible()) {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordVisibleField.setVisible(false);
            togglePasswordButton.setText("👁");
        } else {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordField.setVisible(false);
            togglePasswordButton.setText("🔒");
        }
    }

    // Método principal de validación
    @FXML
    private void handleConfirmar() {
        if (!validarCampos()) return;
        if (!validarCorreosIguales()) return;
        if (!validarFormatoCorreo()) return;
        if (!validarFortalezaPassword()) return;

        showAlert("Éxito", "Registro completado correctamente");
    }

    private boolean validarCampos() {
        if (matriculaField.getText().trim().isEmpty()) {
            showAlert("Error", "La matrícula es obligatoria");
            return false;
        }

        String password = passwordVisibleField.isVisible() ?
                passwordVisibleField.getText() : passwordField.getText();
        if (password.isEmpty()) {
            showAlert("Error", "La contraseña es obligatoria");
            return false;
        }

        if (correoField.getText().trim().isEmpty()) {
            showAlert("Error", "El correo electrónico es obligatorio");
            return false;
        }

        return true;
    }

    private boolean validarCorreosIguales() {
        if (!correoField.getText().equals(confirmarCorreoField.getText())) {
            showAlert("Error", "Los correos electrónicos no coinciden");
            return false;
        }
        return true;
    }

    private boolean validarFormatoCorreo() {
        String email = correoField.getText().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showAlert("Error", "Formato de correo electrónico inválido\nEjemplo válido: usuario@dominio.com");
            return false;
        }
        return true;
    }

    private boolean validarFortalezaPassword() {
        String password = passwordVisibleField.isVisible() ?
                passwordVisibleField.getText() : passwordField.getText();

        if (password.length() < 8) {
            showAlert("Error", "La contraseña debe tener al menos 8 caracteres");
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            showAlert("Error", "La contraseña debe contener al menos una mayúscula");
            return false;
        }

        if (!password.matches(".*[0-9].*")) {
            showAlert("Error", "La contraseña debe contener al menos un número");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}