package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.example.int_biblioteca.dao.ConfigDAO; // <-- IMPORTANTE
// Si usas CmsDAO para persistir info/slider, impórtalo también
// import org.example.int_biblioteca.dao.CmsDAO;

public class MenuPrincipal {

    // Lado izquierdo
    @FXML private Button botonMenu;
    @FXML private Button botonPerfil;
    @FXML private Button botonCatalogoL;
    @FXML private Button botonUsuarios;
    @FXML private Button editarLibrosBtn;
    @FXML private Button editarInfoBtn;
    @FXML private Button editarSliderBtn;
    @FXML private Button btnConfigMulta;  // Botón "Tarifa multa ⚖️" en el FXML
    @FXML private Button btnConfigDias;   // Botón "Días límite 📅" en el FXML
    @FXML private Button btnLogout;

    // Top
    @FXML private TextField buscarField;
    @FXML private HBox barraBusquedaSuperior;

    // Centro
    @FXML private BorderPane rootPane;
    @FXML private HBox sliderContainer;
    @FXML private VBox infoP;

    private Usuario usuarioActual;
    private Parent catalogoRoot;
    private CatalogoLibrosController catalogoController;
    private Node originalCenter;

    private SliderController sliderController;

    @FXML
    public void initialize() {
        cargarSlider();
        // Texto por defecto para que NO se vea en blanco
        renderInfo("""
                INFORMACIÓN PRINCIPAL
                Bienvenido a la Biblioteca Digital.
                
                • Horario: Lunes a Viernes (9:00 - 18:00)
                • Préstamos: Máximo 5 libros por usuario
                • Contacto: biblioteca@ejemplo.com
                """);

        originalCenter = rootPane.getCenter();
        aplicarPermisosPorRol();

        // Que no dejen hueco al ocultarlos
        mirrorManagedToVisible(botonUsuarios, editarInfoBtn, editarSliderBtn, editarLibrosBtn,
                btnConfigMulta, btnConfigDias, btnLogout);

        mostrarBotonesDeEdicion();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        aplicarPermisosPorRol();
        mostrarBotonesDeEdicion();
    }

    private void aplicarPermisosPorRol() {
        Rol r = (usuarioActual != null) ? usuarioActual.getRol() : Rol.USUARIO;

        setVis(botonUsuarios, false);
        setVis(editarLibrosBtn, false);
        setVis(editarInfoBtn, false);
        setVis(editarSliderBtn, false);
        setVis(btnConfigMulta, false);
        setVis(btnConfigDias, false);

        switch (r) {
            case SUPER_ADMIN -> setVis(botonUsuarios, true);
            case ADMIN, BIBLIOTECARIO -> {
                setVis(botonUsuarios, true);
                setVis(editarLibrosBtn, true);
                setVis(editarInfoBtn, true);
                setVis(editarSliderBtn, true);
                setVis(btnConfigMulta, true);
                setVis(btnConfigDias, true);
            }
            case USUARIO -> {}
        }
        setVis(btnLogout, usuarioActual != null);
    }

    private void mostrarBotonesDeEdicion() { aplicarPermisosPorRol(); }

    @FXML
    private void handleBotonMenu(ActionEvent event) {
        if (originalCenter != null && rootPane.getCenter() != originalCenter) {
            rootPane.setCenter(originalCenter);
        }
        restaurarBarraSuperior();
    }

