package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

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
}