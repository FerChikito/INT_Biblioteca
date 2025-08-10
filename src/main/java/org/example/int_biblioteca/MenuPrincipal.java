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

    // Datos simulados de libros
    private List<Libro> libros = new ArrayList<>(List.of(
            new Libro("Cien años de soledad", "Gabriel García Márquez", "978-3-16-148410-0"),
            new Libro("1984", "George Orwell", "978-0-452-28423-4"),
            new Libro("El principito", "Antoine de Saint-Exupéry", "978-84-206-5352-9"),
            new Libro("Harry Potter y la piedra filosofal", "J.K. Rowling", "978-0-7475-3269-9"),
            new Libro("Fahrenheit 451", "Ray Bradbury", "978-0-7432-4722-1")
    ));

    // Setter para recibir el usuario desde el login
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        mostrarBotonesDeEdicion(); // Aquí ya tenemos el usuario y podemos mostrar los botones
    }

    // Método que se ejecuta al hacer clic en el botón de menú
    @FXML
    private void handleBotonMenu(javafx.event.ActionEvent event) {
        // Evita NPE y no dupliques el centro si ya está puesto
        if (originalCenter != null && rootPane.getCenter() != originalCenter) {
            rootPane.setCenter(originalCenter);
        }
    }

    // Método que se ejecuta al hacer clic en el botón de perfil
    @FXML
    private void handleBotonPerfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("perfil.fxml"));

            // 1) Carga la vista
            javafx.scene.Parent vista = loader.load();

            // 2) Tipa el controller correctamente
            org.example.int_biblioteca.PerfilController ctrl = loader.getController();

            // 3) Pasa el usuario y el callback de regresar
            ctrl.setUsuarioActual(usuarioActual);
            ctrl.setOnMostrarVista(v -> rootPane.setCenter(v));
            ctrl.setOnRegresar(() -> {
                if (originalCenter != null && rootPane.getCenter() != originalCenter) {
                    rootPane.setCenter(originalCenter);
                }
            });

            // 4) Muestra la vista en el centro
            rootPane.setCenter(vista);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Perfil",
                    "No se pudo abrir la vista de perfil:\n" + e.getMessage());
            e.printStackTrace();
        }
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
        String termino = buscarField.getText() == null ? "" : buscarField.getText().trim();
        infoP.getChildren().clear();

        if (termino.isEmpty()) {
            cargarTexto();
            return;
        }

        try {
            List<Libro> resultados = org.example.int_biblioteca.dao.LibroDAO.buscar(termino);

            if (resultados.isEmpty()) {
                Label noResultado = new Label("No se encontraron libros.");
                noResultado.setStyle("-fx-font-style: italic;");
                infoP.getChildren().add(noResultado);
            } else {
                for (Libro libro : resultados) {
                    String texto = libro.getTitulo() + " | " + libro.getAutor() + " | ISBN: " + libro.getIsbn();
                    Label etiqueta = new Label("• " + texto);
                    etiqueta.setStyle("-fx-font-size: 14px;");
                    infoP.getChildren().add(etiqueta);
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Base de datos", "No se pudo realizar la búsqueda:\n" + e.getMessage());
            cargarTexto(); // fallback
        }
    }


    // Muestra botón de edición solo si es admin
    private void mostrarBotonesDeEdicion() {
        if (usuarioActual == null) {
            // ocultar todo lo “especial” si no hay sesión
            if (botonUsuarios   != null) botonUsuarios.setVisible(false);
            if (editarInfoBtn   != null) editarInfoBtn.setVisible(false);
            if (editarSliderBtn != null) editarSliderBtn.setVisible(false);
            if (editarLibrosBtn != null) editarLibrosBtn.setVisible(false);
            return;
        }

        Rol rol = usuarioActual.getRol();
        boolean esSuperAdmin   = rol == Rol.SUPER_ADMIN;
        boolean esAdmin        = rol == Rol.ADMIN;
        boolean esBibliotecario= rol == Rol.BIBLIOTECARIO;

        // Reglas del cliente:
        // SUPER_ADMIN: todo
        // ADMIN: CRUD de bibliotecarios y libros (aquí: edición de libros/info/slider)
        // BIBLIOTECARIO: CRUD de usuarios (aquí: botón Usuarios)
        // USUARIO: sin botones extra

        if (botonUsuarios   != null) botonUsuarios.setVisible(esBibliotecario || esAdmin || esSuperAdmin);
        if (editarLibrosBtn != null) editarLibrosBtn.setVisible(esAdmin || esSuperAdmin);
        if (editarInfoBtn   != null) editarInfoBtn.setVisible(esAdmin || esSuperAdmin);
        if (editarSliderBtn != null) editarSliderBtn.setVisible(esAdmin || esSuperAdmin);
    }

    private void showPane(Node pane) {
        for (Node child : centerStack.getChildren()) {
            child.setVisible(child == pane);
        }
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

    }