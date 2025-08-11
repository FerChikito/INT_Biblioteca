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
        cargarTexto(); // Llama a cargarTexto aquí// Luego guarda el contenido original del centro
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

            // 4) Colocamos el nodo raíz del slider dentro de nuestro contenedor en el menú.
            //    Usamos setAll para reemplazar cualquier contenido previo.
            sliderContainer.getChildren().setAll(slider);

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

    // Buscar libros por título, autor o ISBN
    @FXML
    private void buscarLibro() {
        String query = buscarField.getText().trim();
        infoP.getChildren().clear();

        if (query.isEmpty()) { cargarTexto(); return; }

        try {
            var resultados = LibroDAO.buscar(query);
            if (resultados.isEmpty()) {
                Label noResultado = new Label("No se encontraron libros.");
                noResultado.setStyle("-fx-font-style: italic;");
                infoP.getChildren().add(noResultado);
            } else {
                for (Libro libro : resultados) {
                    String texto = libro.getTitulo() + " | " + libro.getAutor() + " | ISBN: " + libro.getIsbn();
                    Label label = new Label("• " + texto);
                    label.setStyle("-fx-font-size: 14px;");
                    infoP.getChildren().add(label);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Búsqueda", "No se pudo buscar en la base de datos:\n" + e.getMessage());
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
        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMIN) return;

        rootPane.setCenter(originalCenter);
        // luego tu lógica de edición en infoP…
        infoP.getChildren().clear();
        TextArea editor = new TextArea("…");
        Button guardar = new Button("Guardar");
        guardar.setOnAction(e -> {
            infoP.getChildren().setAll(new Label(editor.getText()));
        });
        infoP.getChildren().setAll(editor, guardar);
    }

    // ===== Editar el contenido del slider desde el menú =====
    @FXML
    private void handleEditarSlider(ActionEvent event) {
        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMIN) return;
        if (sliderController == null) return; // por si no cargó aún

        // 1) Preparamos el texto actual, separando cada slide con --- en su propia línea
        String inicial = String.join("\n---\n", sliderController.getSlidesText());

        // 2) Creamos un diálogo con TextArea multi-línea
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar slider");
        dialog.setHeaderText("Cada diapositiva sepárala con una línea que contenga solo ---");
        TextArea area = new TextArea(inicial);
        area.setPrefColumnCount(60);
        area.setPrefRowCount(14);
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 3) Procesamos al aceptar: partimos por líneas con solo '---'
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;

            String texto = area.getText();
            // Split por una línea que tenga solo --- (con o sin espacios alrededor)
            String[] partes = texto.split("(?m)^\\s*---\\s*$");

            // Normalizamos: trim a cada parte, descartamos vacías (pero mantenemos saltos internos)
            java.util.List<String> nuevas = new java.util.ArrayList<>();
            for (String p : partes) {
                String s = p.strip();               // quita espacios/linebreaks al inicio/fin
                if (!s.isEmpty()) nuevas.add(s);    // conserva líneas internas como texto del slide
            }

            // Si quedó vacío, no pisamos el slider (evita que se “borre” accidentalmente)
            if (nuevas.isEmpty()) return;

            // 4) Actualizamos el slider SIN reemplazar su nodo (botones y lógica quedan vivos)
            sliderController.setSlidesText(nuevas);
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



}