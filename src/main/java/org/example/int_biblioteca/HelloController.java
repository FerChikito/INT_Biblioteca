package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    private void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    private TextField matriculaField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private TextField correoField;
    @FXML
    private TextField confirmarCorreoField;
    @FXML
    private Button confirmarButton;
    @FXML
    private Button togglePasswordButton;

    @FXML
    private void initialize() {
        // Inicializar el estado del botón de visibilidad
        togglePasswordButton.setText("👁");
    }

    @FXML
    private void togglePasswordVisibility() {
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

    @FXML
    private void handleConfirmar() {
        // Validar campos vacíos
        if (matriculaField.getText().isEmpty() ||
                (passwordField.getText().isEmpty() && passwordVisibleField.getText().isEmpty()) ||
                correoField.getText().isEmpty() || confirmarCorreoField.getText().isEmpty()) {
            showAlert("Error", "Todos los campos son obligatorios");
            return;
        }

        // Validar que los correos coincidan
        if (!correoField.getText().equals(confirmarCorreoField.getText())) {
            showAlert("Error", "Los correos no coinciden");
            return;
        }

        // Validar formato de correo (simple)
        if (!correoField.getText().contains("@") || !correoField.getText().contains(".")) {
            showAlert("Error", "Ingrese un correo electrónico válido");
            return;
        }

        // Si todo está bien
        showAlert("Éxito", "Contraseña actualizada correctamente");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}