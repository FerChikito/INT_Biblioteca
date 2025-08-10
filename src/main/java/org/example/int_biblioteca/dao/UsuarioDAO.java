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
    public static boolean insertar(Usuario usuario) throws SQLException {
        final String sql = "INSERT INTO USUARIOS (NOMBRE, CONTRASENIA, ROL) VALUES (?, ?, ?)";
        try (Connection conexion = Database.getConnection();
             PreparedStatement prepareStatement = conexion .prepareStatement(sql)) {
            prepareStatement.setString(1, usuario.getNombre());
            prepareStatement.setString(2, usuario.getContrasenia());   // <-- OJO: contrasenia
            prepareStatement.setString(3, usuario.getRol().name());    // guarda el enum como texto
            return prepareStatement.executeUpdate() == 1;
        }
    }

    /** Actualiza por nombre (clave lógica) */
    public static boolean actualizar(String nombreOriginal, Usuario nuevoUsuario) throws SQLException {
        final String sql = "UPDATE USUARIOS SET NOMBRE = ?, CONTRASENIA = ?, ROL = ? WHERE NOMBRE = ?";
        try (Connection conexion  = Database.getConnection();
             PreparedStatement prepareStatement = conexion .prepareStatement(sql)) {
            prepareStatement.setString(1, nuevoUsuario.getNombre());
            prepareStatement.setString(2, nuevoUsuario.getContrasenia());
            prepareStatement.setString(3, nuevoUsuario.getRol().name());
            prepareStatement.setString(4, nombreOriginal);
            return prepareStatement.executeUpdate() == 1;
        }
    }

    /** Elimina por nombre */
    public static boolean eliminar(String nombreUsuario) throws SQLException {
        final String sql = "DELETE FROM USUARIOS WHERE NOMBRE = ?";
        try (Connection conexion  = Database.getConnection();
             PreparedStatement prepareStatement = conexion .prepareStatement(sql)) {
            prepareStatement.setString(1, nombreUsuario);
            return prepareStatement.executeUpdate() == 1;
        }
    }

    /** Obtiene un usuario por nombre */
    public static Usuario obtener(String nombreUsuario) throws SQLException {
        final String sql = "SELECT NOMBRE, CONTRASENIA, ROL FROM USUARIOS WHERE NOMBRE = ?";
        try (Connection conexion = Database.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombreUsuario);
            try (ResultSet rs = sentencia.executeQuery()) {
                if (!rs.next()) return null;
                return new Usuario(
                        rs.getString("NOMBRE"),
                        rs.getString("CONTRASENIA"),
                        Rol.valueOf(rs.getString("ROL"))
                );
            }
        }
    }

    /** Lista completa (ordenada por nombre) */
    public static List<Usuario> listar() throws SQLException {
        final String sql = "SELECT NOMBRE, CONTRASENIA, ROL FROM USUARIOS ORDER BY NOMBRE";
        List<Usuario> usuarios  = new ArrayList<Usuario>();
        try (Connection conexion  = Database.getConnection();
             PreparedStatement sentencia  = conexion .prepareStatement(sql);
             ResultSet rs = sentencia .executeQuery()) {
            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getString("NOMBRE"),
                        rs.getString("CONTRASENIA"),
                        Rol.valueOf(rs.getString("ROL"))
                ));
            }
        }
        return usuarios;
    }
}
