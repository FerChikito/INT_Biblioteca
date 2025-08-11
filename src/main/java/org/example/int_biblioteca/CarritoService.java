package org.example.int_biblioteca;

import javafx.beans.property.*;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CarritoService {

    private static final Map<String, Libro> porIsbn = new LinkedHashMap<>();
    private static final ObservableList<Libro> items = FXCollections.observableArrayList();
    private static final ReadOnlyIntegerWrapper total = new ReadOnlyIntegerWrapper(0);

    private CarritoService() {}

    /** Agrega un libro si su ISBN no está en el carrito. */
    public static synchronized boolean agregar(Libro libro) {
        if (libro == null) return false;
        String isbn = safe(libro.getIsbn());
        if (isbn.isEmpty() || porIsbn.containsKey(isbn)) return false;

        porIsbn.put(isbn, libro);
        items.add(libro);
        total.set(porIsbn.size());
        return true;
    }

    /** Elimina por ISBN. */
    public static synchronized boolean eliminarPorIsbn(String isbn) {
        String key = safe(isbn);
        if (!porIsbn.containsKey(key)) return false;
        Libro prev = porIsbn.remove(key);
        if (prev != null) {
            items.removeIf(l -> safe(l.getIsbn()).equals(key));
            total.set(porIsbn.size());
            return true;
        }
        return false;
    }

    /** Vacía el carrito. */
    public static synchronized void vaciar() {
        porIsbn.clear();
        items.clear();
        total.set(0);
    }

    /** ¿El carrito ya contiene este ISBN? */
    public static synchronized boolean contiene(String isbn) {
        return porIsbn.containsKey(safe(isbn));
    }

    /** Lista observable (solo lectura). Actualiza sola la TableView. */
    public static ObservableList<Libro> getItems() {
        return FXCollections.unmodifiableObservableList(items);
    }

    /** Total de libros (primitivo). */
    public static int total() { return total.get(); }

    /** Propiedad para bindings (labels/tooltip). */
    public static ReadOnlyIntegerProperty totalProperty() {
        return total.getReadOnlyProperty();
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    public static synchronized java.util.List<String> getIsbns() {
        return new java.util.ArrayList<>(porIsbn.keySet());
    }

}