    @FXML
    private void handleBotonPerfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("perfil.fxml"));
            Parent vista = loader.load();
            PerfilController ctrl = loader.getController();
            ctrl.setUsuarioActual(usuarioActual);
            ctrl.setOnMostrarVista(v -> rootPane.setCenter(v));
            ctrl.setOnRegresar(() -> {
                if (originalCenter != null && rootPane.getCenter() != originalCenter) {
                    rootPane.setCenter(originalCenter);
                }
                restaurarBarraSuperior();
            });
            rootPane.setCenter(vista);
            restaurarBarraSuperior();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Perfil", "No se pudo abrir la vista de perfil:\n" + e.getMessage());
        }
    }

    @FXML private void handleBotonCatalogoL(ActionEvent e) { abrirCatalogo(null); }

    @FXML private void buscarGlobal() {
        String q = (buscarField.getText() == null) ? "" : buscarField.getText().trim();
        abrirCatalogo(q);
    }

    @FXML
    private void abrirUsuarios(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("crud-usuarios.fxml"));
            Parent vista = loader.load();
            CrudUsuariosController ctrl = loader.getController();
            ctrl.setRolActual(usuarioActual != null ? usuarioActual.getRol() : Rol.USUARIO);
            rootPane.setCenter(vista);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Usuarios", "No se pudo cargar Usuarios:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirMisPrestamos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mis-prestamos.fxml"));
            Parent vista = loader.load();

            MisPrestamosController ctrl = loader.getController();
            ctrl.setUsuarioActual(usuarioActual);
            ctrl.setRolActual(usuarioActual != null ? usuarioActual.getRol() : Rol.USUARIO);
            ctrl.cargarDatos();

            rootPane.setCenter(vista);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Mis Préstamos", "No se pudo abrir:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditarLibros(ActionEvent event) {
        if (!puedeEditar()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("crud-libros.fxml"));
            Parent crudView = loader.load();
            rootPane.setCenter(crudView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Libros", "No se pudo cargar la vista de edición de libros.");
        }
    }

    @FXML
    private void handleEditarInfo(ActionEvent event) {
        if (!puedeEditar()) return;

        // Toma el texto actual visible en infoP
        StringBuilder actual = new StringBuilder();
        for (Node n : new ArrayList<>(infoP.getChildren())) {
            if (n instanceof Label l) {
                if (!actual.isEmpty()) actual.append("\n");
                actual.append(l.getText());
            }
        }

        TextArea editor = new TextArea(actual.toString());
        editor.setPromptText("Escribe la información principal (líneas en párrafos).");

        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Editar información principal");
        d.getDialogPane().setContent(editor);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                renderInfo(editor.getText()); // pinta en pantalla
                // Si quieres persistirlo: CmsDAO.set("INFO_PRINCIPAL", editor.getText());
            }
        });
    }

    @FXML
    private void handleEditarSlider(ActionEvent event) {
        if (!puedeEditar()) return;
        if (sliderController == null) return;

        String inicial = String.join("\n---\n", sliderController.getSlidesText());
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar slider");
        dialog.setHeaderText("Cada diapositiva sepárala con una línea que contenga solo ---");
        TextArea area = new TextArea(inicial);
        area.setPrefColumnCount(60);
        area.setPrefRowCount(14);
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            String[] partes = area.getText().split("(?m)^\\s*---\\s*$");
            List<String> nuevas = new ArrayList<>();
            for (String p : partes) {
                String s = p.strip();
                if (!s.isEmpty()) nuevas.add(s);
            }
            if (!nuevas.isEmpty()) {
                sliderController.setSlidesText(nuevas);
                // Persistencia opcional: CmsDAO.set("SLIDER_TEXT", String.join("\n---\n", nuevas));
            }
        });
    }

    // ========== HANDLERS QUE FALTABAN (COINCIDEN CON TU FXML) ==========
    @FXML
    private void handleEditarTarifaMulta(ActionEvent e) {
        if (!puedeEditar()) return;

        double actual = 15.0; // valor por defecto si BD falla
        try { actual = ConfigDAO.getTarifaMulta(); } catch (Exception ignored) {}

        TextInputDialog d = new TextInputDialog(String.valueOf(actual));
        d.setTitle("Tarifa de multa");
        d.setHeaderText("Introduce la tarifa por día de atraso (por ejemplo, 50.0)");
        d.setContentText("Tarifa por día:");
        d.showAndWait().ifPresent(txt -> {
            try {
                double v = Double.parseDouble(txt);
                if (v < 0) throw new NumberFormatException();
                if (ConfigDAO.setTarifaMulta(v)) {
                    showAlert(Alert.AlertType.INFORMATION, "Multa", "Tarifa actualizada a $" + v + " por día.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Multa", "No se pudo actualizar la tarifa.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Valor inválido", "Escribe un número válido (>= 0).");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error guardando la tarifa:\n" + ex.getMessage());
            }
        });
    }

    @FXML
    private void handleEditarDiasLimite(ActionEvent e) {
        if (!puedeEditar()) return;

        int actual = 3; // valor por defecto si BD falla
        try { actual = ConfigDAO.getDiasLimite(); } catch (Exception ignored) {}

        TextInputDialog d = new TextInputDialog(String.valueOf(actual));
        d.setTitle("Días límite de préstamo");
        d.setHeaderText("Introduce la cantidad de días permitidos para devolver el libro");
        d.setContentText("Días:");
        d.showAndWait().ifPresent(txt -> {
            try {
                int v = Integer.parseInt(txt);
                if (v <= 0) throw new NumberFormatException();
                if (ConfigDAO.setDiasLimite(v)) {
                    showAlert(Alert.AlertType.INFORMATION, "Días límite", "Días límite actualizados a " + v + ".");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Días límite", "No se pudo actualizar el valor.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Valor inválido", "Escribe un entero válido (> 0).");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error guardando los días límite:\n" + ex.getMessage());
            }
        });
    }
    // =========================================================

    // === Helpers ===
    private boolean puedeEditar() {
        if (usuarioActual == null) return false;
        Rol r = usuarioActual.getRol();
        return (r == Rol.ADMIN || r == Rol.BIBLIOTECARIO);
    }

    private void abrirCatalogo(String query) {
        try {
            if (catalogoRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("catalogo-libros.fxml"));
                catalogoRoot = loader.load();
                catalogoController = loader.getController();
            }
            if (catalogoController != null) {
                catalogoController.setUsuarioActual(usuarioActual);
                if (query != null && !query.isBlank()) {
                    catalogoController.buscarDesdeExterno(query);
                }
            }
            rootPane.setCenter(catalogoRoot);
            ocultarBarraSuperior(true);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Catálogo", "No se pudo abrir el catálogo:\n" + e.getMessage());
        }
    }

    private void cargarSlider() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("slider.fxml"));
            HBox slider = loader.load();
            sliderController = loader.getController();
            slider.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            HBox.setHgrow(slider, Priority.ALWAYS);
            sliderContainer.getChildren().setAll(slider);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Renderiza infoP en párrafos (líneas vacías insertan espacio). */
    private void renderInfo(String texto) {
        infoP.getChildren().clear();
        if (texto == null || texto.isBlank()) {
            infoP.getChildren().add(new Label("(sin información)"));
            return;
        }
        String[] lineas = texto.split("\\R");
        for (String ln : lineas) {
            if (ln.isBlank()) {
                Region sep = new Region();
                sep.setMinHeight(6);
                infoP.getChildren().add(sep);
            } else {
                Label lbl = new Label(ln);
                lbl.setWrapText(true);
                lbl.setStyle("-fx-text-fill: #1B2D4F; -fx-font-size: 14px;");
                lbl.setMaxWidth(Double.MAX_VALUE);
                infoP.getChildren().add(lbl);
            }
        }
    }

    private void ocultarBarraSuperior(boolean ocultar) {
        if (barraBusquedaSuperior != null) {
            barraBusquedaSuperior.setVisible(!ocultar);
            barraBusquedaSuperior.setManaged(!ocultar);
        }
    }
    private void restaurarBarraSuperior() { ocultarBarraSuperior(false); }

    private void setVis(Button b, boolean visible) {
        if (b == null) return;
        if (b.managedProperty().isBound()) {
            b.visibleProperty().set(visible);
        } else {
            b.setVisible(visible);
            b.setManaged(visible);
        }
    }

    private void mirrorManagedToVisible(Control... nodes) {
        if (nodes == null) return;
        for (Control n : nodes) {
            if (n != null) n.managedProperty().bind(n.visibleProperty());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCerrarSesion(ActionEvent e) {
        try {
            this.usuarioActual = null;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent login = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(login));
            stage.show();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Cerrar sesión", "No se pudo regresar al inicio:\n" + ex.getMessage());
        }
    }
}
