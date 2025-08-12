package org.example.int_biblioteca.dao;

import org.example.int_biblioteca.Database;

import java.sql.*;

public final class CmsDAO {
    private CmsDAO(){}

    public static String get(String clave) throws SQLException {
        final String sql = "SELECT VALOR FROM CMS_CONFIG WHERE CLAVE = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null; // getString funciona con CLOB
            }
        }
    }

    public static void set(String clave, String valor) throws SQLException {
        final String sql = """
            MERGE INTO CMS_CONFIG c
            USING (SELECT ? CLAVE, ? VALOR FROM dual) src
               ON (c.CLAVE = src.CLAVE)
            WHEN MATCHED THEN UPDATE SET c.VALOR = src.VALOR
            WHEN NOT MATCHED THEN INSERT (CLAVE, VALOR) VALUES (src.CLAVE, src.VALOR)
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, valor);
            ps.executeUpdate();
        }
    }
}
