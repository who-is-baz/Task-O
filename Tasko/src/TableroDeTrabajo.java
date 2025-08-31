import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TableroDeTrabajo extends JFrame {

    private JPanel PanelPrincipal;


    // Vistas para el CardLayout central
    private static final String VIEW_TABLEROS   = "TABLEROS";
    private static final String VIEW_PLANTILLAS = "PLANTILLAS";
    private static final String VIEW_INICIO     = "INICIO";

    // Paleta de colores para los tableros
    private static final Color[] BOARD_COLORS = {
            new Color(244, 67, 54),   // Rojo
            new Color(33, 150, 243),  // Azul
            new Color(76, 175, 80),   // Verde
            new Color(255, 193, 7),   // Amarillo
            new Color(156, 39, 176)   // Morado
    };

    // Área central
    private JPanel contentCards;

    // Panel de tableros y su scroll (para crear cards dinámicamente)
    private JPanel panelTableros;
    private JScrollPane scrollTableros;

    // Datos de usuario
    private final String userEmail;
    private final String userName; //
    private long userId; //


    // --- Constructores ---
// --- Constructores ---
// Conserva los antiguos
    // Constructor viejo (email solo)
    public TableroDeTrabajo(String userEmail) {
        this(userEmail, null);
    }

    public TableroDeTrabajo(String userEmail, String userName) {
        this.userEmail = userEmail;
        this.userName  = userName;
        this.userId    = -1;               // default
        InicioTablero();
        // Asegura id si vienes por este constructor
        ensureUserId();
        MostrarTablero();
    }

    // Constructor nuevo (id + email) — se mantiene igual
    public TableroDeTrabajo(long userId, String userEmail) {
        this.userId    = userId;
        this.userEmail = userEmail;
        this.userName  = null;
        InicioTablero();
        MostrarTablero();
    }

    // Helper:
    private boolean ensureUserId() {
        if (userId > 0) return true;
        try {
            Long id = new UserDAO().findIdByEmail(userEmail);
            if (id != null) {
                userId = id;
                return true;
            }
        } catch (Exception ignored) {}
        return false; // no se pudo resolver
    }





    private void InicioTablero(){
        setTitle("Tablero de trabajo");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1500, 800);
        setLocationRelativeTo(null);
        PanelPrincipal = new JPanel(new BorderLayout());
        setContentPane(PanelPrincipal);
    }

    private void MostrarTablero(){
        // ====== Paleta de colores ======
        final Color BG            = new Color(20, 24, 28);   // fondo base
        final Color SURFACE       = new Color(24, 28, 36);   // header
        final Color SURFACE_DARK  = new Color(18, 22, 28);   // sidebar
        final Color DIVIDER       = new Color(45, 52, 60);   // separadores
        final Color CANVAS        = new Color(13, 17, 23);   // lienzo central
        final Color TEXT          = new Color(235, 245, 255, 210);
        final Color HOVER_BG      = new Color(52, 61, 72, 120);

        // ====== Shell ======
        JPanel PnlFondo = new JPanel(new BorderLayout());
        PnlFondo.setBackground(BG);
        PanelPrincipal.add(PnlFondo, BorderLayout.CENTER);

        // ====== Header ======
        JPanel PnlSuperior = new JPanel(new BorderLayout(12, 0));
        PnlSuperior.setPreferredSize(new Dimension(0, 56));
        PnlSuperior.setBackground(SURFACE);
        PnlSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER));

        // Izquierda: título
        JLabel lblTitulo = new JLabel("   Proyecto • Tablero");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 14f));
        PnlSuperior.add(lblTitulo, BorderLayout.WEST);

        // Centro: buscador (placeholder simple)
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        JTextField txtSearch = new JTextField();
        txtSearch.setBorder(new EmptyBorder(8,12,8,12));
        searchWrap.add(txtSearch, BorderLayout.CENTER);
        searchWrap.setBorder(new EmptyBorder(8, 0, 8, 0));
        PnlSuperior.add(searchWrap, BorderLayout.CENTER);

        // Derecha: botón crear + avatar con menú
        JPanel userWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        userWrap.setOpaque(false);

        // === Botón "Crear tablero" ===
        JButton btnCrear = new JButton("Crear tablero");
        btnCrear.addActionListener(e -> {
            // --- diálogo simple: nombre + paleta de colores ---
            JTextField txtNombre = new JTextField(20);

            JPanel paleta = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            ButtonGroup group = new ButtonGroup();
            final Color[] elegido = { BOARD_COLORS[0] }; // valor por defecto

            for (Color c : BOARD_COLORS) {
                JToggleButton b = new JToggleButton();
                b.setPreferredSize(new Dimension(28, 28));
                b.setBackground(c);
                b.setOpaque(true);
                b.setBorder(BorderFactory.createLineBorder(new Color(230,230,230,120)));
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.addActionListener(ae -> elegido[0] = c);
                group.add(b);
                paleta.add(b);
            }
            // marca el primero por defecto
            ((JToggleButton) paleta.getComponent(0)).setSelected(true);

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6,6,6,6);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx=0; gbc.gridy=0; form.add(new JLabel("Nombre del tablero:"), gbc);
            gbc.gridx=1; form.add(txtNombre, gbc);
            gbc.gridx=0; gbc.gridy=1; form.add(new JLabel("Color:"), gbc);
            gbc.gridx=1; form.add(paleta, gbc);

            int ok = JOptionPane.showConfirmDialog(this, form, "Nuevo tablero",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (ok == JOptionPane.OK_OPTION) {
                String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.");
                    return;
                }

                // Asegura tener userId válido (por si entraste por el ctor que solo recibe email)
                // 1) PERSISTIR EN BD (por usuario)
                if (!ensureUserId()) {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo identificar tu usuario. Vuelve a iniciar sesión.",
                            "Usuario no resuelto", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    ProjectDAO pdao = new ProjectDAO();
                    Long pid = pdao.create(userId, nombre, elegido[0].getRGB(), null, null);

                    // Actualizar/crear el modelo en memoria con IDs de BD
                    ProjectModel model = ProjectStore.get().getOrCreate(nombre);
                    model.id = pid;
                    model.ownerId = userId;
                    model.uiColorRgb = elegido[0].getRGB();
                    ProjectStore.get().saveSnapshot(model); // mantiene caché en memoria

                } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
                    JOptionPane.showMessageDialog(this, "Ya tienes un tablero con ese nombre. Elige otro.");
                    return;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo crear el proyecto en BD:\n" + ex.getMessage());
                    return;
                }


                // 2) ACTUALIZAR LA UI (igual que ya lo hacías)
                if (panelTableros.getComponentCount() == 1 &&
                        panelTableros.getComponent(0) instanceof JLabel) {
                    panelTableros.removeAll();
                }
                panelTableros.add(makeCard(nombre, elegido[0]));
                panelTableros.revalidate();
                panelTableros.repaint();
            }
        });




        // Avatar a partir del correo/nombre
        String initials = computeInitials(userEmail, userName);   // p. ej. "JM"
        Color  avatarBg = colorFromString(userEmail);             // color estable por email
        AvatarLabel avatar = new AvatarLabel(initials, avatarBg);
        avatar.setToolTipText(userEmail);

        // Menú emergente tipo Trello
        JPopupMenu menu = buildUserMenu(userEmail, userName);
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                menu.show(avatar, 0, avatar.getHeight());
            }
        });

        userWrap.add(btnCrear);
        userWrap.add(avatar);
        PnlSuperior.add(userWrap, BorderLayout.EAST);
        PnlFondo.add(PnlSuperior, BorderLayout.NORTH);

        // ====== Sidebar ======
        JPanel PnlMenuLateral = new JPanel();
        PnlMenuLateral.setPreferredSize(new Dimension(260, 0));
        PnlMenuLateral.setBackground(SURFACE_DARK);
        PnlMenuLateral.setLayout(new BoxLayout(PnlMenuLateral, BoxLayout.Y_AXIS));
        PnlMenuLateral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, DIVIDER),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel JlbTableros   = makeNavItem("Tableros", TEXT, HOVER_BG);
        JLabel JlbPlantillas = makeNavItem("Plantillas", TEXT, HOVER_BG);
        JLabel JblInicio     = makeNavItem("Inicio", TEXT, HOVER_BG);

        JlbTableros.setAlignmentX(Component.LEFT_ALIGNMENT);
        JlbPlantillas.setAlignmentX(Component.LEFT_ALIGNMENT);
        JblInicio.setAlignmentX(Component.LEFT_ALIGNMENT);

        PnlMenuLateral.add(JlbTableros);
        PnlMenuLateral.add(Box.createVerticalStrut(8));
        PnlMenuLateral.add(JlbPlantillas);
        PnlMenuLateral.add(Box.createVerticalStrut(8));
        PnlMenuLateral.add(JblInicio);

        PnlFondo.add(PnlMenuLateral, BorderLayout.WEST);

        // ====== Content con CardLayout ======
        contentCards = new JPanel(new CardLayout());
        contentCards.setBackground(CANVAS);
        contentCards.setBorder(new EmptyBorder(24,24,24,24));

