import java.sql.*;
import java.util.*;

public class ColumnDAO {

    public Long create(long projectId, String title, int position) throws SQLException {
        String sql = "INSERT INTO project_columns(project_id,title,position) VALUES(?,?,?)";
        return Database.withRetry(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, projectId);
                ps.setString(2, title);
                ps.setInt(3, position);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) { return gk.next() ? gk.getLong(1) : null; }
            }
        });
    }

    public List<ColumnModel> listByProject(long projectId) throws SQLException {
        String sql = "SELECT id, title, position FROM project_columns WHERE project_id=? ORDER BY position ASC, id ASC";
        List<ColumnModel> out = new ArrayList<>();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ColumnModel cm = new ColumnModel(rs.getString("title"));
                    cm.id = rs.getLong("id");
                    cm.projectId = projectId;
                    cm.position = rs.getInt("position");
                    out.add(cm);
                }
            }
        }
        return out;
    }

    public void delete(long columnId) throws SQLException {
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM project_columns WHERE id=?")) {
            ps.setLong(1, columnId);
            ps.executeUpdate();
        }
    }
}
