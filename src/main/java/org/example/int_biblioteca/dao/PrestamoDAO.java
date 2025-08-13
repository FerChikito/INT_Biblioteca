package org.example.int_biblioteca.dao;

import org.example.int_biblioteca.Database;
import org.example.int_biblioteca.PrestamoListado;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PrestamoDAO {
    private PrestamoDAO(){}

    public static final int MAX_ACTIVOS = 5;

    private static final String ESTADO_A = "A";
    private static final String ESTADO_D = "D";
    private static final String ESTADO_M = "M";

    // ===== Parámetros =====
    public static int diasLimiteDefault() {
        try { return ConfigDAO.getDiasLimite(); }
        catch (Exception e) { return 3; }
    }

    private static BigDecimal leerTarifaGlobal() {
        try { return BigDecimal.valueOf(ConfigDAO.getTarifaMulta()); }
        catch (Exception e) { return new BigDecimal("50"); }
    }

    // ===== Consultas de control =====
    public static int contarActivos(String correo) throws SQLException {
        String sql = """
            SELECT COUNT(*)
              FROM PRESTAMOS
             WHERE CORREO_USUARIO = ?
               AND ESTADO IN ('A','M')
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

    public static boolean tieneVencidos(String correo) throws SQLException {
        String sql = """
            SELECT 1
              FROM PRESTAMOS
             WHERE CORREO_USUARIO = ?
               AND ESTADO IN ('A','M')
               AND FECHA_DEVOL IS NULL
               AND TRUNC(FECHA_LIMITE) < TRUNC(SYSDATE)
             FETCH FIRST 1 ROWS ONLY
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ===== Listados =====
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

    public static List<PrestamoListado> listarActivos() throws SQLException {
        String sql = """
            SELECT p.ID_PRESTAMO, p.CORREO_USUARIO, p.ESTADO, p.FECHA_PREST, p.FECHA_DEVOL,
                   p.FOLIO, p.FECHA_LIMITE,
                   l.TITULO, l.ISBN
              FROM PRESTAMOS p
              JOIN LIBROS l ON l.ID_LIBRO = p.ID_LIBRO
             WHERE p.ESTADO IN ('A','M')
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

    // ===== Operaciones =====
    public static boolean marcarDevuelto(int idPrestamo) throws SQLException {
        String sql = "UPDATE PRESTAMOS SET ESTADO=?, FECHA_DEVOL=SYSDATE WHERE ID_PRESTAMO=? AND FECHA_DEVOL IS NULL";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ESTADO_D);
            ps.setInt(2, idPrestamo);
            return ps.executeUpdate() == 1;
        }
    }

    public static int crearPrestamos(String correoUsuario, List<String> isbns) throws SQLException {
        if (isbns == null || isbns.isEmpty()) return 0;
        final int dias = diasLimiteDefault();

        String sql = """
            INSERT INTO PRESTAMOS (ID_LIBRO, CORREO_USUARIO, ESTADO, FOLIO, FECHA_PREST, FECHA_LIMITE, DIAS_LIMITE_USADOS)
            SELECT l.ID_LIBRO, ?, 'A', ?, SYSDATE, TRUNC(SYSDATE) + ?, ?
              FROM LIBROS l
             WHERE l.ISBN = ?
               AND NOT EXISTS (
                   SELECT 1 FROM PRESTAMOS p
                    WHERE p.ID_LIBRO = l.ID_LIBRO
                      AND p.ESTADO IN ('A','M')
                      AND p.FECHA_DEVOL IS NULL
               )
        """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int total = 0;
            for (String isbn : isbns) {
                ps.setString(1, correoUsuario);
                ps.setString(2, generarFolio());
                ps.setInt(3, dias);
                ps.setInt(4, dias);
                ps.setString(5, isbn);
                total += ps.executeUpdate();
            }
            return total;
        }
    }

    /** Cambia la fecha límite de un préstamo (Admin/Biblio). */
    public static boolean actualizarFechaLimite(int idPrestamo, LocalDate nuevaFecha) throws SQLException {
        String sql = "UPDATE PRESTAMOS SET FECHA_LIMITE=? WHERE ID_PRESTAMO=? AND FECHA_DEVOL IS NULL";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, nuevaFecha == null ? null : Date.valueOf(nuevaFecha));
            ps.setInt(2, idPrestamo);
            return ps.executeUpdate() == 1;
        }
    }

    /** Si ya venció, pone estado 'M' y guarda TARIFA_MULTA_DIA/MULTA_TOTAL. */
    public static boolean aplicarMultaSiCorresponde(int idPrestamo) throws SQLException {
        String sel = """
          SELECT FECHA_LIMITE
            FROM PRESTAMOS
           WHERE ID_PRESTAMO=? AND ESTADO IN ('A','M') AND FECHA_DEVOL IS NULL
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sel)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                LocalDate limite = rs.getDate(1).toLocalDate();
                long diasAtraso = java.time.temporal.ChronoUnit.DAYS.between(limite, LocalDate.now());
                if (diasAtraso <= 0) return false;

                // Por ahora usamos la global. Si más adelante guardas CORREO_BIBLIO en PRESTAMOS,
                // puedes leer tarifa por bibliotecario con MultaTarifaDAO.
                BigDecimal tarifa = leerTarifaGlobal();
                BigDecimal total  = tarifa.multiply(new BigDecimal(diasAtraso));

                try (PreparedStatement up = c.prepareStatement("""
                   UPDATE PRESTAMOS
                      SET ESTADO='M', TARIFA_MULTA_DIA=?, MULTA_TOTAL=?
                    WHERE ID_PRESTAMO=?
                """)) {
                    up.setBigDecimal(1, tarifa);
                    up.setBigDecimal(2, total);
                    up.setInt(3, idPrestamo);
                    return up.executeUpdate() == 1;
                }
            }
        }
    }

    // ===== Mapeo =====
    private static PrestamoListado mapListado(ResultSet rs) throws SQLException {
        LocalDateTime fp = rs.getTimestamp("FECHA_PREST") == null ? null : rs.getTimestamp("FECHA_PREST").toLocalDateTime();
        LocalDateTime fd = rs.getTimestamp("FECHA_DEVOL") == null ? null : rs.getTimestamp("FECHA_DEVOL").toLocalDateTime();
        LocalDate limite = rs.getDate("FECHA_LIMITE") == null ? null : rs.getDate("FECHA_LIMITE").toLocalDate();

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

    private static String generarFolio() {
        String base = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
        int rnd = (int)(Math.random()*9000)+1000;
        return base + "-" + rnd;
    }
}
