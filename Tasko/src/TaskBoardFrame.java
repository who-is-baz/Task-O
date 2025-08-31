import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.net.URL;

/**
 * Ventana de tablero con soporte de fondo, plantillas y comentarios (chat lateral).
 * Persistencia: SOLO en memoria mediante ProjectStore (sin BD).
 */
public class TaskBoardFrame extends JFrame {
    private final ProjectModel project;     // Modelo que persiste en memoria
    private JPanel boardPanel;              // Contenedor de columnas
    private JPanel header;                  // Encabezado (para recolorear por plantilla)
    private ImageViewport viewport;         // Viewport que pinta la imagen de fondo

    // Chat lateral
    private JPanel chatPanel;               // Contenedor del chat (panel lateral)
    private JPanel chatMessages;            // Lista de mensajes (vertical)
    private JScrollPane chatScroll;         // Scroll del chat
    private boolean chatVisible = true;     // visible por defecto
    private final String currentUserDisplay = "Tú"; // cámbialo a email/nombre real si lo tienes

    public TaskBoardFrame(ProjectModel project) {
        this.project = project;
        setTitle("Proyecto: " + project.name);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1500, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // ====== HEADER ======
        header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 55, 109));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Tablero • " + project.name);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        /// Buscador
                searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Buscar tareas…"); // si usas LAF moderno lo muestra
        searchField.setToolTipText("Buscar por título, descripción o prioridad");
        searchField.addActionListener(e -> { currentQuery = searchField.getText(); refreshColumns(); });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { currentQuery = searchField.getText(); refreshColumns(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { currentQuery = searchField.getText(); refreshColumns(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { currentQuery = searchField.getText(); refreshColumns(); }
        });

// Botón para limpiar búsqueda
        JButton btnClearSearch = new JButton("✕");
        estilizarBotonSecundario(btnClearSearch);
        btnClearSearch.setToolTipText("Limpiar búsqueda");
        btnClearSearch.addActionListener(e -> {
            searchField.setText("");
            currentQuery = "";
            refreshColumns();
        });

// Añade el buscador al header (antes del resto de acciones)
        actions.add(searchField);
        actions.add(btnClearSearch);




        // Botón Comentarios (mostrar/ocultar chat)
        JButton btnChat = new JButton("Comentarios");
        estilizarBotonSecundario(btnChat);
        btnChat.setToolTipText("Mostrar/ocultar comentarios del proyecto");
        btnChat.addActionListener(e -> toggleChat());

        // Botón Plantillas (visible)
        JButton btnTemplates = new JButton("Plantillas");
        estilizarBotonSecundario(btnTemplates);
        btnTemplates.setToolTipText("Elegir una plantilla de color y fondo");
        btnTemplates.addActionListener(e -> elegirPlantilla());

        // Botón Fondo... (visible)
        JButton btnBackground = new JButton("Fondo...");
        estilizarBotonSecundario(btnBackground);
        btnBackground.setToolTipText("Seleccionar imagen de fondo desde tu equipo");
        btnBackground.addActionListener(e -> elegirFondoLocal());
        // Botón Reporte
        JButton btnReporte = new JButton("Reporte");
        estilizarBotonSecundario(btnReporte);
        btnReporte.setToolTipText("Ver resumen de proyecto y tareas");
        btnReporte.addActionListener(e -> showReportDialog());
        actions.add(btnReporte);

        // Botón Nueva columna (primario)
        JButton addColumn = new JButton("+ Nueva Columna");
        addColumn.setBackground(new Color(40, 167, 69));
        addColumn.setForeground(Color.WHITE);
        addColumn.setBorderPainted(false);
        addColumn.setFocusPainted(false);
        addColumn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addColumn.addActionListener(e -> addColumnFlow());

        // Orden de botones en el header
        actions.add(btnChat);
        actions.add(btnTemplates);
        actions.add(btnBackground);
        actions.add(addColumn);

        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // ====== BOARD CON FONDO ======
        boardPanel = new JPanel();
        boardPanel.setLayout(new BoxLayout(boardPanel, BoxLayout.X_AXIS));
        boardPanel.setBackground(new Color(245, 245, 245));
        boardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        boardPanel.setOpaque(false); // importante para ver el fondo

        viewport = new ImageViewport();
        viewport.setView(boardPanel);
        viewport.setOverlayDark(0.12f); // leve oscurecido para contraste

        JScrollPane scroll = new JScrollPane();
        scroll.setViewport(viewport);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        // Columnas por defecto si no hay
        if (project.columns.isEmpty()) {
            project.columns.add(new ColumnModel("Por hacer"));
            project.columns.add(new ColumnModel("En progreso"));
            project.columns.add(new ColumnModel("Hecho"));
        }

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);

        // ====== CHAT LATERAL ======
        buildChatPanel();
        if (chatVisible) getContentPane().add(chatPanel, BorderLayout.EAST);

        // Aplica plantilla/fondo del modelo (en memoria)
        aplicarEstadoVisualDesdeModelo();

        // ... después de inicializar 'project'
        if (project.id != null) {
            cargarEstructuraDesdeBD();
        }
        refreshColumns();


        // Dibuja columnas existentes
        refreshColumns();
    }
    private void cargarEstructuraDesdeBD() {
        if (project.id == null) return;

        try {
            ColumnDAO cdao = new ColumnDAO();
            TaskDAO tdao = new TaskDAO();

            // 1) Trae columnas
            java.util.List<ColumnModel> cols = cdao.listByProject(project.id);

            // 2) Si NO hay columnas en BD, siembra las 3 por defecto (una sola vez)
            if (cols.isEmpty()) {
                Long todoId = cdao.create(project.id, "Por hacer", 0);
                Long progId = cdao.create(project.id, "En progreso", 1);
                Long doneId = cdao.create(project.id, "Hecho", 2);

                ColumnModel c1 = new ColumnModel("Por hacer");  c1.id = todoId; c1.projectId = project.id; c1.position = 0;
                ColumnModel c2 = new ColumnModel("En progreso"); c2.id = progId; c2.projectId = project.id; c2.position = 1;
                ColumnModel c3 = new ColumnModel("Hecho");       c3.id = doneId; c3.projectId = project.id; c3.position = 2;

                cols.add(c1); cols.add(c2); cols.add(c3);
            }

            // 3) Limpia el modelo en memoria y recarga desde BD
            project.columns.clear();
            for (ColumnModel cm : cols) {
                java.util.List<TaskModel> tasks = tdao.listByColumn(cm.id);
                cm.tasks.clear();
                cm.tasks.addAll(tasks);
                project.columns.add(cm);
            }

            // 4) Aplica el estado visual que ya tengas (plantilla/fondo)
            aplicarEstadoVisualDesdeModelo();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar columnas/tareas:\n" + ex.getMessage());
        }
    }



    // ========= DROP-IN: pega esto dentro de TaskBoardFrame (misma clase) =========
    private JPanel makeTaskPanelFiltered(ColumnModel cm, TaskModel tm, String query) {
        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setPreferredSize(new Dimension(280, 130));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(priorityColor(tm.priority), 3),
                new EmptyBorder(10,10,10,10)
        ));

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        // Helpers locales para resaltar
        String q = (query == null) ? "" : query.trim();
        java.util.function.Function<String,String> hi = (s) -> {
            String base = escapeHtml(s == null ? "" : s);
            if (q.isEmpty()) return base;
            String safeQ = java.util.regex.Pattern.quote(q);
            return base.replaceFirst("(?i)" + safeQ, "<span style='background:#ffe08a;'>$0</span>");
        };

        // Título con highlight
        JLabel title = new JLabel("<html>" + hi.apply(tm.title) + "</html>");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(33,37,41));

        // Descripción (truncada) con highlight
        String descFull = tm.description == null ? "" : tm.description;
        String truncated = descFull.length() > 80 ? descFull.substring(0,80) + "..." : descFull;
        JLabel desc = new JLabel("<html>"+ hi.apply(truncated) +"</html>");
        desc.setFont(new Font("Arial", Font.PLAIN, 12));
        desc.setForeground(Color.GRAY);

        // Línea de info: prioridad, creación, fecha límite
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        info.setBackground(Color.WHITE);

        JLabel pr = new JLabel("● " + tm.priority);
        pr.setForeground(priorityColor(tm.priority));
        pr.setFont(new Font("Arial", Font.BOLD, 11));

        String created = java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")
                .withZone(java.time.ZoneId.systemDefault())
                .format(java.time.Instant.ofEpochMilli(tm.createdAtMillis));
        JLabel dt = new JLabel("| " + created);
        dt.setFont(new Font("Arial", Font.PLAIN, 10));
        dt.setForeground(Color.GRAY);

        info.add(pr);
        info.add(dt);

        if (tm.dueAtMillis > 0) {
            String dueStr = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .withZone(java.time.ZoneId.systemDefault())
                    .format(java.time.Instant.ofEpochMilli(tm.dueAtMillis));
            JLabel due = new JLabel("| ⏰ " + dueStr);
            due.setFont(new Font("Arial", Font.BOLD, 11));
            due.setForeground(colorForDue(tm.dueAtMillis));
            due.setToolTipText("Fecha límite");
            info.add(due);
        }

        // Panel vertical con título, descripción e info
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(desc);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(info);

        // Botón eliminar (✕) con persistencia en BD
        JButton del = new JButton("✕");
        del.setForeground(Color.RED);
        del.setBorderPainted(false);
        del.setContentAreaFilled(false);
        del.setCursor(new Cursor(Cursor.HAND_CURSOR));
        del.setToolTipText("Eliminar tarea");

        del.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(
                    TaskBoardFrame.this,
                    "¿Eliminar tarea?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION
            );
            if (opt != JOptionPane.YES_OPTION) return;

            // 1) Eliminar en BD si existe allí
            try {
                if (tm.id != null) {
                    new TaskDAO().delete(tm.id);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        TaskBoardFrame.this,
                        "No se pudo eliminar la tarea en BD:\n" + ex.getMessage()
                );
                return; // No tocar UI si falló la BD
            }

            // 2) Eliminar de memoria
            cm.tasks.remove(tm);

            // 3) Eliminar de UI (y quitar el filler si está justo después)
            Container parent = card.getParent(); // tasksPanel
            if (parent != null) {
                int idx = -1;
                for (int i = 0; i < parent.getComponentCount(); i++) {
                    if (parent.getComponent(i) == card) { idx = i; break; }
                }
                if (idx != -1) {
                    parent.remove(idx);
                    if (idx < parent.getComponentCount() &&
                            parent.getComponent(idx) instanceof Box.Filler) {
                        parent.remove(idx);
                    }
                }
                parent.revalidate();
                parent.repaint();
            }

            // 4) Persistir cambios visuales del proyecto (si aplica)
            onModelChanged();
        });

        // Hover de la card
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { card.setBackground(new Color(248,249,250)); }
            public void mouseExited (java.awt.event.MouseEvent evt) { card.setBackground(Color.WHITE); }
        });

        content.add(textPanel, BorderLayout.CENTER);
        content.add(del, BorderLayout.EAST);
        card.add(content, BorderLayout.CENTER);
        return card;
    }




    // === BUSCADOR ===
    private JTextField searchField;
    private String currentQuery = "";

    /** Normaliza string para comparación. */
    private String norm(String s) { return s == null ? "" : s.toLowerCase(); }

    /** ¿La tarea coincide con la búsqueda? (título, desc o prioridad) */
    private boolean matches(TaskModel t, String q) {
        if (q == null || q.trim().isEmpty()) return true; // sin filtro
        String qn = norm(q).trim();
        if (qn.isEmpty()) return true;
        if (norm(t.title).contains(qn)) return true;
        if (t.description != null && norm(t.description).contains(qn)) return true;
        if (t.priority != null && norm(t.priority).contains(qn)) return true;
        return false;
    }

    /** Resalta (case-insensitive) la primera coincidencia de q en s (con escape HTML). */
    private String highlightOnce(String s, String q) {
        if (s == null) s = "";
        String html = escapeHtml(s);
        if (q == null || q.trim().isEmpty()) return html;
        String qn = q.trim();
        // Búsqueda insensible a mayúsculas: usamos (?i) y escapamos caracteres especiales en q
        String safeQ = java.util.regex.Pattern.quote(qn);
        return html.replaceFirst("(?i)" + safeQ, "<span style='background: #ffe08a;'>$0</span>");
    }



    /** Construye el CSV completo del reporte (encabezado + filas de tareas). */
    private String buildCsvReport() {
        StringBuilder sb = new StringBuilder();

        // Primera línea con el nombre del proyecto
        sb.append(csv("Proyecto")).append(',').append(csv(project.name)).append('\n');
        sb.append('\n');

        // Encabezados de columnas
        sb.append(csv("Columna")).append(',')
                .append(csv("Título")).append(',')
                .append(csv("Descripción")).append(',')
                .append(csv("Prioridad")).append(',')
                .append(csv("Creada")).append(',')
                .append(csv("Fecha límite")).append(',')
                .append(csv("Estado")).append('\n');

        java.time.format.DateTimeFormatter createdFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(java.time.ZoneId.systemDefault());
        java.time.format.DateTimeFormatter dueFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(java.time.ZoneId.systemDefault());

        for (ColumnModel col : project.columns) {
            for (TaskModel t : col.tasks) {
                String created = createdFmt.format(java.time.Instant.ofEpochMilli(t.createdAtMillis));
                String dueStr  = t.dueAtMillis > 0 ? dueFmt.format(java.time.Instant.ofEpochMilli(t.dueAtMillis)) : "";
                String estado  = dueStatus(t.dueAtMillis);

                sb.append(csv(col.title)).append(',')
                        .append(csv(t.title)).append(',')
                        .append(csv(t.description == null ? "" : t.description)).append(',')
                        .append(csv(t.priority)).append(',')
                        .append(csv(created)).append(',')
                        .append(csv(dueStr)).append(',')
                        .append(csv(estado)).append('\n');
            }
        }
        return sb.toString();
    }

    /** Escapa un valor para CSV (comillas dobles, comas, saltos de línea). */
    private String csv(String s) {
        if (s == null) s = "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + escaped + "\"" : escaped;
    }

    /** Devuelve estado legible para la fecha límite. */
    private String dueStatus(long dueAtMillis) {
        if (dueAtMillis <= 0) return "(sin fecha)";
        long now = System.currentTimeMillis();
        if (dueAtMillis < now) return "VENCIDA";
        long diff = dueAtMillis - now;
        long twoDays = 48L * 3600L * 1000L;
        return (diff <= twoDays) ? "POR VENCER (≤48h)" : "EN TIEMPO";
    }


    // ---------- Estilos ----------
    private void estilizarBotonSecundario(JButton b) {
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(new Color(255,255,255,160)));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    private void showReportDialog() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter createdFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());
        DateTimeFormatter dueFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault());

        // Encabezado
        sb.append("REPORTE DEL PROYECTO\n");
        sb.append("====================\n");
        sb.append("Nombre: ").append(project.name).append("\n");

        int totalCols = project.columns.size();
        int totalTasks = project.columns.stream().mapToInt(c -> c.tasks.size()).sum();
        sb.append("Columnas: ").append(totalCols).append("\n");
        sb.append("Tareas: ").append(totalTasks).append("\n\n");

        // Detalle por tarea
        if (totalTasks == 0) {
            sb.append("No hay tareas registradas.\n");
        } else {
            for (ColumnModel col : project.columns) {
                if (col.tasks.isEmpty()) continue;
                sb.append("== Columna: ").append(col.title).append(" ==\n");
                for (TaskModel t : col.tasks) {
                    sb.append("• Título      : ").append(t.title).append("\n");
                    sb.append("  Descripción : ").append(t.description == null || t.description.isEmpty() ? "(sin descripción)" : t.description).append("\n");
                    sb.append("  Prioridad   : ").append(t.priority).append("\n");
                    sb.append("  Creada      : ").append(createdFmt.format(Instant.ofEpochMilli(t.createdAtMillis))).append("\n");

                    if (t.dueAtMillis > 0) {
                        String dueStr = dueFmt.format(Instant.ofEpochMilli(t.dueAtMillis));
                        String estado;
                        long now = System.currentTimeMillis();
                        if (t.dueAtMillis < now) {
                            estado = "VENCIDA";
                        } else if (t.dueAtMillis - now <= 48L*3600L*1000L) {
                            estado = "POR VENCER (≤48h)";
                        } else {
                            estado = "EN TIEMPO";
                        }
                        sb.append("  Fecha límite: ").append(dueStr).append("  (").append(estado).append(")\n");
                    } else {
                        sb.append("  Fecha límite: (sin fecha)\n");
                    }
                    sb.append("\n");
                }
            }
        }

        // UI del reporte
        JTextArea area = new JTextArea(sb.toString(), 28, 80);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(area,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Botones: Copiar y Guardar
        JButton btnCopiar = new JButton("Copiar");
        btnCopiar.addActionListener(e -> {
            area.selectAll();
            area.copy();
            area.select(0,0);
            JOptionPane.showMessageDialog(this, "Reporte copiado al portapapeles.");
        });


        JButton btnGuardar = new JButton("Guardar .txt");
        btnGuardar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("Reporte_" + project.name.replaceAll("[^a-zA-Z0-9._-]", "_") + ".txt"));
            int r = fc.showSaveDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                try (java.io.FileWriter w = new java.io.FileWriter(fc.getSelectedFile())) {
                    w.write(area.getText());
                    JOptionPane.showMessageDialog(this, "Guardado en:\n" + fc.getSelectedFile().getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo guardar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnGuardarCsv = new JButton("Guardar .csv");
        btnGuardarCsv.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("Reporte_" + project.name.replaceAll("[^a-zA-Z0-9._-]", "_") + ".csv"));
            int r = fc.showSaveDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                try (java.io.FileWriter w = new java.io.FileWriter(fc.getSelectedFile())) {
                    w.write(buildCsvReport()); // <-- usa el helper de abajo
                    JOptionPane.showMessageDialog(this, "CSV guardado en:\n" + fc.getSelectedFile().getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo guardar CSV:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });




        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        south.add(btnCopiar);
        south.add(btnGuardar);
        south.add(btnGuardarCsv);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(sp, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);

        JDialog dlg = new JDialog(this, "Reporte de proyecto", true);
        dlg.setContentPane(wrap);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);


    }


    // ---------- Carga estado visual desde el modelo ---
    //
    // -------
    private void aplicarEstadoVisualDesdeModelo() {
        // 1) Plantilla (color header + fondo por recurso + columnas por defecto si vacío)
        if (project.templateId != null) {
            BoardTemplate t = BoardTemplate.byId(project.templateId);
            if (t != null) {
                header.setBackground(t.headerColor);
                Image bg = cargarRecursoImagen(t.resourceBackground);
                if (bg != null) viewport.setBackgroundImage(bg);
                if (project.columns.isEmpty() && t.defaultColumns != null) {
                    for (String c : t.defaultColumns) project.columns.add(new ColumnModel(c));
                }
            }
        }
        // 2) Fondo local (tiene prioridad si existe)
        if (project.backgroundPath != null && !project.backgroundPath.isEmpty()) {
            try {
                Image img = ImageIO.read(new File(project.backgroundPath));
                if (img != null) viewport.setBackgroundImage(img);
            } catch (Exception ignored) {}
        }
    }

    // ---------- Fondo local ----------
    private void elegirFondoLocal() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar imagen de fondo");
        fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg","jpeg","png","webp"));
        int r = fc.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();
                Image img = ImageIO.read(f);
                if (img == null) {
                    JOptionPane.showMessageDialog(this, "No se pudo cargar la imagen.");
                    return;
                }
                viewport.setBackgroundImage(img);
                project.backgroundPath = f.getAbsolutePath();
                // Si usas fondo manual, puedes limpiar la plantilla si quieres:
                // project.templateId = null;

                repaint();
                onModelChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir la imagen:\n" + ex.getMessage());
            }
        }
    }


    private void elegirPlantilla() {
        java.util.List<BoardTemplate> base = BoardTemplate.all();
        java.util.List<CustomTemplates.SimpleTemplate> custom = CustomTemplates.all();

        // Construimos el menú con base + personalizadas
        java.util.List<String> nombres = new java.util.ArrayList<>();
        for (BoardTemplate t : base) nombres.add(t.displayName + " (base)");
        for (CustomTemplates.SimpleTemplate s : custom) nombres.add(s.displayName + " (custom)");

        if (nombres.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay plantillas disponibles.");
            return;
        }

        String[] opciones = nombres.toArray(new String[0]);
        String sel = (String) JOptionPane.showInputDialog(
                this, "Elige una plantilla:", "Plantillas",
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (sel == null) return;

        // ¿Seleccionó base?
        for (BoardTemplate t : base) {
            if (sel.equals(t.displayName + " (base)")) {
                header.setBackground(t.headerColor);
                Image bg = cargarRecursoImagen(t.resourceBackground);
                if (bg != null) {
                    viewport.setBackgroundImage(bg);
                    project.backgroundPath = null;
                }
                project.templateId = t.id;
                if (project.columns.isEmpty() && t.defaultColumns != null) {
                    for (String c : t.defaultColumns) project.columns.add(new ColumnModel(c));
                }
                refreshColumns();
                onModelChanged();
                return;
            }
        }

        // ¿Seleccionó personalizada?
        for (CustomTemplates.SimpleTemplate s : custom) {
            if (sel.equals(s.displayName + " (custom)")) {
                header.setBackground(s.headerColor);
                // Fondo desde archivo local si está
                if (s.backgroundPath != null && !s.backgroundPath.isEmpty()) {
                    try {
                        java.awt.Image img = javax.imageio.ImageIO.read(new java.io.File(s.backgroundPath));
                        if (img != null) {
                            viewport.setBackgroundImage(img);
                            project.backgroundPath = s.backgroundPath; // persistimos ruta local
                        }
                    } catch (Exception ignored) {}
                }
                project.templateId = s.id; // guardamos el id (aunque sea custom)
                refreshColumns();
                onModelChanged();
                return;
            }
        }
    }


    private Image cargarRecursoImagen(String path) {
        try {
            URL url = getClass().getResource(path);
            return (url != null) ? new ImageIcon(url).getImage() : null;
        } catch (Exception e) { return null; }
    }

    /** Guardado inmediato en memoria (sin BD). */
    private void onModelChanged() {
        ProjectStore.get().saveSnapshot(project); // memoria
        // persistir visual si hay id en BD
        if (project.id != null) {
            try { new ProjectDAO().updateVisual(project); } catch (Exception ignored) {}
        }
    }


    // ---------- Render de columnas ----------
    private void refreshColumns() {
        boardPanel.removeAll();
        int totalMatches = 0;
        for (int i = 0; i < project.columns.size(); i++) {
            ColumnModel cm = project.columns.get(i);
            JPanel colPanel = makeColumnPanel(cm, currentQuery); // <-- usa el filtro
            totalMatches += (int) java.util.Arrays.stream(colPanel.getComponents()).count(); // aproximado visual
            boardPanel.add(colPanel);
            boardPanel.add(Box.createHorizontalStrut(15));
        }
        boardPanel.revalidate();
        boardPanel.repaint();
    }




    private void addColumnFlow() {
        String name = JOptionPane.showInputDialog(this, "Nombre de la nueva columna:");
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) return;

        try {
            ColumnModel cm;
            if (project.id != null) {
                ColumnDAO cdao = new ColumnDAO();
                int pos = project.columns.size();
                Long colId = cdao.create(project.id, name, pos);
                cm = new ColumnModel(name);
                cm.id = colId;
                cm.projectId = project.id;
                cm.position = pos;
            } else {
                cm = new ColumnModel(name); // fallback solo-memoria
                cm.position = project.columns.size();
            }

            project.columns.add(cm);
            refreshColumns();
            onModelChanged();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar la columna en BD:\n" + ex.getMessage());
        }
    }


    // Colores para fecha límite: rojo si vencida, naranja si ≤48h, verde si falta más
    private Color colorForDue(long dueMillis) {
        long now = System.currentTimeMillis();
        if (dueMillis < now) return new Color(220, 53, 69); // vencida
        long diff = dueMillis - now;
        long twoDays = 48L * 3600L * 1000L;
        if (diff <= twoDays) return new Color(255, 193, 7); // por vencer pronto
        return new Color(40, 167, 69); // aún hay tiempo
    }

    // === Construcción visual de una columna (conecta al modelo) ===
    private JPanel makeColumnPanel(final ColumnModel cm, String query) {
        JPanel col = new JPanel(new BorderLayout());
        col.setPreferredSize(new Dimension(300, 600));
        col.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        col.setBackground(Color.WHITE);
        col.setBorder(BorderFactory.createRaisedBevelBorder());

        // header (igual que tu versión original)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel(cm.title);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(25, 55, 109));

        JButton delete = new JButton("✕");
        delete.setForeground(Color.RED);
        delete.setBorderPainted(false);
        delete.setContentAreaFilled(false);
        delete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delete.setToolTipText("Eliminar columna");
        delete.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar columna y todas sus tareas?", "Confirmar",
                    JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    if (cm.id != null) new ColumnDAO().delete(cm.id);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar en BD:\n" + ex.getMessage());
                    return;
                }
                project.columns.remove(cm);
                refreshColumns();
                onModelChanged();
            }

        });

        header.add(title, BorderLayout.WEST);
        header.add(delete, BorderLayout.EAST);

        // tasks panel
        JPanel tasksPanel = new JPanel();
        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
        tasksPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(tasksPanel);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // botón agregar tarea
        JButton addTask = new JButton("+ Agregar Tarea");
        addTask.setBackground(new Color(0, 123, 255));
        addTask.setForeground(Color.WHITE);
        addTask.setBorderPainted(false);
        addTask.setFocusPainted(false);
        addTask.addActionListener(e -> addTaskFlow(cm, tasksPanel));

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        btnPanel.add(addTask);

        // montar
        col.add(header, BorderLayout.NORTH);
        col.add(scroll, BorderLayout.CENTER);
        col.add(btnPanel, BorderLayout.SOUTH);

        // pintar tareas del modelo (FILTRADAS)
        int matchesInColumn = 0;
        for (TaskModel tm : cm.tasks) {
            if (matches(tm, query)) {
                tasksPanel.add(makeTaskPanelFiltered(cm, tm, query));
                tasksPanel.add(Box.createVerticalStrut(10));
                matchesInColumn++;
            }
        }

        // Si no hay coincidencias, mostrar estado vacío en la columna
        if (matchesInColumn == 0) {
            JLabel empty = new JLabel(query == null || query.isEmpty()
                    ? "No hay tareas."
                    : "Sin coincidencias en esta columna.");
            empty.setForeground(new Color(140, 150, 160));
            empty.setBorder(new EmptyBorder(10, 10, 10, 10));
            tasksPanel.add(empty);
        }

        return col;
    }



    private void addTaskFlow(ColumnModel cm, JPanel tasksPanel) {
        JTextField title = new JTextField(25);
        JTextArea  desc  = new JTextArea(5, 25);
        JComboBox<String> prio = new JComboBox<>(new String[]{"Baja","Media","Alta"});

        // === NUEVO: selector de fecha límite ===
        JSpinner dueSpinner = new JSpinner(new SpinnerDateModel());
        dueSpinner.setEditor(new JSpinner.DateEditor(dueSpinner, "yyyy-MM-dd"));
        dueSpinner.setValue(new java.util.Date()); // hoy por defecto
        JCheckBox sinFecha = new JCheckBox("Sin fecha límite", false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6); gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx=0; gbc.gridy=0; form.add(new JLabel("Título:"), gbc);
        gbc.gridx=1; form.add(title, gbc);
        gbc.gridx=0; gbc.gridy=1; form.add(new JLabel("Descripción:"), gbc);
        gbc.gridx=1; form.add(new JScrollPane(desc), gbc);
        gbc.gridx=0; gbc.gridy=2; form.add(new JLabel("Prioridad:"), gbc);
        gbc.gridx=1; form.add(prio, gbc);
        gbc.gridx=0; gbc.gridy=3; form.add(new JLabel("Fecha límite:"), gbc);

        JPanel dueWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dueWrap.add(dueSpinner);
        dueWrap.add(Box.createHorizontalStrut(10));
        dueWrap.add(sinFecha);

        gbc.gridx=1; form.add(dueWrap, gbc);

        int ok = JOptionPane.showConfirmDialog(this, form, "Nueva tarea",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok == JOptionPane.OK_OPTION) {
            String t = title.getText().trim();
            if (t.isEmpty()) return;

            TaskModel tm = new TaskModel(t, desc.getText().trim(), (String) prio.getSelectedItem());
            if (!sinFecha.isSelected()) {
                java.util.Date d = (java.util.Date) dueSpinner.getValue();
                tm.dueAtMillis = d.getTime();
            } else {
                tm.dueAtMillis = 0L; // sin fecha
            }

            try {
                // === NUEVO: guardar en BD si la columna ya está en la BD ===
                if (cm.id != null) {
                    TaskDAO tdao = new TaskDAO();
                    Long tid = tdao.create(cm.id, tm);
                    tm.id = tid;
                    tm.columnId = cm.id;
                }
                // === Mantener en memoria y en UI como antes ===
                cm.tasks.add(tm);
                tasksPanel.add(makeTaskPanel(cm, tm));
                tasksPanel.add(Box.createVerticalStrut(10));
                tasksPanel.revalidate();
                tasksPanel.repaint();
                onModelChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar la tarea en BD:\n" + ex.getMessage());
            }
        }
    }


    // === Visual de una tarea ===
    private JPanel makeTaskPanel(ColumnModel cm, TaskModel tm) {
        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setPreferredSize(new Dimension(280, 130));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(priorityColor(tm.priority), 3),
                new EmptyBorder(10,10,10,10)
        ));

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel(tm.title);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(33,37,41));

        String truncated = tm.description != null && tm.description.length() > 50
                ? tm.description.substring(0,50) + "..." : (tm.description == null ? "" : tm.description);
        JLabel desc = new JLabel("<html>"+truncated+"</html>");
        desc.setFont(new Font("Arial", Font.PLAIN, 12));
        desc.setForeground(Color.GRAY);

        // Línea de info: prioridad, fecha creación y (si aplica) fecha límite
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        info.setBackground(Color.WHITE);

        JLabel pr = new JLabel("● " + tm.priority);
        pr.setForeground(priorityColor(tm.priority));
        pr.setFont(new Font("Arial", Font.BOLD, 11));

        String created = DateTimeFormatter.ofPattern("dd/MM HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(tm.createdAtMillis));
        JLabel dt = new JLabel("| " + created);
        dt.setFont(new Font("Arial", Font.PLAIN, 10));
        dt.setForeground(Color.GRAY);

        info.add(pr);
        info.add(dt);

        if (tm.dueAtMillis > 0) {
            String dueStr = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(tm.dueAtMillis));
            JLabel due = new JLabel("| ⏰ " + dueStr);
            due.setFont(new Font("Arial", Font.BOLD, 11));
            due.setForeground(colorForDue(tm.dueAtMillis));
            due.setToolTipText("Fecha límite");
            info.add(due);
        }

        // Panel vertical con título, descripción e info
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(desc);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(info);

        JButton del = new JButton("✕");
        del.setForeground(Color.RED);
        del.setBorderPainted(false);
        del.setContentAreaFilled(false);
        del.setCursor(new Cursor(Cursor.HAND_CURSOR));
        del.setToolTipText("Eliminar tarea");
        del.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(
                    this, "¿Eliminar tarea?", "Confirmar", JOptionPane.YES_NO_OPTION
            );
            if (opt != JOptionPane.YES_OPTION) return;

            // 1) Eliminar en BD si la tarea existe allí
            try {
                if (tm.id != null) {
                    new TaskDAO().delete(tm.id);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this, "No se pudo eliminar la tarea en BD:\n" + ex.getMessage()
                );
                return; // No tocar UI si falló la BD
            }

            // 2) Eliminar de memoria
            cm.tasks.remove(tm);

            // 3) Eliminar de UI (y quitar el filler si está justo después)
            Container parent = card.getParent(); // tasksPanel
            if (parent != null) {
                int idx = -1;
                for (int i = 0; i < parent.getComponentCount(); i++) {
                    if (parent.getComponent(i) == card) { idx = i; break; }
                }
                if (idx != -1) {
                    parent.remove(idx); // quita la tarjeta
                    if (idx < parent.getComponentCount() &&
                            parent.getComponent(idx) instanceof Box.Filler) {
                        parent.remove(idx); // quita separador
                    }
                }
                parent.revalidate();
                parent.repaint();
            }

            // 4) Notificar cambio de modelo (persistirá visual si tienes onModelChanged() → DAO)
            onModelChanged();
        });

        // hover
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { card.setBackground(new Color(248,249,250)); }
            public void mouseExited (java.awt.event.MouseEvent evt) { card.setBackground(Color.WHITE); }
        });

        content.add(textPanel, BorderLayout.CENTER);
        content.add(del, BorderLayout.EAST);
        card.add(content, BorderLayout.CENTER);
        return card;
    }


    private Color priorityColor(String p) {
        switch (p) {
            case "Alta":  return new Color(220, 53, 69);
            case "Media": return new Color(255, 193, 7);
            case "Baja":  return new Color(40, 167, 69);
            default:      return Color.GRAY;
        }
    }

    // ===================== CHAT PANEL =====================
    private void buildChatPanel() {
        chatPanel = new JPanel(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(340, 0));
        chatPanel.setBackground(new Color(28, 33, 41));
        chatPanel.setBorder(BorderFactory.createMatteBorder(0,1,0,0,new Color(54,61,73)));

        // Header del chat
        JLabel lbl = new JLabel("  Comentarios");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setBorder(new EmptyBorder(10,10,10,10));
        chatPanel.add(lbl, BorderLayout.NORTH);

        // Lista de mensajes
        chatMessages = new JPanel();
        chatMessages.setLayout(new BoxLayout(chatMessages, BoxLayout.Y_AXIS));
        chatMessages.setBackground(new Color(28, 33, 41));

        // Cargar mensajes existentes
        for (CommentModel c : project.comments) {
            chatMessages.add(renderMessage(c));
            chatMessages.add(Box.createVerticalStrut(8));
        }

        chatScroll = new JScrollPane(chatMessages,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScroll.setBorder(null);
        chatScroll.getViewport().setBackground(new Color(28, 33, 41));
        chatScroll.getVerticalScrollBar().setUnitIncrement(12);

        chatPanel.add(chatScroll, BorderLayout.CENTER);

        // Input
        JPanel input = new JPanel(new BorderLayout(8,8));
        input.setBorder(new EmptyBorder(10,10,10,10));
        input.setBackground(new Color(28, 33, 41));
        JTextField txt = new JTextField();
        JButton send = new JButton("Enviar");
        send.setBackground(new Color(59,130,246));
        send.setForeground(Color.WHITE);
        send.setBorderPainted(false);
        send.setFocusPainted(false);

        Runnable doSend = () -> {
            String s = txt.getText().trim();
            if (s.isEmpty()) return;
            CommentModel c = new CommentModel(currentUserDisplay, s);
            project.comments.add(c);
            chatMessages.add(renderMessage(c));
            chatMessages.add(Box.createVerticalStrut(8));
            chatMessages.revalidate();
            chatMessages.repaint();
            scrollChatToBottom();
            txt.setText("");
            onModelChanged();
        };

        send.addActionListener(e -> doSend.run());
        txt.addActionListener(e -> doSend.run()); // Enter para enviar

        input.add(txt, BorderLayout.CENTER);
        input.add(send, BorderLayout.EAST);
        chatPanel.add(input, BorderLayout.SOUTH);
    }

    private JComponent renderMessage(CommentModel c) {
        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setBackground(new Color(42, 49, 60));
        bubble.setBorder(new EmptyBorder(8,10,8,10));

        String when = DateTimeFormatter.ofPattern("dd/MM HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(c.createdAtMillis));

        JLabel meta = new JLabel(c.author + " • " + when);
        meta.setForeground(new Color(210, 220, 235));
        meta.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel text = new JLabel("<html>"+escapeHtml(c.text)+"</html>");
        text.setForeground(new Color(230,236,245));
        text.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(new Color(42,49,60));
        wrap.add(meta);
        wrap.add(Box.createVerticalStrut(4));
        wrap.add(text);

        bubble.add(wrap, BorderLayout.CENTER);
        return bubble;
    }

    private void scrollChatToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private String escapeHtml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }

    private void toggleChat() {
        chatVisible = !chatVisible;
        if (chatVisible) {
            getContentPane().add(chatPanel, BorderLayout.EAST);
        } else {
            getContentPane().remove(chatPanel);
        }
        revalidate();
        repaint();
    }
}