// ====== Panel TABLEROS (4 por fila) + scroll ======
        panelTableros = new JPanel(new GridLayout(0, 4, 20, 20)); // 0 filas = dinámicas, 4 columnas fijas
        panelTableros.setOpaque(true);
        panelTableros.setBackground(CANVAS);

// Estado vacío (ocupa 1 celda hasta que agregues el primer tablero)
        JLabel emptyState = new JLabel("Aún no tienes tableros. Crea el primero con “Crear tablero”.");
        emptyState.setForeground(new Color(180, 190, 200));
        emptyState.setHorizontalAlignment(SwingConstants.CENTER);
        panelTableros.add(emptyState);

// Scroll SOLO vertical + desplazamiento más controlado
        scrollTableros = new JScrollPane(
                panelTableros,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollTableros.setBorder(null);
        scrollTableros.setBackground(CANVAS);
        scrollTableros.getViewport().setBackground(CANVAS);
// Ajusta la “sensibilidad” del scroll: menos salto -> más control
        scrollTableros.getVerticalScrollBar().setUnitIncrement(12);   // paso pequeño con rueda
        scrollTableros.getVerticalScrollBar().setBlockIncrement(140); // PageUp/Down

// ====== Panel PLANTILLAS (nuevo, estilo Trello) ======
        JPanel panelPlantillas = new PanelPlantillas(this::abrirDesdePlantilla);
        panelPlantillas.setOpaque(false);

// Panel INICIO
        JPanel panelInicio = new JPanel();
        panelInicio.setOpaque(false);
        panelInicio.add(new JLabel("Bienvenido al inicio.", SwingConstants.CENTER));

        contentCards.add(scrollTableros, VIEW_TABLEROS);
        contentCards.add(panelPlantillas, VIEW_PLANTILLAS);
        contentCards.add(panelInicio, VIEW_INICIO);

        PnlFondo.add(contentCards, BorderLayout.CENTER);


        // Navegación del sidebar
        JlbTableros.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showView(VIEW_TABLEROS); }
        });
        JlbPlantillas.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showView(VIEW_PLANTILLAS); }
        });
        JblInicio.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showView(VIEW_INICIO); }
        });

        // Vista inicial
        showView(VIEW_PLANTILLAS);

        PanelPrincipal.revalidate();
        PanelPrincipal.repaint();
        cargarProyectosDesdeBD();
    }

    private void cargarProyectosDesdeBD() {
        panelTableros.removeAll();

        if (!ensureUserId()) {
            // Estado vacío amigable si por algún motivo no encontramos el id
            JLabel empty = new JLabel("Aún no tienes tableros. Crea el primero con “Crear tablero”.");
            empty.setForeground(new Color(180,190,200));
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            panelTableros.add(empty);
            panelTableros.revalidate();
            panelTableros.repaint();
            return;
        }

        try {
            ProjectDAO pdao = new ProjectDAO();
            java.util.List<ProjectModel> projects = pdao.listByOwner(userId);
            if (projects.isEmpty()) {
                JLabel empty = new JLabel("Aún no tienes tableros. Crea el primero con “Crear tablero”.");
                empty.setForeground(new Color(180,190,200));
                empty.setHorizontalAlignment(SwingConstants.CENTER);
                panelTableros.add(empty);
            } else {
                for (ProjectModel pm : projects) {
                    Color col = (pm.uiColorRgb != null) ? new Color(pm.uiColorRgb) : new Color(33,150,243);
                    panelTableros.add(makeCard(pm.name, col));
                }
            }
        } catch (Exception ex) {
            // No bloquees: muestra estado vacío en vez de impedir uso
            JLabel empty = new JLabel("No se pudieron cargar tus tableros. Puedes crear uno nuevo.");
            empty.setForeground(new Color(180,70,70));
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            panelTableros.add(empty);
            // (opcional) log con ex.printStackTrace();
        }

        panelTableros.revalidate();
        panelTableros.repaint();
    }



    /** Callback: al elegir una plantilla desde la vista Plantillas */
    private void abrirDesdePlantilla(BoardTemplate plantilla) {
        // Crea/obtiene el proyecto a partir del nombre de la plantilla
        String projectName = plantilla.displayName + " Board";
        ProjectModel project = ProjectStore.get().getOrCreate(projectName);
        project.templateId = plantilla.id;
        // Crea columnas por defecto si está vacío
        if (project.columns.isEmpty() && plantilla.defaultColumns != null) {
            for (String c : plantilla.defaultColumns) {
                project.columns.add(new ColumnModel(c));
            }
        }
        // Abre el tablero real (TaskBoardFrame ya soporta templateId/fondo)
        new TaskBoardFrame(project).setVisible(true);
    }

    /** Crea un item de menú con hover y cursor de mano */
    private JLabel makeNavItem(String text, Color textColor, Color hoverBg) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(textColor);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(0,0,0,0)); // transparente
        lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { lbl.setBackground(hoverBg); }
            @Override public void mouseExited (MouseEvent e) { lbl.setBackground(new Color(0,0,0,0)); }
        });
        return lbl;
    }

    /** Muestra una vista del CardLayout por clave */
    private void showView(String key) {
        CardLayout cl = (CardLayout) contentCards.getLayout();
        cl.show(contentCards, key);
    }

    /** Agrega una tarjeta de proyecto y refresca el panel */
    private void addProjectCard(String title) {
        // Si el único componente era el mensaje vacío, lo quitamos
        if (panelTableros.getComponentCount() == 1 &&
                panelTableros.getComponent(0) instanceof JLabel) {
            panelTableros.removeAll();
        }
        panelTableros.add(makeCard(title));
        panelTableros.revalidate();
        panelTableros.repaint();
    }

    /** Card de proyecto (abre su tablero al hacer clic) */
    private JComponent makeCard(String title, Color headerColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(260, 160));
        card.setBackground(new Color(28, 33, 41));
        card.setOpaque(true);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(54, 61, 73)),
                new EmptyBorder(6,6,6,6)
        ));


        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(260, 110));
        header.setBackground(headerColor);
        header.setOpaque(true);
        card.add(header, BorderLayout.CENTER);

        JLabel lbl = new JLabel(title);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 14f));
        lbl.setBorder(new EmptyBorder(6,6,6,6));
        card.add(lbl, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(86, 95, 110)),
                        new EmptyBorder(6,6,6,6)
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(54, 61, 73)),
                        new EmptyBorder(6,6,6,6)
                ));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                openProjectBoard(title);
            }

        });

        return card;
    }
    // Sobrecarga para llamadas antiguas: elige un color por defecto/estable
    private JComponent makeCard(String title) {
        // usa un color estable por nombre o uno aleatorio si prefieres
        Color fallback = new Color(33,150,243); // o stableColor(title) si tienes ese helper
        return makeCard(title, fallback);
    }
    // Crear tablero desde una plantilla BASE


    // Crear tablero desde una plantilla PERSONALIZADA
    private void crearTableroDesdeCustomTemplate(CustomTemplates.SimpleTemplate s) {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo tablero:", s.displayName);
        if (nombre == null) return;
        nombre = nombre.trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.");
            return;
        }
        if (!ensureUserId()) {
            JOptionPane.showMessageDialog(this, "No se pudo identificar tu usuario. Vuelve a iniciar sesión.");
            return;
        }

        try {
            ProjectDAO pdao = new ProjectDAO();
            Integer rgb = s.headerColor == null ? null : s.headerColor.getRGB();
            // Guardamos el ID de la plantilla personalizada (s.id) y su fondo local
            Long pid = pdao.create(userId, nombre, rgb, s.id, s.backgroundPath);

            // Actualiza caché
            ProjectModel model = ProjectStore.get().getOrCreate(nombre);
            model.id = pid;
            model.ownerId = userId;
            model.uiColorRgb = rgb;
            model.templateId = s.id;
            model.backgroundPath = s.backgroundPath;
            ProjectStore.get().saveSnapshot(model);

            // Dibuja card
            if (panelTableros.getComponentCount() == 1 && panelTableros.getComponent(0) instanceof JLabel)
                panelTableros.removeAll();
            panelTableros.add(makeCard(nombre, s.headerColor));
            panelTableros.revalidate(); panelTableros.repaint();

        } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
            JOptionPane.showMessageDialog(this, "Ya tienes un tablero con ese nombre. Elige otro.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo crear el tablero desde plantilla:\n" + ex.getMessage());
        }
    }




    /** Abre (o crea) la ventana del tablero de tareas para un proyecto */
    private void openProjectBoard(String projectName) {
        try {
            ProjectDAO pdao = new ProjectDAO();
            for (ProjectModel pm : pdao.listByOwner(userId)) {
                if (pm.name.equalsIgnoreCase(projectName)) {
                    new TaskBoardFrame(pm).setVisible(true);
                    return;
                }
            }
            // Fallback si no se encontró (no debería pasar si todo persiste bien)
            ProjectModel model = ProjectStore.get().getOrCreate(projectName);
            model.ownerId = userId;
            new TaskBoardFrame(model).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el tablero:\n" + ex.getMessage());
        }
    }



    /** Etiqueta circular con iniciales (avatar) */
    private static class AvatarLabel extends JLabel {
        private final Color bg;
        AvatarLabel(String initials, Color bg) {
            super(initials, SwingConstants.CENTER);
            this.bg = bg;
            setForeground(Color.WHITE);
            setFont(getFont().deriveFont(Font.BOLD, 13f));
            setPreferredSize(new Dimension(28, 28));
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(bg);
            g2.fillOval(0, 0, w, h);
            FontMetrics fm = g2.getFontMetrics(getFont());
            String text = getText();
            int x = (w - fm.stringWidth(text)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(getForeground());
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }

    /** Devuelve iniciales: por nombre si existe; si no, por correo (parte local) */
    private String computeInitials(String email, String name) {
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length == 1) return parts[0].substring(0,1).toUpperCase();
            return (parts[0].substring(0,1) + parts[parts.length-1].substring(0,1)).toUpperCase();
        }
        String local = (email != null ? email : "").split("@")[0];
        if (local.isEmpty()) return "U";
        String[] parts = local.split("[._-]+");
        if (parts.length == 1) {
            return local.substring(0, Math.min(2, local.length())).toUpperCase();
        }
        return (parts[0].substring(0,1) + parts[1].substring(0,1)).toUpperCase();
    }

    /** Color de avatar determinístico según el string (hash) */
    private Color colorFromString(String s) {
        if (s == null) s = "";
        int h = s.hashCode();
        float hue = (h & 0xFFFFFF) / (float)0xFFFFFF; // 0..1
        return Color.getHSBColor(hue, 0.55f, 0.75f);
    }

    /** Construye el menú del usuario (popup) */
    private JPopupMenu buildUserMenu(String email, String name) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(new EmptyBorder(8,8,8,8));

        // Cabecera
        JPanel header = new JPanel(new BorderLayout(8,0));
        header.setOpaque(false);
        AvatarLabel mini = new AvatarLabel(computeInitials(email, name), colorFromString(email));
        mini.setPreferredSize(new Dimension(28,28));
        header.add(mini, BorderLayout.WEST);

        String display = (name != null && !name.isBlank()) ? name : email;
        JLabel title = new JLabel("<html><b>" + display + "</b><br><span style='font-size:10px;color:#9aa5b1;'>"
                + email + "</span></html>");
        header.add(title, BorderLayout.CENTER);

        menu.add(header);
        menu.addSeparator();

        // Opciones (placeholders)
        menu.add(makeMenuItem("Perfil y visibilidad", () -> info("Abrir Perfil")));
        menu.add(makeMenuItem("Actividad", () -> info("Ver Actividad")));
        menu.add(makeMenuItem("Tarjetas", () -> info("Ver Tarjetas")));
        menu.add(makeMenuItem("Ajustes", () -> info("Abrir Ajustes")));

        JMenu tema = new JMenu("Tema");
        tema.add(makeMenuItem("Claro",  () -> info("Tema claro")));
        tema.add(makeMenuItem("Oscuro", () -> info("Tema oscuro")));
        menu.add(tema);

        menu.addSeparator();
        menu.add(makeMenuItem("Crear Espacio de trabajo", () -> info("Crear workspace")));
        menu.add(makeMenuItem("Ayuda", () -> info("Abrir Ayuda")));
        menu.add(makeMenuItem("Accesos directos", () -> info("Ver accesos")));
        menu.addSeparator();

        // Confirmación para cerrar sesión
        menu.add(makeMenuItem("Cerrar sesión", () -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Seguro que quieres cerrar sesión?",
                    "Confirmar cierre de sesión",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                dispose(); // cierra el tablero
                new InicioSesion().setVisible(true);
            }
        }));

        return menu;
    }

    /** Crea un JMenuItem con acción tipo Runnable */
    private JMenuItem makeMenuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        return item;
    }

    /** Utilidad para demo */
    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    // --- main de prueba (puedes borrar si llamas desde el login) ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TableroDeTrabajo("jose.mendoza@example.com").setVisible(true));
    }
}

