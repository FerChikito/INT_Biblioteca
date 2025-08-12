package org.example.int_biblioteca;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.example.int_biblioteca.dao.LibroDAO;

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
    @FXML
    private Button botonEditar;
    @FXML
    private Button editarInfoBtn;
    @FXML
    private Button editarSliderBtn;
    @FXML
    private Button editarLibrosBtn;
    @FXML
    private BorderPane rootPane; // tu BorderPane raíz
    private Node originalCenter;                  // guarda aquí el centro original
    @FXML
    private StackPane centerStack;
    @FXML
    private AnchorPane sliderView;
    @FXML
    private VBox infoView;
    @FXML
    private Button botonUsuarios;      // NUEVO: abre CRUD Usuarios

    // Usuario que inició sesión
    private Usuario usuarioActual;
    private static final String CMS_INFO   = "INFO_PRINCIPAL";
    private static final String CMS_SLIDES = "SLIDER_TEXT";

    @FXML
    private void buscarGlobal() {
        String q = buscarField.getText() == null ? "" : buscarField.getText().trim();
        abrirCatalogo(q);
    }

    // Datos simulados de libros
    private List<Libro> libros = new ArrayList<>(List.of(
            new Libro("Cien años de soledad", "Gabriel García Márquez", "978-3-16-148410-0"),
            new Libro("1984", "George Orwell", "978-0-452-28423-4"),
            new Libro("El principito", "Antoine de Saint-Exupéry", "978-84-206-5352-9"),
            new Libro("Harry Potter y la piedra filosofal", "J.K. Rowling", "978-0-7475-3269-9"),
            new Libro("Fahrenheit 451", "Ray Bradbury", "978-0-7432-4722-1")
    ));

    // Método que se ejecuta al hacer clic en el botón de menú
    @FXML
    private void handleBotonMenu(javafx.event.ActionEvent event) {
        // Evita NPE y no dupliques el centro si ya está puesto
        if (originalCenter != null && rootPane.getCenter() != originalCenter) {
            rootPane.setCenter(originalCenter);
        }
        restaurarBarraSuperior(); // <- importante
    }

    // Método que se ejecuta al hacer clic en el botón de perfil
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
                restaurarBarraSuperior(); // <- importante
            });
            rootPane.setCenter(vista);
            restaurarBarraSuperior(); // <- importante
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Perfil", "No se pudo abrir la vista de perfil:\n" + e.getMessage());
        }
    }

    // Método que se ejecuta al hacer clic en el botón de catálogo
    @FXML
    private void handleBotonCatalogoL(ActionEvent event) {
        abrirCatalogo(null);
    }

    // Método para inicializar el slider
    @FXML
    public void initialize() {
        cargarSlider();
        cargarInfoDesdeBD();// Llama a cargarTexto aquí// Luego guarda el contenido original del centro
        originalCenter = rootPane.getCenter();
        aplicarPermisosPorRol(); // por si ya hay usuario seteado antes
        // Que no ocupen espacio cuando estén ocultos:
        if (botonUsuarios   != null) botonUsuarios.managedProperty().bind(botonUsuarios.visibleProperty());
        if (editarInfoBtn   != null) editarInfoBtn.managedProperty().bind(editarInfoBtn.visibleProperty());
        if (editarSliderBtn != null) editarSliderBtn.managedProperty().bind(editarSliderBtn.visibleProperty());
        if (editarLibrosBtn != null) editarLibrosBtn.managedProperty().bind(editarLibrosBtn.visibleProperty());

        mostrarBotonesDeEdicion();
    }

    // Guardamos una referencia al controller del slider cargado desde slider.fxml.
