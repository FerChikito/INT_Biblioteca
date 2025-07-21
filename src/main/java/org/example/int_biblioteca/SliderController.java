package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import java.util.ArrayList;
import java.util.List;

public class SliderController {
    @FXML
    private HBox sliderContainer;
    @FXML
    private Button btnIzq;
    @FXML
    private Button btnDerch;

    private List<Label> slides = new ArrayList<>();
    private int currentSlideIndex = 0;
    @FXML
    public void initialize() {
        crearSlides();
        mostrarSlideActual();
        // Configurar botones
        btnIzq.setOnAction(this::handleBtnIzq);
        btnDerch.setOnAction(this::handleBtnDerch);
    }

    private void crearSlides() {
        // Crear y agregar los slides
        Label slide1 = new Label("¡Bienvenido a la Biblioteca!\n\nAquí encontrarás los mejores libros.");
        slide1.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: center;");
        Label slide2 = new Label("Horario de atención:\nLunes a Viernes: 9am - 6pm\nSábados: 10am - 2pm");
        slide2.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: center;");
        Label slide3 = new Label("Novedades:\n- Nueva colección de ciencia ficción\n- Taller de lectura cada jueves");
        slide3.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: center;");
        slides.add(slide1);
        slides.add(slide2);
        slides.add(slide3);
    }

    private void mostrarSlideActual() {
        sliderContainer.getChildren().clear();
        sliderContainer.getChildren().add(slides.get(currentSlideIndex));
    }
    private void handleBtnIzq(ActionEvent event) {
        currentSlideIndex--;
        if (currentSlideIndex < 0) {
            currentSlideIndex = slides.size() - 1; // Volver al último slide
        }
        mostrarSlideActual();
    }
    private void handleBtnDerch(ActionEvent event) {
        currentSlideIndex++;
        if (currentSlideIndex >= slides.size()) {
            currentSlideIndex = 0; // Volver al primer slide
        }
        mostrarSlideActual();
    }
}