/* =====================  CLASES AUXILIARES PARA LA VISTA PLANTILLAS  ===================== */


/** Vista de Plantillas con categorías, buscador y tarjetas tipo Trello. */
class PanelPlantillas extends JPanel {
    private static final int COLS = 3;          // 3 columnas fijas
    private static final int CARD_W = 320;      // ancho aprox. de cada tarjeta
    private static final int CARD_H = 180;      // alto aprox. de cada tarjeta
    private static final int GAP    = 16;       // separación entre tarjetas
    private final JTextField campoBusqueda;
    private final JPanel gridCards;
    private List<BoardTemplate> data;
    private String categoriaActiva = null; // null = todas
    private final Consumer<BoardTemplate> onSelect;

    PanelPlantillas(Consumer<BoardTemplate> onSelect) {
        this.onSelect = onSelect;
        setLayout(new BorderLayout());
        setBackground(new Color(26, 32, 40));

// =================== HEADER (título + buscador) ===================
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 24, 10, 24));

        JLabel titulo = new JLabel("Plantillas");
        titulo.setForeground(new Color(224, 231, 255));
        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel buscar = new JPanel(new BorderLayout());
        buscar.setOpaque(false);
        campoBusqueda = new JTextField();
        campoBusqueda.setToolTipText("Buscar plantilla");
        campoBusqueda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 85, 104)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        campoBusqueda.setPreferredSize(new Dimension(320, 36));
        buscar.add(campoBusqueda, BorderLayout.EAST);

        header.add(titulo, BorderLayout.WEST);
        header.add(buscar, BorderLayout.EAST);

