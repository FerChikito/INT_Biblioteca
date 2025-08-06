package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegistroController {

    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private DatePicker fechaNacimientoPicker;
    @FXML private TextField correoField;
    @FXML private TextField confirmarCorreoField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox terminosCheck;
    @FXML private Button registrarBtn;
    @FXML private Button togglePassBtn;
    @FXML private Button toggleConfirmPassBtn;

    @FXML
    public void initialize() {
        validarFormulario(); // Inicializa el botón deshabilitado
        boolean todosLlenos = !nombreField.getText().trim().isEmpty()
                && !apellidoField.getText().trim().isEmpty()
                && fechaNacimientoPicker.getValue() != NULL
                && !correoField.getText().trim().isEmpty()
                && !confirmarCorreoField.getText().trim().isEmpty()
                && !passwordField.getText().trim().isEmpty()
                && !confirmPasswordField.getText().trim().isEmpty();

        boolean correosIguales = correoField.getText().equals(confirmarCorreoField.getText());
        boolean contrasIguales = passwordField.getText().equals(confirmPasswordField.getText());

    }
