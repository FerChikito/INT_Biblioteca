module org.example.int_biblioteca {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens org.example.int_biblioteca to javafx.fxml;
    exports org.example.int_biblioteca;
}