// =================== CATEGORÍAS (chips) ===================
        JPanel categorias = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        categorias.setOpaque(false);
        categorias.setBorder(new EmptyBorder(0, 24, 0, 24));
        String[] cats = {"Negocio","Diseño","Educación","Ingeniería","Marketing",
                "Gestión de Proyectos","Trabajo a distancia","Productividad"};
        for (String c : cats) categorias.add(chipCategoria(c));

// === Agrupa header + categorias arriba (NORTH) ===
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(header);
        top.add(Box.createVerticalStrut(8));
        top.add(categorias);
        add(top, BorderLayout.NORTH);

// =================== CONTENIDO (CENTER) ===================
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(16, 24, 24, 24));

        JLabel subtitulo = new JLabel("Plantillas nuevas y fantásticas");
        subtitulo.setForeground(new Color(210, 220, 235));
        subtitulo.setFont(new Font("Arial", Font.BOLD, 16));
        subtitulo.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Antes: gridCards = new JPanel(new WrapLayout(...));
// AHORA:
        gridCards = new JPanel(new GridBagLayout());
        gridCards.setOpaque(false);


// **Scroll más suave y sin barra horizontal**
        JScrollPane scroll = new JScrollPane(gridCards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(new Color(26, 32, 40));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // solo vertical
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(12);   // paso corto
        scroll.getVerticalScrollBar().setBlockIncrement(140); // PageUp/Down


        contenido.add(subtitulo, BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);

// ¡IMPORTANTE! el contenido va en CENTER, no en SOUTH
        add(contenido, BorderLayout.CENTER);

// Resto: cargar data y refrescar
        data = BoardTemplate.all();
        campoBusqueda.getDocument().addDocumentListener(new SimpleDocumentListener(this::refrescar));
        refrescar();
    }

    /** Adjunta un popup "Crear tablero…" a la tarjeta dada. */
    private void attachCrearTableroPopup(JComponent target, BoardTemplate t) {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem miCrear = new JMenuItem("Crear tablero…");
        miCrear.addActionListener(e -> onSelect.accept(t)); // reutiliza el flujo existente
        menu.add(miCrear);

        // Soporte básico
        target.setComponentPopupMenu(menu);

        // Soporte robusto cross-platform
        target.addMouseListener(new MouseAdapter() {
            private void maybeShow(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    menu.show(target, e.getX(), e.getY());
                }
            }
            @Override public void mousePressed(MouseEvent e)  { maybeShow(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShow(e); }
        });
    }


    private JButton chipCategoria(String nombre) {
        JButton b = new JButton(nombre);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setForeground(new Color(224, 231, 255));
        b.setBackground(new Color(44, 51, 63));
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        b.addActionListener(e -> {
            // toggle categoría
            if (nombre.equals(categoriaActiva)) categoriaActiva = null;
            else categoriaActiva = nombre;
            refrescar();
        });
        return b;
    }

    private void refrescar() {
        String q = campoBusqueda.getText() == null ? "" : campoBusqueda.getText().trim().toLowerCase();
        java.util.List<BoardTemplate> filtradas = data.stream()
                .filter(t -> q.isEmpty() || t.displayName.toLowerCase().contains(q))
                .filter(t -> categoriaActiva == null || (t.category != null && t.category.equalsIgnoreCase(categoriaActiva)))
                .collect(java.util.stream.Collectors.toList());

        gridCards.removeAll();

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(GAP/2, GAP/2, GAP/2, GAP/2);
        gbc.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;

        for (int i = 0; i < filtradas.size(); i++) {
            BoardTemplate t = filtradas.get(i);
            int col = i % COLS;
            int row = i / COLS;

            TarjetaPlantilla card = new TarjetaPlantilla(t, onSelect);

            card.setPreferredSize(new Dimension(CARD_W, CARD_H)); // fija tamaño visual

// <<< AÑADIDO: popup "Crear tablero…" por tarjeta
            attachCrearTableroPopup(card, t);

            card.setPreferredSize(new Dimension(CARD_W, CARD_H)); // fija tamaño visual

            gbc.gridx = col;
            gbc.gridy = row;
            gbc.weightx = 0;                  // que no estiren
            gbc.fill = java.awt.GridBagConstraints.NONE;
            gridCards.add(card, gbc);
        }

        // Filler elástico para empujar hacia arriba y permitir scroll cómodo
        gbc.gridx = 0;
        gbc.gridy = (filtradas.size() + COLS - 1) / COLS;
        gbc.gridwidth = COLS;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gridCards.add(Box.createGlue(), gbc);

        gridCards.revalidate();
        gridCards.repaint();
    }

}

