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