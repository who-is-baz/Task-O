import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class CustomTemplates {

    public static final String FILE_NAME = "custom_templates.tsv";

    public static class SimpleTemplate {
        public final String id;
        public final String displayName;
        public final Color headerColor;
        public final String backgroundPath; // ruta local (puede estar vacía)

        public SimpleTemplate(String id, String displayName, Color headerColor, String backgroundPath) {
            this.id = id;
            this.displayName = displayName;
            this.headerColor = headerColor;
            this.backgroundPath = backgroundPath == null ? "" : backgroundPath;
        }
    }

    public static synchronized List<SimpleTemplate> all() {
        List<SimpleTemplate> out = new ArrayList<>();
        File f = new File(FILE_NAME);
        if (!f.exists()) return out;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\t", -1);
                if (p.length < 4) continue;
                String id   = p[0];
                String name = p[1];
                int rgb;
                try { rgb = Integer.parseInt(p[2]); } catch (Exception e) { continue; }
                String bg   = p[3];
                out.add(new SimpleTemplate(id, name, new Color(rgb), bg));
            }
        } catch (IOException ignored) {}
        return out;
    }

    public static synchronized void add(SimpleTemplate s) throws IOException {
        // append al archivo
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(s.id); bw.write('\t');
            bw.write(s.displayName); bw.write('\t');
            bw.write(Integer.toString(s.headerColor.getRGB())); bw.write('\t');
            bw.write(s.backgroundPath == null ? "" : s.backgroundPath);
            bw.write('\n');
        }
    }

    public static synchronized void delete(String id) throws IOException {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        List<SimpleTemplate> keep = new ArrayList<>();
        for (SimpleTemplate s : all()) {
            if (!s.id.equalsIgnoreCase(id)) keep.add(s);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
            for (SimpleTemplate s : keep) {
                bw.write(s.id); bw.write('\t');
                bw.write(s.displayName); bw.write('\t');
                bw.write(Integer.toString(s.headerColor.getRGB())); bw.write('\t');
                bw.write(s.backgroundPath == null ? "" : s.backgroundPath);
                bw.write('\n');
            }
        }
    }
}
