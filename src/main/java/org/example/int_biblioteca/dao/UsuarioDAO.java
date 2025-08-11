package org.example.int_biblioteca.dao;

import org.example.int_biblioteca.Database;
import org.example.int_biblioteca.Usuario;
import org.example.int_biblioteca.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UsuarioDAO {
    private UsuarioDAO() {}

    private static Usuario map(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getString("NOMBRE"),
                rs.getString("CORREO"),
                rs.getString("NUMERO_TELEFONICO"),
                rs.getString("DIRECCION"),
                rs.getString("CONTRASENIA"),
                Rol.fromDb(rs.getString("ROL"))
        );
    }

    public static Optional<Usuario> autenticar(String correo, String contrasenia) throws SQLException {
        String sql = """
            SELECT NOMBRE, CORREO, NUMERO_TELEFONICO, DIRECCION, CONTRASENIA, ROL
            FROM USUARIOS
            WHERE CORREO = ? AND CONTRASENIA = ?
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, contrasenia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public static List<Usuario> listar() throws SQLException {
        String sql = """
            SELECT NOMBRE, CORREO, NUMERO_TELEFONICO, DIRECCION, CONTRASENIA, ROL
            FROM USUARIOS
            ORDER BY NOMBRE
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Usuario> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public static boolean insertar(Usuario u) throws SQLException {
        String sql = """
            INSERT INTO USUARIOS (NOMBRE, CORREO, NUMERO_TELEFONICO, DIRECCION, CONTRASENIA, ROL)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getCorreo());
            ps.setString(3, u.getNumeroTelefonico());
            ps.setString(4, u.getDireccion());
            ps.setString(5, u.getContrasenia());
            ps.setString(6, u.getRol().name());
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean actualizarPorCorreo(String correoOriginal, Usuario u) throws SQLException {
        String sql = """
            UPDATE USUARIOS
               SET NOMBRE=?, CORREO=?, NUMERO_TELEFONICO=?, DIRECCION=?, CONTRASENIA=?, ROL=?
             WHERE CORREO=?
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getCorreo());
            ps.setString(3, u.getNumeroTelefonico());
            ps.setString(4, u.getDireccion());
            ps.setString(5, u.getContrasenia());
            ps.setString(6, u.getRol().name());
            ps.setString(7, correoOriginal);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean eliminarPorCorreo(String correo) throws SQLException {
        String sql = "DELETE FROM USUARIOS WHERE CORREO=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo);
            return ps.executeUpdate() == 1;
        }
    }
}