/** Tarjeta de plantilla con preview e botón "Usar esta plantilla" */
class TarjetaPlantilla extends JPanel {
    TarjetaPlantilla(BoardTemplate t, Consumer<BoardTemplate> onSelect) {
        setPreferredSize(new Dimension(280, 150));
        setLayout(new BorderLayout());
        setBackground(new Color(42, 49, 60));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(64, 72, 86)),
                new EmptyBorder(10,10,10,10)
        ));

        // Preview
        JLabel preview = new JLabel();
        preview.setOpaque(true);
        preview.setBackground(new Color(52, 61, 75));
        preview.setPreferredSize(new Dimension(260, 80));
        Image img = cargarDesdeRecurso(t.resourceBackground);
        if (img != null) {
            Image scaled = img.getScaledInstance(260, 80, Image.SCALE_SMOOTH);
            preview.setIcon(new ImageIcon(scaled));
        }
        add(preview, BorderLayout.NORTH);

        // Título + botón
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        JLabel titulo = new JLabel(t.displayName);
        titulo.setForeground(new Color(230, 236, 245));
        titulo.setFont(new Font("Arial", Font.BOLD, 14));
        JButton usar = new JButton("Usar esta plantilla");
        usar.setForeground(Color.WHITE);
        usar.setBackground(new Color(59,130,246));
        usar.setBorderPainted(false);
        usar.setFocusPainted(false);
        usar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        usar.addActionListener(e -> onSelect.accept(t));

        abajo.add(titulo, BorderLayout.WEST);
        abajo.add(usar, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);
    }

    private Image cargarDesdeRecurso(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            return (url != null) ? new ImageIcon(url).getImage() : null;
        } catch (Exception e) { return null; }
    }
}

