import java.sql.*;

public class UserDAO {
    public Long register(String email, String rawPassword, String displayName, boolean isAdmin) throws SQLException {
        String sql = "INSERT INTO users(email, password_hash, display_name, is_admin) VALUES(?,?,?,?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email.toLowerCase().trim());
            ps.setString(2, PasswordUtil.sha256(rawPassword));
            ps.setString(3, displayName);
            ps.setBoolean(4, isAdmin);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    public Long login(String email, String rawPassword) throws SQLException {
        String sql = "SELECT id, password_hash FROM users WHERE email=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (hash.equals(PasswordUtil.sha256(rawPassword))) return rs.getLong("id");
                }
            }
        }
        return null;
    }
    public void ensureAdmin(String email, String rawPassword, String displayName) throws SQLException {
        String q = "SELECT id FROM users WHERE email=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return; // ya existe
            }
        }
        // no existe -> crear admin
        register(email, rawPassword, displayName, true);
    }
    // UserDAO.java
    public Long findIdByEmail(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return null;
    }


    public boolean isAdmin(long userId) throws SQLException {
        String sql = "SELECT is_admin FROM users WHERE id=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
}
