package org.example.int_biblioteca.dao;

import org.example.int_biblioteca.Database;

import java.sql.*;
import java.util.Optional;

/**
 * Configuración global en tabla CONFIGURACION (CLAVE, VALOR).
 * Claves usadas:
 *  - DIAS_LIMITE           -> entero (default 3)
 *  - MULTA_TARIFA_GLOBAL   -> decimal/double (default 50.0)
 */
public final class ConfigDAO {
    private ConfigDAO(){}

    private static Optional<String> getRaw(String clave) throws SQLException {
        String sql = "SELECT VALOR FROM CONFIGURACION WHERE CLAVE = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString(1));
                return Optional.empty();
            }
        }
    }

    private static boolean upsert(String clave, String valor) throws SQLException {
        String upd = "UPDATE CONFIGURACION SET VALOR=? WHERE CLAVE=?";
        String ins = "INSERT INTO CONFIGURACION (CLAVE, VALOR) VALUES (?,?)";
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, valor);
                ps.setString(2, clave);
                int n = ps.executeUpdate();
                if (n == 1) return true;
            }
            try (PreparedStatement ps = c.prepareStatement(ins)) {
                ps.setString(1, clave);
                ps.setString(2, valor);
                return ps.executeUpdate() == 1;
            }
        }
    }

    // ===== DÍAS LÍMITE =====
    public static int getDiasLimite() {
        try {
            return getRaw("DIAS_LIMITE").map(Integer::parseInt).orElse(3);
        } catch (Exception ignored) {
            return 3;
        }
    }

    public static boolean setDiasLimite(int dias) throws SQLException {
        if (dias <= 0) throw new IllegalArgumentException("dias debe ser > 0");
        return upsert("DIAS_LIMITE", Integer.toString(dias));
    }

    // ===== TARIFA MULTA GLOBAL =====
    public static double getTarifaMulta() {
        try {
            return getRaw("MULTA_TARIFA_GLOBAL").map(Double::parseDouble).orElse(50.0);
        } catch (Exception ignored) {
            return 50.0;
        }
    }

    public static boolean setTarifaMulta(double tarifa) throws SQLException {
        if (tarifa < 0) throw new IllegalArgumentException("tarifa debe ser >= 0");
        return upsert("MULTA_TARIFA_GLOBAL", Double.toString(tarifa));
    }
}
