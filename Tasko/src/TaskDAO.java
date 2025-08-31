import java.sql.*;
import java.util.*;

public class TaskDAO {

    public Long create(long columnId, TaskModel t) throws SQLException {
        String sql = "INSERT INTO tasks(column_id,title,description,priority,created_at,due_at) VALUES(?,?,?,?,?,?)";
        return Database.withRetry(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, columnId);
                ps.setString(2, t.title);
                ps.setString(3, t.description);
                ps.setString(4, t.priority);
                ps.setLong(5, t.createdAtMillis);
                if (t.dueAtMillis <= 0) ps.setNull(6, Types.BIGINT); else ps.setLong(6, t.dueAtMillis);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) { return gk.next() ? gk.getLong(1) : null; }
            }
        });
    }

    public List<TaskModel> listByColumn(long columnId) throws SQLException {
        String sql = "SELECT id,title,description,priority,created_at,COALESCE(due_at,0) AS due_at FROM tasks WHERE column_id=? ORDER BY id DESC";
        List<TaskModel> out = new ArrayList<>();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, columnId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TaskModel t = new TaskModel(
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("priority"));
                    t.id = rs.getLong("id");
                    t.columnId = columnId;
                    t.createdAtMillis = rs.getLong("created_at");
                    t.dueAtMillis = rs.getLong("due_at"); // 0 si NULL
                    out.add(t);
                }
            }
        }
        return out;
    }

    public void delete(long taskId) throws SQLException {
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM tasks WHERE id=?")) {
            ps.setLong(1, taskId);
            ps.executeUpdate();
        }
    }
}
