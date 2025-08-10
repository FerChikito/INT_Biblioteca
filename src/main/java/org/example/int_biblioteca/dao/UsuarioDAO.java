package org.example.int_biblioteca.dao;

import org.example.int_biblioteca.Database;
import org.example.int_biblioteca.Usuario;
import org.example.int_biblioteca.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class UsuarioDAO {
    private UsuarioDAO() { }

    /** Inserta usando NOMBRE (único), CONTRASENIA y ROL */
    public static boolean insertar(Usuario u) throws SQLException {
        final String sql = "INSERT INTO USUARIOS (NOMBRE, CONTRASENIA, ROL) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getContrasenia());   // <-- OJO: contrasenia
            ps.setString(3, u.getRol().name());    // guarda el enum como texto
            return ps.executeUpdate() == 1;
        }
    }

    /** Actualiza por nombre (clave lógica) */
    public static boolean actualizar(String nombreOriginal, Usuario nuevo) throws SQLException {
        final String sql = "UPDATE USUARIOS SET NOMBRE = ?, CONTRASENIA = ?, ROL = ? WHERE NOMBRE = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nuevo.getNombre());
            ps.setString(2, nuevo.getContrasenia());
            ps.setString(3, nuevo.getRol().name());
            ps.setString(4, nombreOriginal);
            return ps.executeUpdate() == 1;
        }
    }

    /** Elimina por nombre */
    public static boolean eliminar(String nombre) throws SQLException {
        final String sql = "DELETE FROM USUARIOS WHERE NOMBRE = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            return ps.executeUpdate() == 1;
        }
    }

    /** Obtiene un usuario por nombre */
    public static Usuario obtener(String nombre) throws SQLException {
        final String sql = "SELECT NOMBRE, CONTRASENIA, ROL FROM USUARIOS WHERE NOMBRE = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String nom = rs.getString("NOMBRE");
                String contrasenia = rs.getString("CONTRASENIA");
                Rol rol = Rol.valueOf(rs.getString("ROL"));
                // Constructor esperado: Usuario(String nombre, String contrasenia, Rol rol)
                return new Usuario(nom, contrasenia, rol);
            }
        }
    }

    /** Lista completa (ordenada por nombre) */
    public static List<Usuario> listar() throws SQLException {
        final String sql = "SELECT NOMBRE, CONTRASENIA, ROL FROM USUARIOS ORDER BY NOMBRE";
        List<Usuario> out = new ArrayList<Usuario>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nom = rs.getString("NOMBRE");
                String contrasenia = rs.getString("CONTRASENIA");
                Rol rol = Rol.valueOf(rs.getString("ROL"));
                out.add(new Usuario(nom, contrasenia, rol));
            }
        }
        return out;
    }
}