/** Listener simple para campos de texto (evita boilerplate). */
class SimpleDocumentListener implements javax.swing.event.DocumentListener {
    private final Runnable r;
    public SimpleDocumentListener(Runnable r){ this.r = r; }
    public void insertUpdate(javax.swing.event.DocumentEvent e){ r.run(); }
    public void removeUpdate(javax.swing.event.DocumentEvent e){ r.run(); }
    public void changedUpdate(javax.swing.event.DocumentEvent e){ r.run(); }
}

/** FlowLayout que hace “wrap” a los componentes (para el grid de tarjetas). */
class WrapLayout extends FlowLayout {
    public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
    @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
    @Override public Dimension minimumLayoutSize(Container target) { return layoutSize(target, false); }
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getWidth() == 0 ? Integer.MAX_VALUE : target.getWidth();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - insets.left - insets.right - getHgap()*2;
            int x = 0, y = getVgap(), rowHeight = 0;
            for (Component c : target.getComponents()) {
                if (!c.isVisible()) continue;
                Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                if (x == 0 || x + d.width <= maxWidth) {
                    x += d.width + getHgap();
                    rowHeight = Math.max(rowHeight, d.height);
                } else {
                    y += rowHeight + getVgap();
                    x = d.width + getHgap();
                    rowHeight = d.height;
                }
            }
            y += rowHeight + getVgap();
            return new Dimension(targetWidth, y + insets.top + insets.bottom);
        }
    }
}

