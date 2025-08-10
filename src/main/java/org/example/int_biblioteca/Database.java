package org.example.int_biblioteca;

import java.io.InputStream;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Database {
    private static final String DEFAULT_URL = "jdbc:oracle:thin:@pprogra_high"; // alias de tu tnsnames.ora
    private static final String PROP_FILE_SYS = "db.config";                    // VM option opcional: -Ddb.config=...
    private static volatile Properties PROPS;
    private static final java.util.concurrent.atomic.AtomicBoolean LOGGED_TNS = new java.util.concurrent.atomic.AtomicBoolean(false);

    private Database() {}

    // ---------- Carga de propiedades ----------
    private static Properties props() {
        if (PROPS != null) return PROPS;
        synchronized (Database.class) {
            if (PROPS != null) return PROPS;
            PROPS = new Properties();

            // 1) Ruta del .properties: -Ddb.config=... o por defecto config/db.properties
            String override = System.getProperty(PROP_FILE_SYS);
            Path cfg = (override != null && !override.isBlank())
                    ? Paths.get(override)
                    : Paths.get("config", "db.properties");

            try (InputStream in = Files.newInputStream(cfg)) {
                PROPS.load(in);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "No pude leer " + cfg.toAbsolutePath() +
                                ". Define -Ddb.config=... o crea config/db.properties. Causa: " + e.getMessage(), e);
            }

            // 2) Configurar TNS_ADMIN (prioridad: VM option -> ENV -> properties)
            String tns = System.getProperty("oracle.net.tns_admin");
            if (tns == null || tns.isBlank()) tns = System.getenv("TNS_ADMIN");
            if (tns == null || tns.isBlank()) tns = PROPS.getProperty("db.tns_admin"); // <-- clave correcta
            if (tns != null && !tns.isBlank()) {
                System.setProperty("oracle.net.tns_admin", tns);
            }

            // 3) Driver y timeout
            try { Class.forName("oracle.jdbc.OracleDriver"); } catch (ClassNotFoundException ignored) {}
            DriverManager.setLoginTimeout(15);
            return PROPS;
        }
    }

    // ---------- Helpers para leer url/usuario/password ----------
    private static String url() {
        String v = System.getenv("DB_URL");
        if (v != null && !v.isBlank()) return v;
        v = System.getProperty("db.url"); // por si lo pasas como -Ddb.url=...
        if (v != null && !v.isBlank()) return v;
        v = props().getProperty("db.url");
        return (v == null || v.isBlank()) ? DEFAULT_URL : v;
    }

    private static String user() {
        String v = System.getenv("DB_USER");
        if (v != null && !v.isBlank()) return v;
        v = System.getProperty("db.user");
        if (v != null && !v.isBlank()) return v;
        v = props().getProperty("db.user");
        if (v == null || v.isBlank())
            throw new IllegalStateException("Falta usuario: define DB_USER o db.user en config/db.properties");
        return v;
    }

    private static String pass() {
        String v = System.getenv("DB_PASS");
        if (v != null && !v.isBlank()) return v;
        v = System.getProperty("db.pass");
        if (v != null && !v.isBlank()) return v;
        v = props().getProperty("db.pass");
        if (v == null || v.isBlank())
            throw new IllegalStateException("Falta contraseña: define DB_PASS o db.pass en config/db.properties");
        return v;
    }

    // ---------- Conexión ----------
    public static Connection getConnection() throws SQLException {
        // 1) Asegura cargar properties y setear db.tns_admin -> oracle.net.tns_admin
        props();

        // 2) Log una sola vez
        if (!LOGGED_TNS.getAndSet(true)) {
            String tns = System.getProperty("oracle.net.tns_admin");
            System.out.println("[DB] TNS_ADMIN=" + (tns == null || tns.isBlank() ? "<no definido>" : tns));
        }

        // 3) Conecta con lo que haya en db.url/db.user/db.pass (o overrides)
        return DriverManager.getConnection(url(), user(), pass());
    }

    // ---------- Ping ----------
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
