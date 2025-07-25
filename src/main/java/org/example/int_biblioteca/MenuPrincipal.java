package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MenuPrincipal {
    // Declaración de los componentes de la interfaz
    @FXML
    private Button botonMenu; // Botón para abrir el menú
    @FXML
    private Button botonPerfil; // Botón para acceder al perfil
    @FXML
    private Button botonCatalogoL; // Botón para acceder al catálogo de libros
    @FXML
    private TextField buscarField; // Campo de texto para buscar
    @FXML
    private HBox sliderContainer; // Contenedor para el slider
    @FXML
    private VBox infoP;

    // Método que se ejecuta al hacer clic en el botón de menú
    @FXML
    private void handleBotonMenu(ActionEvent event) {
        // Lógica para abrir el menú
        System.out.println("Botón Menú presionado");
    }

    // Método que se ejecuta al hacer clic en el botón de perfil
    @FXML
    private void handleBotonPerfil(ActionEvent event) {
        // Lógica para acceder al perfil
        System.out.println("Botón Perfil presionado");
    }

    // Método que se ejecuta al hacer clic en el botón de catálogo
    @FXML
    private void handleBotonCatalogoL(ActionEvent event) {
        // Lógica para acceder al catálogo de libros
        System.out.println("Botón Catálogo de Libros presionado");
    }

    // Método para inicializar el slider
    @FXML
    public void initialize() {
        cargarSlider();
        cargarTexto(); // Llama a cargarTexto aquí
    }

    private void cargarSlider() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("slider.fxml")); // Asegúrate de que la ruta sea correcta
            HBox slider = loader.load();
            sliderContainer.getChildren().add(slider);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private void cargarTexto() {
        // Limpiar el VBox
        infoP.getChildren().clear();
        // Crear el texto
        Label titulo = new Label("INFORMACIÓN PRINCIPAL");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Crear un Label para el contenido
        Label contenido = new Label("Bienvenido a la Biblioteca Digital.");
        contenido.setWrapText(true); // Permite que el texto se ajuste a múltiples líneas
        // Añadir el contenido adicional
        Label horario = new Label("\n• Horario: Lunes a Viernes (9:00 - 18:00)");
        Label prestamos = new Label("\n• Préstamos: Máximo 5 libros por usuario");
        Label contacto = new Label("\n• Contacto: biblioteca@ejemplo.com");
        // Añadir al VBox
        infoP.getChildren().addAll(titulo, contenido, horario, prestamos, contacto);
    }
}