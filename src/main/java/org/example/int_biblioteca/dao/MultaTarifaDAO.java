package org.example.int_biblioteca.dao;

import org.example.int_biblioteca.Database;

import java.sql.*;
import java.time.LocalDate;

/**
 * Maneja la tabla MULTA_TARIFA:
 *  ID (PK), CORREO_BIBLIO, TARIFA_DIA, VIGENTE_DESDE, ACTIVA (0/1)
 *
 * Permite tener una tarifa por bibliotecario (si no hay activa, se usa la global de ConfigDAO).
 */
public final class MultaTarifaDAO {
    private MultaTarifaDAO(){}

    public static Double getTarifaActivaPorBibliotecario(String correoBiblio) throws SQLException {
        String sql = """
          SELECT TARIFA_DIA
            FROM MULTA_TARIFA
           WHERE CORREO_BIBLIO = ?
             AND ACTIVA = 1
           ORDER BY VIGENTE_DESDE DESC
           FETCH FIRST 1 ROWS ONLY
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correoBiblio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return null; // usar global
    }

    public static boolean activarTarifa(String correoBiblio, double tarifa) throws SQLException {
        if (tarifa < 0) throw new IllegalArgumentException("tarifa >= 0");
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                // desactivar anteriores
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE MULTA_TARIFA SET ACTIVA=0 WHERE CORREO_BIBLIO=?")) {
                    ps.setString(1, correoBiblio);
                    ps.executeUpdate();
                }
                // insertar nueva
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO MULTA_TARIFA (ID, CORREO_BIBLIO, TARIFA_DIA, VIGENTE_DESDE, ACTIVA) " +
                                "VALUES (MULTA_TARIFA_SEQ.NEXTVAL, ?, ?, ?, 1)")) {
                    ps.setString(1, correoBiblio);
                    ps.setDouble(2, tarifa);
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                    int n = ps.executeUpdate();
                    c.commit();
                    return n == 1;
                }
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
