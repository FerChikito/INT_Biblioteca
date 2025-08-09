package org.example.int_biblioteca;

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

    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    @FXML
    public void initialize() {
        validarFormulario(); // Inicializa el botón deshabilitado
    }

    @FXML
    public void validarFormulario() {
        boolean todosLlenos = !nombreField.getText().trim().isEmpty()
                && !apellidoField.getText().trim().isEmpty()
                && fechaNacimientoPicker.getValue() != null
                && !correoField.getText().trim().isEmpty()
                && !confirmarCorreoField.getText().trim().isEmpty()
                && !passwordField.getText().trim().isEmpty()
                && !confirmPasswordField.getText().trim().isEmpty();

        boolean correosIguales = correoField.getText().equals(confirmarCorreoField.getText());
        boolean contrasIguales = passwordField.getText().equals(confirmPasswordField.getText());
        boolean terminosAceptados = terminosCheck.isSelected();

        registrarBtn.setDisable(!(todosLlenos && correosIguales && contrasIguales && terminosAceptados));
    }

    @FXML
    public void togglePassword() {
        passwordVisible = !passwordVisible;
        togglePassBtn.setText(passwordVisible ? " " : "👁");
        // Se puede implementar lógica con TextField si deseas visibilidad real
    }

    @FXML
    public void toggleConfirmPassword() {
        confirmPasswordVisible = !confirmPasswordVisible;
        toggleConfirmPassBtn.setText(confirmPasswordVisible ? " " : "👁");
    }
}
