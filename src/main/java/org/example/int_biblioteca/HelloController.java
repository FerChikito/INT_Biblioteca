package org.example.int_biblioteca;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Button ojo;
    @FXML
    private PasswordField pass;
    @FXML
    private TextField verPass;

    private Stage stage;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    private boolean isPasswordVisible = false; // Variable para controlar la visibilidad
    @FXML
    public void verPass(ActionEvent event) {
        String passNormal = pass.getText();
        if (!isPasswordVisible) {
            // Mostrar la contraseña
            pass.setVisible(false);
            verPass.setVisible(true);
            verPass.setText(passNormal);
            isPasswordVisible = true; // Cambiar el estado a visible
        } else {
            // Ocultar la contraseña
            pass.setVisible(true);
            verPass.setVisible(false);
            isPasswordVisible = false; // Cambiar el estado a no visible
        }
    }

}