/** Catálogo de plantillas con categoría, color de header y fondo por recurso. */
class BoardTemplate {
    public final String id;
    public final String displayName;
    public final String category;           // para filtrar
    public final String resourceBackground; // "/resources/plantillas/xxx.jpg"
    public final Color headerColor;
    public final java.util.List<String> defaultColumns;

    public BoardTemplate(String id, String name, String category, String resBg,
                         Color headerColor, java.util.List<String> cols) {
        this.id = id; this.displayName = name; this.category = category;
        this.resourceBackground = resBg; this.headerColor = headerColor; this.defaultColumns = cols;
    }

    public static java.util.List<BoardTemplate> all() {
        return java.util.Arrays.asList(

                /// /////////
                /// //////////////
                /// //////////
                /// ///////INGRESAR LAS FOTOS QUE FALTAN PARA QUE QUEDE CHIDO Y BONITO

                /// APARTADO DE NEGOCIOS
                new BoardTemplate("negocio", "Negocio", "Negocio",
                        "/resources/Negocios.jpeg",
                        new Color(25, 55, 109), java.util.Arrays.asList("Backlog","En Progreso","Revisión","Hecho")),
                new BoardTemplate("negocio", "Negocio", "Negocio",
                        "/resources/Negocios1.jpeg",
                        new Color(25, 55, 109), java.util.Arrays.asList("Backlog","En Progreso","Revisión","Hecho")),
                new BoardTemplate("negocio", "Negocio", "Negocio",
                        "/resources/Negocios2.jpeg",
                        new Color(25, 55, 109), java.util.Arrays.asList("Backlog","En Progreso","Revisión","Hecho")),




                /// /APARTADOS DE DISEÑO
                new BoardTemplate("diseno", "Diseño", "Diseño",
                        "/resources/Diseño.jpg",
                        new Color(99, 102, 241), java.util.Arrays.asList("Ideas","Wireframes","UI en curso","Entregado")),
                new BoardTemplate("diseno", "Diseño", "Diseño",
                        "/resources/Diseño1.jpg",
                        new Color(99, 102, 241), java.util.Arrays.asList("Ideas","Wireframes","UI en curso","Entregado")),
                new BoardTemplate("diseno", "Diseño", "Diseño",
                        "/resources/Diseño2.jpg",
                        new Color(99, 102, 241), java.util.Arrays.asList("Ideas","Wireframes","UI en curso","Entregado")),




                /// /APARTADOS DE EDUCACION
                new BoardTemplate("educacion", "Educación", "Educación",
                        "/resources/Educacion.jpeg",
                        new Color(16, 185, 129), java.util.Arrays.asList("Tareas","Estudio","Exámenes","Completado")),
                new BoardTemplate("educacion", "Educación", "Educación",
                        "/resources/Educacion1.jpeg",
                        new Color(16, 185, 129), java.util.Arrays.asList("Tareas","Estudio","Exámenes","Completado")), new BoardTemplate("educacion", "Educación", "Educación",
                        "/resources/Educacion2.jpeg",
                        new Color(16, 185, 129), java.util.Arrays.asList("Tareas","Estudio","Exámenes","Completado")),




                /// /APARTADOS DE INGENIERIA
                new BoardTemplate("ingenieria", "Ingeniería", "Ingeniería",
                        "/resource/Ingenieria.jpeg",
                        new Color(31, 41, 55), java.util.Arrays.asList("To Do","In Dev","Code Review","Done")),
                new BoardTemplate("ingenieria", "Ingeniería", "Ingeniería",
                        "/resources/Ingenieria1.jpeg",
                        new Color(31, 41, 55), java.util.Arrays.asList("To Do","In Dev","Code Review","Done")),
                new BoardTemplate("ingenieria", "Ingeniería", "Ingeniería",
                        "/resources/Ingenieria2.jpeg",
                        new Color(31, 41, 55), java.util.Arrays.asList("To Do","In Dev","Code Review","Done")),



                /// /APARTADO DE MARKETING
                new BoardTemplate("marketing", "Marketing", "Marketing",
                        "/resources/marketing.jpg",
                        new Color(2,132,199), java.util.Arrays.asList("Ideas","Producción","Aprobación","Publicado")),



                /// /APARTADO DE PROYECTOS
                new BoardTemplate("proyectos", "Gestión de Proyectos", "Gestión de Proyectos",
                        "/resources/proyectos.jpg",
                        new Color(2, 132, 199), java.util.Arrays.asList("Por hacer","En progreso","Bloqueado","Hecho")),



                /// /APARTADO DE TRABAJO A DISTANCIA
                new BoardTemplate("remoto", "Trabajo a distancia", "Trabajo a distancia",
                        "/resources/remoto.jpg",
                        new Color(15, 118, 110), java.util.Arrays.asList("Plan","Daily","En curso","Done")),



                /// /APARTADO DE PRODUCTIVIDAD
                new BoardTemplate("productividad", "Productividad", "Productividad",
                        "/resources/productividad.jpg",
                        new Color(87, 83, 201), java.util.Arrays.asList("Captura","Prioriza","Haz","Revisa"))

        );
    }

    public static BoardTemplate byId(String id) {
        if (id == null) return null;
        for (BoardTemplate t : all()) {
            if (id.equalsIgnoreCase(t.id)) {
                return t;
            }
        }
        return null;
    }

}
