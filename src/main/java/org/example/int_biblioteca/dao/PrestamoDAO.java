package org.example.int_biblioteca.dao;

import org.example.int_biblioteca.Database;
import org.example.int_biblioteca.PrestamoListado;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PrestamoDAO {
    private PrestamoDAO(){}

    // Reglas de negocio
    public static final int MAX_ACTIVOS = 5;   // máx. préstamos activos por usuario
    public static final int DIAS_LIMITE = 3;   // días para la fecha límite

    // Constantes de estado/columnas
    private static final String ESTADO_ACTIVO   = "A";
    private static final String ESTADO_DEVUELTO = "D";
    private static final String COL_USUARIO     = "CORREO_USUARIO";

    // === contar préstamos activos del usuario
    public static int contarActivos(String correo) throws SQLException {
        String sql = """
            SELECT COUNT(*)
              FROM PRESTAMOS
             WHERE CORREO_USUARIO = ?
               AND ESTADO = 'A'
               AND FECHA_DEVOL IS NULL
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // === tiene préstamos vencidos (fecha_límite < hoy y no devueltos)
    public static boolean tieneVencidos(String correo) throws SQLException {
        String sql = """
            SELECT 1
              FROM PRESTAMOS
             WHERE CORREO_USUARIO = ?
               AND ESTADO = 'A'
               AND FECHA_DEVOL IS NULL
               AND TRUNC(FECHA_LIMITE) < TRUNC(SYSDATE)
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // === listar préstamos del usuario
    public static List<PrestamoListado> listarPorUsuario(String correo) throws SQLException {
        String sql = """
            SELECT p.ID_PRESTAMO, p.CORREO_USUARIO, p.ESTADO, p.FECHA_PREST, p.FECHA_DEVOL,
                   p.FOLIO, p.FECHA_LIMITE,
                   l.TITULO, l.ISBN
              FROM PRESTAMOS p
              JOIN LIBROS l ON l.ID_LIBRO = p.ID_LIBRO
             WHERE p.CORREO_USUARIO = ?
             ORDER BY p.ID_PRESTAMO DESC
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                List<PrestamoListado> out = new ArrayList<>();
                while (rs.next()) out.add(mapListado(rs));
                return out;
            }
        }
    }

    // === listar préstamos activos (para gestión)
    public static List<PrestamoListado> listarActivos() throws SQLException {
        String sql = """
            SELECT p.ID_PRESTAMO, p.CORREO_USUARIO, p.ESTADO, p.FECHA_PREST, p.FECHA_DEVOL,
                   p.FOLIO, p.FECHA_LIMITE,
                   l.TITULO, l.ISBN
              FROM PRESTAMOS p
              JOIN LIBROS l ON l.ID_LIBRO = p.ID_LIBRO
             WHERE p.ESTADO = 'A'
               AND p.FECHA_DEVOL IS NULL
             ORDER BY p.ID_PRESTAMO DESC
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PrestamoListado> out = new ArrayList<>();
            while (rs.next()) out.add(mapListado(rs));
            return out;
        }
    }

    // === crear préstamos (con folio y fecha_límite)
    public static int crearPrestamos(String correo, List<String> isbns) throws SQLException {
        if (isbns == null || isbns.isEmpty()) return 0;

        String sql = """
    INSERT INTO PRESTAMOS (ID_LIBRO, CORREO_USUARIO, ESTADO, FOLIO, FECHA_LIMITE)
    SELECT l.ID_LIBRO, ?, 'A', ?, ?
      FROM LIBROS l
     WHERE l.ISBN = ?
       AND NOT EXISTS (
           SELECT 1 FROM PRESTAMOS p
            WHERE p.ID_LIBRO = l.ID_LIBRO
              AND p.ESTADO = 'A'
              AND p.FECHA_DEVOL IS NULL
       )
""";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int total = 0;
            for (String isbn : isbns) {
                String folio = generarFolio();
                java.sql.Date fechaLimite = java.sql.Date.valueOf(
                        calcularFechaLimite(java.time.LocalDate.now(), DIAS_LIMITE)
                );

                ps.setString(1, correo);
                ps.setString(2, folio);
                ps.setDate(3, fechaLimite);  // << aquí va la fecha “inteligente”
                ps.setString(4, isbn);
                total += ps.executeUpdate();
            }
            return total;
        }

    }

    private static java.time.LocalDate calcularFechaLimite(java.time.LocalDate hoy, int dias) {
        java.time.LocalDate f = hoy.plusDays(dias);
        switch (f.getDayOfWeek()) {
            case SATURDAY: return f.plusDays(2);
            case SUNDAY:   return f.plusDays(1);
            default:       return f;
        }
    }

    // === marcar como devuelto
    public static boolean marcarDevuelto(int idPrestamo) throws SQLException {
        String sql = "UPDATE PRESTAMOS SET ESTADO=?, FECHA_DEVOL=SYSDATE WHERE ID_PRESTAMO=? AND ESTADO=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ESTADO_DEVUELTO);
            ps.setInt(2, idPrestamo);
            ps.setString(3, ESTADO_ACTIVO);
            return ps.executeUpdate() == 1;
        }
    }

    // === mapeo ResultSet -> PrestamoListado
    private static PrestamoListado mapListado(ResultSet rs) throws SQLException {
        LocalDateTime fp = null, fd = null;
        Timestamp t1 = rs.getTimestamp("FECHA_PREST");
        Timestamp t2 = rs.getTimestamp("FECHA_DEVOL");
        if (t1 != null) fp = t1.toLocalDateTime();
        if (t2 != null) fd = t2.toLocalDateTime();

        java.sql.Date dl = rs.getDate("FECHA_LIMITE");
        LocalDate limite = (dl == null) ? null : dl.toLocalDate();

        return new PrestamoListado(
                rs.getInt("ID_PRESTAMO"),
                rs.getString("CORREO_USUARIO"),
                rs.getString("TITULO"),
                rs.getString("ISBN"),
                rs.getString("ESTADO"),
                fp,
                fd,
                rs.getString("FOLIO"),
                limite
        );
    }

    // === generador simple de folio
    private static String generarFolio() {
        String base = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
        int rnd = (int)(Math.random() * 9000) + 1000;
        return base + "-" + rnd;
    }
}
