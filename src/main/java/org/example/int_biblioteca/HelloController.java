package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HelloController {
    @FXML private TextField matriculaField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private TextField correoField;
    @FXML private TextField confirmarCorreoField;
    @FXML private Button confirmarButton;
    @FXML private Button togglePasswordButton;

    @FXML
    private void initialize() {
        // Configurar el botón de mostrar/ocultar contraseña
        if (togglePasswordButton != null) {
            togglePasswordButton.setText("👁");
            togglePasswordButton.setOnAction(event -> togglePasswordVisibility());
        }

        if (passwordVisibleField != null) {
            passwordVisibleField.setVisible(false);
        }

        if (confirmarButton != null) {
            confirmarButton.setOnAction(event -> handleConfirmar());
        }
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
        // Validación básica de campos
        if (matriculaField.getText().isEmpty() ||
                (passwordField.getText().isEmpty() && passwordVisibleField.getText().isEmpty()) ||
                correoField.getText().isEmpty() ||
                confirmarCorreoField.getText().isEmpty()) {

            showAlert("Error", "Todos los campos son obligatorios");
            return;
        }

        if (!correoField.getText().equals(confirmarCorreoField.getText())) {
            showAlert("Error", "Los correos no coinciden");
            return;
        }

        showAlert("Éxito", "Contraseña recuperada correctamente");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}