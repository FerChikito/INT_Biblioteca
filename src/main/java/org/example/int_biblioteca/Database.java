package org.example.int_biblioteca;

import java.io.InputStream;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

public final class Database {
    private static final String DEFAULT_URL = "jdbc:oracle:thin:@pprogra_high";
    private static final String PROP_FILE_SYS = "db.config"; // VM option opcional: -Ddb.config=otra/ruta.properties
    private static volatile Properties PROPS;

    private Database() {}

    private static Properties props() {
        if (PROPS != null) return PROPS;
        synchronized (Database.class) {
            if (PROPS != null) return PROPS;
            PROPS = new Properties();

            String override = System.getProperty(PROP_FILE_SYS);
            Path cfg = override != null ? Paths.get(override) : Paths.get("config", "db.properties");

            try (InputStream in = Files.newInputStream(cfg)) {
                PROPS.load(in);
            } catch (Exception e) {
                throw new IllegalStateException("No pude leer " + cfg.toAbsolutePath()
                        + ". Define -Ddb.config=... o crea config/db.properties. Causa: " + e.getMessage(), e);
            }

            // Configura el wallet (TNS_ADMIN) si viene en properties
            String tns = PROPS.getProperty("tns_admin");
            if (tns != null && !tns.isBlank()) {
                System.setProperty("oracle.net.tns_admin", tns);
            }

            try { Class.forName("oracle.jdbc.OracleDriver"); } catch (ClassNotFoundException ignored) {}
            DriverManager.setLoginTimeout(10);
            return PROPS;
        }
    }

    private static String get(String env, String propKey, String def) {
        String v = System.getenv(env);
        if (v != null && !v.isBlank()) return v;
        v = props().getProperty(propKey);
        return (v == null || v.isBlank()) ? def : v;
    }

    public static Connection getConnection() throws SQLException {
        String url  = get("DB_URL",  "db.url",  DEFAULT_URL); // alias TNS
        String user = get("DB_USER", "db.user", null);
        String pass = get("DB_PASS", "db.pass", null);
        if (user == null || pass == null) {
            throw new IllegalStateException("Faltan credenciales: define DB_USER/DB_PASS o db.user/db.pass");
        }
        return DriverManager.getConnection(url, user, pass);
    }

    public static boolean ping() {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM DUAL");
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Ping BD falló: " + e.getMessage());
            return false;
        }
    }
}
