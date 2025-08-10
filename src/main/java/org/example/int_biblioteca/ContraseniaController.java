package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ContraseniaController {

    @FXML private PasswordField currentPasswordField1;     // contraseña actual (oculta)
    @FXML private TextField     currentPasswordVisibleField; // actual (visible)
    @FXML private Button        toggleCurrentPasswordButton;

    @FXML private PasswordField newPasswordField;          // nueva (oculta)
    @FXML private TextField     newPasswordVisibleField;   // nueva (visible)
    @FXML private Button        toggleNewPasswordButton;

    @FXML private Button        confirmarButton;

    private boolean currentVisible = false;
    private boolean newVisible = false;

    @FXML
    private void initialize() {
        // Para que el TextField visible no ocupe espacio cuando esté oculto
        if (currentPasswordVisibleField != null)
            currentPasswordVisibleField.managedProperty().bind(currentPasswordVisibleField.visibleProperty());
        if (newPasswordVisibleField != null)
            newPasswordVisibleField.managedProperty().bind(newPasswordVisibleField.visibleProperty());

        // Estado inicial como en hello-view
        if (currentPasswordField1 != null) currentPasswordField1.setVisible(true);
        if (currentPasswordVisibleField != null) currentPasswordVisibleField.setVisible(false);
        if (newPasswordField != null) newPasswordField.setVisible(true);
        if (newPasswordVisibleField != null) newPasswordVisibleField.setVisible(false);

        if (toggleCurrentPasswordButton != null) toggleCurrentPasswordButton.setText("👁");
        if (toggleNewPasswordButton != null) toggleNewPasswordButton.setText("👁");
    }

    // ===== Ojito contraseña actual =====
    @FXML
    private void toggleCurrentPassword() {
        currentVisible = !currentVisible;
        if (currentVisible) {
            currentPasswordVisibleField.setText(currentPasswordField1.getText());
            currentPasswordVisibleField.setVisible(true);
            currentPasswordField1.setVisible(false);
            toggleCurrentPasswordButton.setText("🙈");
        } else {
            currentPasswordField1.setText(currentPasswordVisibleField.getText());
            currentPasswordField1.setVisible(true);
            currentPasswordVisibleField.setVisible(false);
            toggleCurrentPasswordButton.setText("\uD83D\uDC40");
        }
    }

    // ===== Ojito contraseña nueva =====
    @FXML
    private void toggleNewPassword() {
        newVisible = !newVisible;
        if (newVisible) {
            newPasswordVisibleField.setText(newPasswordField.getText());
            newPasswordVisibleField.setVisible(true);
            newPasswordField.setVisible(false);
            toggleNewPasswordButton.setText("🙈");
        } else {
            newPasswordField.setText(newPasswordVisibleField.getText());
            newPasswordField.setVisible(true);
            newPasswordVisibleField.setVisible(false);
            toggleNewPasswordButton.setText("👁");
        }
    }

    private String getCurrentPassword() {
        return currentVisible ? currentPasswordVisibleField.getText() : currentPasswordField1.getText();
    }

    private String getNewPassword() {
        return newVisible ? newPasswordVisibleField.getText() : newPasswordField.getText();
    }

    @FXML
    private void confirmar() {
        // Aquí validas y llamas a tu DAO para actualizar.
        // Ejemplo básico:
        String actual = getCurrentPassword();
        String nueva  = getNewPassword();
        if (nueva == null || nueva.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Ingresa la nueva contraseña.").showAndWait();
            return;
        }
        // TODO: validar 'actual' contra BD y actualizar por UsuarioDAO.cambiarContrasenia(...)
        new Alert(Alert.AlertType.INFORMATION, "Contraseña actualizada (demo).").showAndWait();
    }
}