// Con esto podremos modificar su contenido (diapositivas) SIN reemplazar el nodo en pantalla.
    private SliderController sliderController;
    // ===== Cargar el slider al iniciar el menú =====
    private void cargarSlider() {
        try {
            // 1) Preparamos un FXMLLoader apuntando al FXML del slider.
            //    La ruta es relativa al paquete de esta clase: org/example/int_biblioteca/slider.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("slider.fxml"));

            // 2) Cargamos el FXML. Esto instancia el árbol de nodos (HBox raíz del slider)
            //    y también crea el controller declarado en el FXML (SliderController).
            HBox slider = loader.load();

            // 3) Obtenemos el controller real del slider. ¡Clave!
            //    Con esta referencia podremos llamar a sus métodos públicos (setSlidesText, next, prev, etc.).
            sliderController = loader.getController();

            slider.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            javafx.scene.layout.HBox.setHgrow(slider, javafx.scene.layout.Priority.ALWAYS);

            // 4) Colocamos el nodo raíz del slider dentro de nuestro contenedor en el menú.
            //    Usamos setAll para reemplazar cualquier contenido previo.
            sliderContainer.getChildren().setAll(slider);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarInfoDesdeBD() {
        try {
            // 1) INFO_PRINCIPAL
            String info = org.example.int_biblioteca.dao.CmsDAO.get(CMS_INFO);
            if (info == null || info.isBlank()) {
                // fallback si no hay nada en BD
                info = "INFORMACIÓN PRINCIPAL\n\nBienvenido a la Biblioteca Digital.\n" +
                        "• Horario: Lunes a Viernes (9:00 - 18:00)\n" +
                        "• Préstamos: Máximo 5 libros por usuario\n" +
                        "• Contacto: biblioteca@ejemplo.com";
            }
            renderInfo(info);

            // 2) SLIDER_TEXT
            String slides = org.example.int_biblioteca.dao.CmsDAO.get(CMS_SLIDES);
            if (slides != null && sliderController != null) {
                // dividir por líneas --- (una línea con solo ---)
                String[] partes = slides.split("(?m)^\\s*---\\s*$");
                java.util.List<String> lista = new java.util.ArrayList<>();
                for (String p : partes) {
                    String s = p.strip();
                    if (!s.isEmpty()) lista.add(s);
                }
                if (!lista.isEmpty()) {
                    sliderController.setSlidesText(lista);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // si falla BD, al menos muestra un fallback
            renderInfo("No se pudo cargar la información desde la base de datos.\n" +
                    "Intenta más tarde o contacta al administrador.");
        }
    }

    /** Pinta el texto de infoP con pequeños párrafos (líneas vacías separan bloques). */
    private void renderInfo(String texto) {
        infoP.getChildren().clear();
        if (texto == null || texto.isBlank()) {
            infoP.getChildren().add(new Label("(sin información)"));
            return;
        }
        String[] lineas = texto.split("\\R"); // separa por saltos de línea
        for (String ln : lineas) {
            if (ln.isBlank()) {
                // separador visual
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

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        mostrarBotonesDeEdicion();
    }


    // Muestra botón de edición solo si es admin
    private void mostrarBotonesDeEdicion() {
        Rol r = (usuarioActual != null) ? usuarioActual.getRol() : Rol.USUARIO;

        boolean verUsuarios     = (r == Rol.SUPER_ADMIN || r == Rol.ADMIN || r == Rol.BIBLIOTECARIO);
        boolean verEditarLibros = (r == Rol.SUPER_ADMIN || r == Rol.ADMIN);
        boolean verEditarSlider = (r == Rol.SUPER_ADMIN || r == Rol.ADMIN);
        boolean verEditarInfo   = (r == Rol.SUPER_ADMIN || r == Rol.ADMIN);

        if (editarLibrosBtn != null)  editarLibrosBtn.setVisible(verEditarLibros);
        if (editarSliderBtn != null)  editarSliderBtn.setVisible(verEditarSlider);
        if (editarInfoBtn != null)    editarInfoBtn.setVisible(verEditarInfo);
        if (botonEditar != null)      botonEditar.setVisible(verEditarInfo);
        if (botonUsuarios != null)    botonUsuarios.setVisible(verUsuarios);
    }

    // Acciones para botones de edición (solo admin)
    @FXML
    private void handleEditarInfo(ActionEvent event) {
        if (usuarioActual == null || (usuarioActual.getRol() != Rol.ADMIN && usuarioActual.getRol() != Rol.SUPER_ADMIN)) return;

        // 1) Carga el texto actual desde BD
        String actual = "";
        try { actual = org.example.int_biblioteca.dao.CmsDAO.get(CMS_INFO); } catch (Exception ignored) {}

        // 2) Diálogo de edición
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar información principal");
        dialog.setHeaderText("Modifica el texto que aparece debajo del slider.");
        TextArea area = new TextArea(actual == null ? "" : actual);
        area.setPrefColumnCount(60);
        area.setPrefRowCount(14);
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            String nuevo = area.getText();
            try {
                org.example.int_biblioteca.dao.CmsDAO.set(CMS_INFO, nuevo);
                renderInfo(nuevo); // refresca UI
                showAlert(Alert.AlertType.INFORMATION, "Info", "Información guardada.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Info", "No se pudo guardar:\n" + e.getMessage());
            }
        });
    }


    // ===== Editar el contenido del slider desde el menú =====
    @FXML
    private void handleEditarSlider(ActionEvent event) {
        if (usuarioActual == null || (usuarioActual.getRol() != Rol.ADMIN && usuarioActual.getRol() != Rol.SUPER_ADMIN)) return;
        if (sliderController == null) return;

        // 1) Texto actual: intenta desde BD, si no, desde lo que hay en pantalla
        String actual = null;
        try { actual = org.example.int_biblioteca.dao.CmsDAO.get(CMS_SLIDES); } catch (Exception ignored) {}
        if (actual == null || actual.isBlank()) {
            actual = String.join("\n---\n", sliderController.getSlidesText());
        }

        // 2) Diálogo
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar slider");
        dialog.setHeaderText("Cada diapositiva sepárala con una línea que contenga solo ---");
        TextArea area = new TextArea(actual);
        area.setPrefColumnCount(60);
        area.setPrefRowCount(14);
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;

            String texto = area.getText();
            // Split por línea con solo --- (con o sin espacios alrededor)
            String[] partes = texto.split("(?m)^\\s*---\\s*$");

            java.util.List<String> nuevas = new java.util.ArrayList<>();
            for (String p : partes) {
                String s = p.strip();
                if (!s.isEmpty()) nuevas.add(s);
            }
            if (nuevas.isEmpty()) return;

            try {
                // Guarda en BD como un solo CLOB
                String aGuardar = String.join("\n---\n", nuevas);
                org.example.int_biblioteca.dao.CmsDAO.set(CMS_SLIDES, aGuardar);

                // Actualiza el slider en vivo
                sliderController.setSlidesText(nuevas);
                showAlert(Alert.AlertType.INFORMATION, "Slider", "Diapositivas guardadas.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Slider", "No se pudo guardar:\n" + e.getMessage());
            }
        });
    }


    @FXML
    private void handleEditarLibros(ActionEvent event) {
        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMIN) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("crud-libros.fxml"));
            Parent crudView = loader.load();
            rootPane.setCenter(crudView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la vista de edición de libros.");
        }
    }

    @FXML
    private void abrirUsuarios(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("crud-usuarios.fxml"));
            Parent vista = loader.load();
            CrudUsuariosController ctrl = loader.getController();
            ctrl.setRolActual(usuarioActual != null ? usuarioActual.getRol() : Rol.USUARIO); // <- aquí se recarga UNA vez
            rootPane.setCenter(vista);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Usuarios", "No se pudo cargar Usuarios:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setVis(Button b, boolean visible) {
        if (b == null) return;
        b.setVisible(visible);
        b.setManaged(visible); // que no deje hueco
    }

    private void aplicarPermisosPorRol() {
        if (usuarioActual == null) {
            setVis(botonUsuarios, false);
            setVis(editarInfoBtn, false);
            setVis(editarSliderBtn, false);
            setVis(editarLibrosBtn, false);
            return;
        }
        Rol r = usuarioActual.getRol();

        boolean verUsuarios     = (r == Rol.SUPER_ADMIN) || (r == Rol.ADMIN) || (r == Rol.BIBLIOTECARIO);
        boolean verEditarLibros = (r == Rol.ADMIN);
        boolean verEditarInfo   = (r == Rol.ADMIN);
        boolean verEditarSlider = (r == Rol.ADMIN);

        // Si quieres que SUPER_ADMIN vea SOLO "Usuarios" (además de lo default)
        if (r == Rol.SUPER_ADMIN) {
            verEditarLibros = false;
            verEditarInfo   = false;
            verEditarSlider = false;
        }
        setVis(botonUsuarios, verUsuarios);
        setVis(editarLibrosBtn, verEditarLibros);
        setVis(editarInfoBtn,   verEditarInfo);
        setVis(editarSliderBtn, verEditarSlider);
    }

    // Utilidad para mostrar alertas
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private HBox barraBusquedaSuperior;

    private Parent catalogoRoot;
    private CatalogoLibrosController catalogoController;

    private void ocultarBarraSuperior(boolean ocultar) {
        if (barraBusquedaSuperior != null) {
            barraBusquedaSuperior.setVisible(!ocultar);
            barraBusquedaSuperior.setManaged(!ocultar); // que no deje hueco
        }
    }

    private void restaurarBarraSuperior() { ocultarBarraSuperior(false); }

    private void abrirCatalogo(String query) {
        try {
            if (catalogoRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("catalogo-libros.fxml"));
                catalogoRoot = loader.load();
                catalogoController = loader.getController();
            }

            // se  asegúra de pasar SIEMPRE el usuario antes de mostrar
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

    @FXML
    private void abrirMisPrestamos(javafx.event.ActionEvent event) {
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
    private void handleCerrarSesion(javafx.event.ActionEvent event) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Cerrar sesión?", ButtonType.OK, ButtonType.CANCEL);
        conf.setHeaderText(null);
        conf.setTitle("Confirmación");
        if (conf.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            // 1) Limpiar estado de sesión y carrito
            this.usuarioActual = null;
            try { org.example.int_biblioteca.CarritoService.vaciar(); } catch (Exception ignored) {}

            // 2) Volver a la pantalla de login
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("hello-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Cerrar sesión", "No se pudo regresar al inicio de sesión:\n" + e.getMessage());
        }
    }



}