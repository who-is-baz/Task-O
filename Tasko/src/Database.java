import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.sql.*;
import java.util.Properties;
import java.util.function.Supplier;


public final class Database {
    // ... (tu Database.init y Database.get como ya los tienes) ...

    @FunctionalInterface
    public interface SqlWork<T> {
        T run(Connection c) throws SQLException;
    }

    /** Ejecuta trabajo SQL con un reintento si hay Communications link failure. */
    public static <T> T withRetry(SqlWork<T> work) throws SQLException {
        try {
            try (Connection c = get()) {
                return work.run(c);
            }
        } catch (SQLException ex) {
            // Si es problema de comunicación, intenta UNA vez más con nueva conexión
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("communications link failure") || msg.contains("connection reset")
                    || msg.contains("connection is closed")) {
                try (Connection c2 = get()) {
                    return work.run(c2);
                }
            }
            throw ex;
        }
    }

    private static String URL, USER, PASS;

    public static synchronized void init(String url, String user, String pass) {
        URL = url; USER = user; PASS = pass;
        try {
            try { Class.forName("com.mysql.cj.jdbc.Driver"); }
            catch (ClassNotFoundException e) { Class.forName("org.mariadb.jdbc.Driver"); }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver JDBC.", e);
        }
    }

    /** SIEMPRE devuelve una conexión nueva. Quien la use debe cerrarla. */
    public static Connection get() throws SQLException {
        if (URL == null) throw new IllegalStateException("Database.init(...) no fue llamado.");
        Properties p = new Properties();
        if (USER != null) p.setProperty("user", USER);
        if (PASS != null) p.setProperty("password", PASS);
        p.setProperty("useUnicode", "true");
        p.setProperty("characterEncoding", "utf8");
        return DriverManager.getConnection(URL, p);
    }

    public static boolean testConnection() {
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            e.printStackTrace(); return false;
        }
    }
}
