package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

public class SliderController {

    @FXML private StackPane slidePane;   // donde mostramos el slide actual
    @FXML private Button btnIzq;         // botones (opcional si usas onAction en FXML)
    @FXML private Button btnDerch;

    private final List<Node> slides = new ArrayList<>();
    private int indiceActual = 0;

    /* ===== API pública para que MenuPrincipal edite el contenido ===== */

    /** Reemplaza todas las diapositivas con nodos arbitrarios. */
    public void setSlidesNodes(List<Node> nuevos) {
        slides.clear();
        if (nuevos != null) slides.addAll(nuevos);
        indiceActual = 0;
        render();
    }

    /** Reemplaza todas las diapositivas con etiquetas de texto (cómodo para edición). */
    public void setSlidesText(List<String> textos) {
        List<Node> nodos = new ArrayList<>();
        if (textos != null) {
            for (String s : textos) {
                Label lbl = new Label(s);
                lbl.setWrapText(true);
                lbl.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-alignment: center;");
                lbl.setMaxWidth(Double.MAX_VALUE);
                nodos.add(lbl);
            }
        }
        setSlidesNodes(nodos);
    }

    /** Devuelve una copia de los textos actuales (si los nodos son Labels). */
    public List<String> getSlidesText() {
        List<String> out = new ArrayList<>();
        for (Node n : slides) {
            if (n instanceof Label lbl) out.add(lbl.getText());
        }
        return out;
    }

    /* ====================== Navegación ====================== */

    @FXML
    private void next() {
        if (slides.isEmpty()) return;
        indiceActual = (indiceActual + 1) % slides.size();
        render();
    }

    @FXML
    private void prev() {
        if (slides.isEmpty()) return;
        indiceActual = (indiceActual - 1 + slides.size()) % slides.size();
        render();
    }

    private void render() {
        if (slides.isEmpty()) {
            Label vacio = new Label("(sin contenido)");
            vacio.setStyle("-fx-text-fill: #555; -fx-font-style: italic;");
            slidePane.getChildren().setAll(vacio);
        } else {
            slidePane.getChildren().setAll(slides.get(indiceActual));
        }
    }

    @FXML
    private void initialize() {
        // Contenido por defecto
        if (slides.isEmpty()) {
            setSlidesText(List.of(
                    "¡Bienvenido a la Biblioteca!\n\nAquí encontrarás los mejores libros.",
                    "Horario de atención:\nLunes a Viernes 9:00–18:00\nSábados 10:00–14:00",
                    "Novedades:\n• Ciencia ficción\n• Taller de lectura (jueves)"
            ));
        }

        // Si no pusiste onAction en el FXML, puedes enganchar aquí:
        if (btnIzq   != null) btnIzq.setOnAction(e -> prev());
        if (btnDerch != null) btnDerch.setOnAction(e -> next());
    }
}
