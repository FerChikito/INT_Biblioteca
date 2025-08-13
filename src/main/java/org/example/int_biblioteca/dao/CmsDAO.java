package org.example.int_biblioteca.dao;

import org.example.int_biblioteca.Database;
import java.sql.*;

public final class CmsDAO {
    private CmsDAO(){}

    public static String get(String clave) throws SQLException {
        String sql = "SELECT VALOR FROM CMS_CONFIG WHERE CLAVE = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public static boolean set(String clave, String valor) throws SQLException {
        String up = """
            MERGE INTO CMS_CONFIG t
            USING (SELECT ? CLAVE, ? VALOR FROM dual) s
               ON (t.CLAVE = s.CLAVE)
             WHEN MATCHED THEN UPDATE SET t.VALOR = s.VALOR
             WHEN NOT MATCHED THEN INSERT (CLAVE, VALOR) VALUES (s.CLAVE, s.VALOR)
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(up)) {
            ps.setString(1, clave);
            ps.setString(2, valor);
            return ps.executeUpdate() >= 1;
        }
    }
}
