package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class MenuPrincipal {
    // Declaración de los componentes de la interfaz
    @FXML
    private Button botonMenu; // Botón para abrir el menú
    @FXML
    private Button botonPerfil; // Botón para acceder al perfil
    @FXML
    private Button botonCatalogoL; // Botón para acceder al catálogo de libros
    @FXML
    private Button btnIzq; // Botón para navegar a la izquierda
    @FXML
    private Button btnDerch; // Botón para navegar a la derecha
    @FXML
    private TextField buscarField; // Campo de texto para buscar

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

    // Método que se ejecuta al hacer clic en el botón de navegación izquierda
    @FXML
    private void handleBtnIzq(ActionEvent event) {
        // Lógica para navegar a la izquierda
        System.out.println("Botón Izquierda presionado");
    }

    // Método que se ejecuta al hacer clic en el botón de navegación derecha
    @FXML
    private void handleBtnDerch(ActionEvent event) {
        // Lógica para navegar a la derecha
        System.out.println("Botón Derecha presionado");
    }

}
