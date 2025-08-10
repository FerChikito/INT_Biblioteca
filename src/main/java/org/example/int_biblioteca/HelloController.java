package org.example.int_biblioteca;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Button ojo; // Botón para mostrar/ocultar contraseña
    @FXML
    private PasswordField passwordField; // Campo de contraseña para el login
    @FXML
    private TextField verPass; // Campo de texto para mostrar la contraseña

    private Stage stage;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    // Campos del login
    @FXML
    private TextField usernameField; // Campo para el nombre de usuario

    // Campos del registro
    @FXML
    private TextField fullNameField; // Campo para el nombre completo
    @FXML
    private TextField addressField; // Campo para la dirección
    @FXML
    private TextField phoneField; // Campo para el teléfono
    @FXML
    private TextField emailField;  // Campo para el correo electrónico
    @FXML
    private PasswordField passwordFieldReg;  // Campo de contraseña para el registro
    @FXML
    private PasswordField confirmPasswordField; // Campo para confirmar la contraseña

    @FXML
    private boolean isPasswordVisible = false; // Variable para controlar la visibilidad

    @FXML
    public void verPass(ActionEvent event) {
        String passNormal = passwordField.getText();
        if (!isPasswordVisible) {
            // Mostrar la contraseña
            passwordField.setVisible(false);
            verPass.setVisible(true);
            verPass.setText(passNormal);
            isPasswordVisible = true; // Cambiar el estado a visible
        } else {
            // Ocultar la contraseña
            passwordField.setVisible(true);
            verPass.setVisible(false);
            isPasswordVisible = false; // Cambiar el estado a no visible
        }
    }

    @FXML
    protected void onLoginClick() {
        // Obtiene el texto del campo de contraseña y el campo de texto visible
        String password = isPasswordVisible ? verPass.getText() : passwordField.getText();
        // Verifica si el campo de contraseña está vacío y muestra una alerta correspondiente
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Campo vacío", "Por favor, completa el campo de contraseña.");
        } else {
            // Muestra una alerta de inicio de sesión exitoso (puedes personalizar este mensaje)
            showAlert(Alert.AlertType.INFORMATION, "Inicio de sesión", "Bienvenido!");
        }
    }

    //Método para mostrar alertas
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); // Establece el título de la alerta
        alert.setHeaderText(null); // No se establece encabezado
        alert.setContentText(content); // Establece el contenido de la alerta
        alert.showAndWait(); // Muestra la alerta y espera a que se cierre
    }

    // Simulación de base de datos
    private static final List<Usuario> usuarios = new ArrayList<>();

    static {
        usuarios.add(new Usuario("isaak", "123", Rol.SUPER_ADMIN));
        usuarios.add(new Usuario("emi", "123", Rol.BIBLIOTECARIO));
        usuarios.add(new Usuario("jesus", "123", Rol.ADMIN));
        usuarios.add(new Usuario("josh", "111", Rol.USUARIO));
    }
    private static Usuario usuarioActual;

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    // LOGIN
    @FXML
    protected void handleLogin(ActionEvent event) {
        String correo = usernameField.getText();
        String pass = passwordField.getText();

        for (Usuario usuario : usuarios) {
            if (usuario.getNombre().equals(correo) && usuario.getContrasenia().equals(pass)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/int_biblioteca/menu-principal.fxml"));
                    Scene scene = new Scene(loader.load());

                    // Aquí pasas el usuario al controlador del menú
                    MenuPrincipal controller = loader.getController();
                    controller.setUsuario(usuario);

                    // Luego muestras la escena
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar el menú");
                }
                return;
            }
        }

        showAlert(Alert.AlertType.ERROR, "Error", "Usuario o contraseña incorrectos");
    }

    // Desde login, ir a registro
    @FXML
    protected void handleRegister(ActionEvent event) {
        cambiarEscena("Registro.fxml", event);
    }

    // Desde registro, guardar usuario
    @FXML
    protected void handleRegisterFromForm(ActionEvent event) {
        String correo = emailField.getText();
        String pass = passwordFieldReg.getText();
        String confirm = confirmPasswordField.getText();

        if (!pass.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Las contraseñas no coinciden");
            return;
        }

        // Aquí puedes decidir si registrar como usuario normal
        usuarios.add(new Usuario(correo, pass, Rol.USUARIO));
        showAlert(Alert.AlertType.INFORMATION, "Registro", "Usuario registrado con éxito");
        cambiarEscena("hello-view.fxml", event);
    }

    // Desde registro, cancelar
    @FXML
    protected void handleCancel(ActionEvent event) {
        cambiarEscena("hello-view.fxml", event);
    }

    private void cambiarEscena(String fxml, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml)); // Cargar la nueva escena
            if (loader.getLocation() == null) {
                throw new IOException("No se encontró el archivo FXML: " + fxml);
            }
            Scene scene = new Scene(loader.load()); // Crear la nueva escena
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Obtener la ventana actual
            stage.setScene(scene); // Cambiar la escena
            stage.show(); // Mostrar la nueva escena
        } catch (IOException e) {
            e.printStackTrace(); // Imprimir la traza de la excepción en caso de error
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la pantalla: " + fxml);
        }

    }
}