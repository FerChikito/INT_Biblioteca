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
        cargarTexto(); // Llama a cargarTexto aquí// Luego guarda el contenido original del centro
        originalCenter = rootPane.getCenter();
        // Que no ocupen espacio cuando estén ocultos:
        if (botonUsuarios   != null) botonUsuarios.managedProperty().bind(botonUsuarios.visibleProperty());
        if (editarInfoBtn   != null) editarInfoBtn.managedProperty().bind(editarInfoBtn.visibleProperty());
        if (editarSliderBtn != null) editarSliderBtn.managedProperty().bind(editarSliderBtn.visibleProperty());
        if (editarLibrosBtn != null) editarLibrosBtn.managedProperty().bind(editarLibrosBtn.visibleProperty());

        mostrarBotonesDeEdicion();
    }

    private void cargarSlider() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("slider.fxml"));
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

    // Buscar libros por título, autor o ISBN
    @FXML
    private void buscarLibro() {
        String query = buscarField.getText().toLowerCase().trim();
        infoP.getChildren().clear();

        if (query.isEmpty()) {
            cargarTexto();
            return;
        }


        List<Libro> resultados = libros.stream()
                .filter(libro -> libro.getTitulo().toLowerCase().contains(query)
                        || libro.getAutor().toLowerCase().contains(query)
                        || libro.getIsbn().toLowerCase().contains(query))
                .toList();

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

    @FXML
    private void handleEditarSlider(ActionEvent event) {
        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMIN) return;

        // Antes de editar slider, vuelve al centro original
        rootPane.setCenter(originalCenter);

        // Ahora sí toca tu lógica de edición dentro del sliderContainer
        sliderContainer.getChildren().clear();
        TextField nuevoTexto = new TextField("Texto del slider…");
        Button guardar = new Button("Guardar");
        guardar.setOnAction(e -> {
            sliderContainer.getChildren().setAll(new Label(nuevoTexto.getText()));
        });
        sliderContainer.getChildren().setAll(nuevoTexto, guardar);
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
    private void abrirUsuarios(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("crud-usuarios.fxml"));
            Parent vista = loader.load();
            rootPane.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la vista de usuarios.");
        }
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