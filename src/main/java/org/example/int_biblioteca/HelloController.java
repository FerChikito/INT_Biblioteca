package org.example.int_biblioteca;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Button ojo;
    @FXML
    private PasswordField pass;
    @FXML
    private TextField verPass;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    public void verPass(ActionEvent event) {
        String passNormal = pass.getText();

        pass.setVisible(false);
        verPass.setVisible(true);
        verPass.setText(passNormal);
    }
}