import java.sql.*;
import java.util.*;

public class ProjectDAO {

    public Long create(long ownerId, String name, Integer uiColorRgb, String templateId, String backgroundPath) throws SQLException {
        String sql = "INSERT INTO projects(owner_id,name,ui_color_rgb,template_id,background_path) VALUES(?,?,?,?,?)";
        return Database.withRetry(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, ownerId);
                ps.setString(2, name);
                if (uiColorRgb == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, uiColorRgb);
                if (templateId == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, templateId);
                if (backgroundPath == null) ps.setNull(5, Types.LONGVARCHAR); else ps.setString(5, backgroundPath);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) { return gk.next() ? gk.getLong(1) : null; }
            }
        });
    }

    public List<ProjectModel> listByOwner(long ownerId) throws SQLException {
        String sql = "SELECT id, name, ui_color_rgb, template_id, background_path FROM projects WHERE owner_id=? ORDER BY id DESC";
        List<ProjectModel> out = new ArrayList<>();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProjectModel pm = new ProjectModel(rs.getString("name"));
                    pm.id = rs.getLong("id");
                    pm.ownerId = ownerId;
                    int rgb = rs.getInt("ui_color_rgb"); if (!rs.wasNull()) pm.uiColorRgb = rgb;
                    pm.templateId = rs.getString("template_id");
                    pm.backgroundPath = rs.getString("background_path");
                    out.add(pm);
                }
            }
        }
        return out;
    }

    public void updateVisual(ProjectModel pm) throws SQLException {
        if (pm.id == null) return;
        String sql = "UPDATE projects SET ui_color_rgb=?, template_id=?, background_path=? WHERE id=?";
        Database.withRetry(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (pm.uiColorRgb == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, pm.uiColorRgb);
                if (pm.templateId == null) ps.setNull(2, Types.VARCHAR); else ps.setString(2, pm.templateId);
                if (pm.backgroundPath == null) ps.setNull(3, Types.LONGVARCHAR); else ps.setString(3, pm.backgroundPath);
                ps.setLong(4, pm.id);
                ps.executeUpdate();
                return null;
            }
        });
    }

    public void delete(long projectId) throws SQLException {
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM projects WHERE id=?")) {
            ps.setLong(1, projectId);
            ps.executeUpdate();
        }
    }
}
