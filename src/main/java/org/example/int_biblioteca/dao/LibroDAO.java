package org.example.int_biblioteca.dao;

import org.example.int_biblioteca.Libro;
import org.example.int_biblioteca.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class LibroDAO {
    private LibroDAO() {}

    public static boolean insertar(Libro l) throws SQLException {
        final String sql = "INSERT INTO LIBROS (ISBN, TITULO, AUTOR) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, l.getIsbn());
            ps.setString(2, l.getTitulo());
            ps.setString(3, l.getAutor());
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean actualizarPorIsbn(String isbnOriginal, Libro nuevo) throws SQLException {
        final String sql = "UPDATE LIBROS SET ISBN = ?, TITULO = ?, AUTOR = ? WHERE ISBN = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nuevo.getIsbn());
            ps.setString(2, nuevo.getTitulo());
            ps.setString(3, nuevo.getAutor());
            ps.setString(4, isbnOriginal);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean eliminarPorIsbn(String isbn) throws SQLException {
        final String sql = "DELETE FROM LIBROS WHERE ISBN = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);
            return ps.executeUpdate() == 1;
        }
    }

    public static List<Libro> listar() throws SQLException {
        final String sql = "SELECT ID_LIBRO, ISBN, TITULO, AUTOR FROM LIBROS ORDER BY ID_LIBRO DESC";
        List<Libro> out = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Libro(
                        rs.getString("TITULO"),
                        rs.getString("AUTOR"),
                        rs.getString("ISBN")
                ));
            }
        }
        return out;
    }

    public static List<Libro> buscar(String termino) throws SQLException {
        final String sql = """
        SELECT ID_LIBRO, ISBN, TITULO, AUTOR
          FROM LIBROS
         WHERE UPPER(TITULO) LIKE ?
            OR UPPER(AUTOR)  LIKE ?
            OR UPPER(ISBN)   LIKE ?
         ORDER BY TITULO
        """;
        String patron = "%" + termino.trim().toUpperCase() + "%";

        List<Libro> resultados = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultados.add(new Libro(
                            rs.getString("TITULO"),
                            rs.getString("AUTOR"),
                            rs.getString("ISBN")
                    ));
                }
            }
        }
        return resultados;
    }

}
