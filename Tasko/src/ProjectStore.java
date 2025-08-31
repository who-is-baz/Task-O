import java.util.*;

public class ProjectStore {
    private static final ProjectStore INSTANCE = new ProjectStore();
    public static ProjectStore get() { return INSTANCE; }
    private ProjectStore() {}

    private final Map<String, ProjectModel> cache = new HashMap<>();

    /** ¿Existe en caché? (sin BD) */
    public synchronized boolean exists(String name) {
        return cache.containsKey(name);
    }

    /** Obtiene o crea un proyecto en memoria (sin tocar BD) */
    public synchronized ProjectModel getOrCreate(String name) {
        return cache.computeIfAbsent(name, ProjectModel::new);
    }

    public synchronized ProjectModel get(String name) { return cache.get(name); }

    public synchronized Collection<ProjectModel> all() { return cache.values(); }

    /** “Guardar” = actualizar caché (sin BD) */
    public synchronized void saveSnapshot(ProjectModel pm) {
        cache.put(pm.name, pm);
    }

    /** “Actualizar visual” = actualizar caché (sin BD) */
    public synchronized void updateVisual(ProjectModel pm) {
        cache.put(pm.name, pm);
    }
}

/** ===== Modelos ===== */
// >>> En ProjectStore.java (agrega/ajusta SOLO estas partes) <<<
class ProjectModel {
    public final String name;

    // NUEVO: Identificadores para BD
    public Long id;            // id del proyecto en BD
    public Long ownerId;       // FK users.id

    public final List<ColumnModel> columns = new ArrayList<>();
    public String backgroundPath;
    public Integer uiColorRgb;
    public String templateId;

    public final List<CommentModel> comments = new ArrayList<>();
    ProjectModel(String name) { this.name = name; }
}

class ColumnModel {
    public Long id;            // id columna en BD
    public Long projectId;     // FK projects.id
    public int position;       // orden
    public String title;
    public final List<TaskModel> tasks = new ArrayList<>();
    ColumnModel(String title) { this.title = title; }
}

class TaskModel {
    public Long id;            // id tarea en BD
    public Long columnId;      // FK project_columns.id
    public String title;
    public String description;
    public String priority;
    public long createdAtMillis = System.currentTimeMillis();
    public long dueAtMillis = 0L;
    TaskModel(String t, String d, String p) { title = t; description = d; priority = p; }
}



class CommentModel {
    public final String author;  // puedes poner el email o “Tú”
    public final String text;
    public final long createdAtMillis = System.currentTimeMillis();
    CommentModel(String author, String text) { this.author = author; this.text = text; }
}
