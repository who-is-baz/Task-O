import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;

public class AdminFrame extends JFrame {

    // ====== TAB USUARIOS ======
    private JTable usersTable;
    private DefaultTableModel usersModel;

    // ====== TAB PLANTILLAS ======
    private JTable tplTable;
    private DefaultTableModel tplModel;

    public AdminFrame() {
        setTitle("Administrador");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        // Barra de menú (NO altera el layout de los tabs)
        setJMenuBar(buildMenuBar());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Usuarios", buildUsersTab());
        tabs.addTab("Plantillas", buildTemplatesTab());

        setContentPane(tabs);

        // Cargar datos iniciales
        loadUsers();
        loadTemplates();
    }

    /* ========================= MENÚ SUPERIOR ========================= */
    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu menuCuenta = new JMenu("Cuenta");

        JMenuItem miLogout = new JMenuItem("Cerrar sesión");
        // Atajo de teclado: Ctrl+L (Cmd+L en macOS lo toma como Meta+L)
        miLogout.setAccelerator(KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        miLogout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(
                    this,
                    "¿Seguro que deseas cerrar sesión?",
                    "Cerrar sesión",
                    JOptionPane.YES_NO_OPTION
            );
            if (ok == JOptionPane.YES_OPTION) {
                dispose(); // cierra AdminFrame
                SwingUtilities.invokeLater(() -> new InicioSesion().setVisible(true)); // regresa al login
            }
        });

        menuCuenta.add(miLogout);
        mb.add(menuCuenta);
        return mb;
    }

    /* ========================= USUARIOS ========================= */
    private JPanel buildUsersTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10,10,10,10));

        usersModel = new DefaultTableModel(new Object[]{"ID","Email","Nombre","Admin"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Long.class;
                    case 3 -> Boolean.class;
                    default -> String.class;
                };
            }
        };
        usersTable = new JTable(usersModel);
        usersTable.setRowHeight(22);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> loadUsers());

        JButton btnNew = new JButton("Crear usuario…");
        btnNew.addActionListener(e -> showCreateUserDialog());

        JButton btnToggleAdmin = new JButton("Alternar admin");
        btnToggleAdmin.addActionListener(e -> toggleAdminOfSelected());

        JButton btnDelete = new JButton("Eliminar seleccionado");
        btnDelete.setForeground(new Color(200,0,0));
        btnDelete.addActionListener(e -> deleteSelectedUser());

        buttons.add(btnRefresh);
        buttons.add(btnNew);
        buttons.add(btnToggleAdmin);
        buttons.add(btnDelete);

        root.add(buttons, BorderLayout.NORTH);
        root.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        return root;
    }

    private void loadUsers() {
        usersModel.setRowCount(0);
        String sql = "SELECT id, email, display_name, is_admin FROM users ORDER BY id";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usersModel.addRow(new Object[]{
                        rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBoolean(4)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios:\n" + ex.getMessage(),
                    "BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCreateUserDialog() {
        JTextField tfEmail = new JTextField(25);
        JTextField tfName  = new JTextField(25);
        JPasswordField pf  = new JPasswordField(25);
        JCheckBox cbAdmin  = new JCheckBox("Es administrador");

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6); g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; p.add(new JLabel("Email:"), g);
        g.gridx=1; p.add(tfEmail, g);
        g.gridx=0; g.gridy=1; p.add(new JLabel("Nombre mostrado:"), g);
        g.gridx=1; p.add(tfName, g);
        g.gridx=0; g.gridy=2; p.add(new JLabel("Contraseña:"), g);
        g.gridx=1; p.add(pf, g);
        g.gridx=1; g.gridy=3; p.add(cbAdmin, g);

        int ok = JOptionPane.showConfirmDialog(this, p, "Nuevo usuario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        String email = tfEmail.getText().trim();
        String name  = tfName.getText().trim();
        String pass  = new String(pf.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email y contraseña son obligatorios.");
            return;
        }

        try {
            UserDAO dao = new UserDAO();
            dao.register(email, pass, name, cbAdmin.isSelected());
            loadUsers();
            JOptionPane.showMessageDialog(this, "Usuario creado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo crear:\n" + ex.getMessage(),
                    "BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleAdminOfSelected() {
        int row = usersTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un usuario."); return; }
        long id = (Long) usersModel.getValueAt(row, 0);
        boolean current = (Boolean) usersModel.getValueAt(row, 3);
        String sql = "UPDATE users SET is_admin=? WHERE id=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, !current);
            ps.setLong(2, id);
            ps.executeUpdate();
            loadUsers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar:\n" + ex.getMessage(),
                    "BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un usuario."); return; }
        long id = (Long) usersModel.getValueAt(row, 0);
        String email = (String) usersModel.getValueAt(row, 1);
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la cuenta de " + email + " (ID " + id + ")?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM users WHERE id=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            int n = ps.executeUpdate();
            if (n > 0) {
                loadUsers();
                JOptionPane.showMessageDialog(this, "Usuario eliminado.");
            } else {
                JOptionPane.showMessageDialog(this, "No se eliminó ningún registro.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar:\n" + ex.getMessage(),
                    "BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ========================= PLANTILLAS ========================= */
    private JPanel buildTemplatesTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10,10,10,10));

        tplModel = new DefaultTableModel(new Object[]{"ID","Nombre","Color encabezado","#RGB","Fondo"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tplTable = new JTable(tplModel);
        tplTable.setRowHeight(22);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> loadTemplates());

        JButton btnNew = new JButton("Nueva plantilla…");
        btnNew.addActionListener(e -> showCreateTemplateDialog());

        JButton btnDelete = new JButton("Eliminar seleccionada");
        btnDelete.setForeground(new Color(200,0,0));
        btnDelete.addActionListener(e -> deleteSelectedTemplate());

        buttons.add(btnRefresh);
        buttons.add(btnNew);
        buttons.add(btnDelete);

        root.add(buttons, BorderLayout.NORTH);
        root.add(new JScrollPane(tplTable), BorderLayout.CENTER);
        return root;
    }

    private void loadTemplates() {
        tplModel.setRowCount(0);

        // 1) Plantillas base (solo lectura)
        java.util.List<BoardTemplate> base = BoardTemplate.all();
        for (BoardTemplate t : base) {
            String hex = String.format("#%06X", (0xFFFFFF & t.headerColor.getRGB()));
            tplModel.addRow(new Object[]{ t.id, t.displayName, "Base", hex, t.resourceBackground });
        }

        // 2) Personalizadas (desde archivo)
        List<CustomTemplates.SimpleTemplate> custom = CustomTemplates.all();
        for (CustomTemplates.SimpleTemplate s : custom) {
            String hex = String.format("#%06X", (0xFFFFFF & s.headerColor.getRGB()));
            tplModel.addRow(new Object[]{ s.id, s.displayName, "Personalizada", hex, s.backgroundPath });
        }
    }

    private void showCreateTemplateDialog() {
        JTextField tfId   = new JTextField(20);
        JTextField tfName = new JTextField(25);
        JButton pickColor = new JButton("Elegir color…");
        JLabel  colorSw   = new JLabel("        ");
        colorSw.setOpaque(true);
        colorSw.setBackground(new Color(25,55,109));

        pickColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Color encabezado", colorSw.getBackground());
            if (c != null) colorSw.setBackground(c);
        });

        JTextField tfBg = new JTextField(25);
        JButton pickBg  = new JButton("Examinar…");
        pickBg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int r = fc.showOpenDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                tfBg.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6); g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; p.add(new JLabel("ID (único):"), g);
        g.gridx=1; p.add(tfId, g);
        g.gridx=0; g.gridy=1; p.add(new JLabel("Nombre visible:"), g);
        g.gridx=1; p.add(tfName, g);
        g.gridx=0; g.gridy=2; p.add(new JLabel("Color encabezado:"), g);
        JPanel sw = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        sw.add(colorSw); sw.add(pickColor);
        g.gridx=1; p.add(sw, g);
        g.gridx=0; g.gridy=3; p.add(new JLabel("Fondo (imagen local):"), g);
        JPanel bg = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        bg.add(tfBg); bg.add(pickBg);
        g.gridx=1; p.add(bg, g);

        int ok = JOptionPane.showConfirmDialog(this, p, "Nueva plantilla",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        String id   = tfId.getText().trim();
        String name = tfName.getText().trim();
        String bgPath = tfBg.getText().trim();
        Color color = colorSw.getBackground();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID y Nombre son obligatorios.");
            return;
        }
        // Validar que el ID no choque con las base
        for (BoardTemplate t : BoardTemplate.all()) {
            if (t.id.equalsIgnoreCase(id)) {
                JOptionPane.showMessageDialog(this, "El ID ya existe en las plantillas base.");
                return;
            }
        }
        // Validar que no exista ya en personalizadas
        for (CustomTemplates.SimpleTemplate s : CustomTemplates.all()) {
            if (s.id.equalsIgnoreCase(id)) {
                JOptionPane.showMessageDialog(this, "El ID ya existe en plantillas personalizadas.");
                return;
            }
        }

        CustomTemplates.SimpleTemplate st = new CustomTemplates.SimpleTemplate(
                id, name, color, bgPath
        );
        try {
            CustomTemplates.add(st);
            loadTemplates();
            JOptionPane.showMessageDialog(this, "Plantilla creada.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar:\n" + ex.getMessage(),
                    "Plantillas", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedTemplate() {
        int row = tplTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona una plantilla."); return; }
        String id = String.valueOf(tplModel.getValueAt(row, 0));
        String tipo = String.valueOf(tplModel.getValueAt(row, 2));
        if (!"Personalizada".equals(tipo)) {
            JOptionPane.showMessageDialog(this, "Solo puedes eliminar plantillas personalizadas.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la plantilla personalizada '" + id + "'?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            CustomTemplates.delete(id);
            loadTemplates();
            JOptionPane.showMessageDialog(this, "Plantilla eliminada.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar:\n" + ex.getMessage(),
                    "Plantillas", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ========================= UTIL ========================= */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminFrame().setVisible(true));
    }
}
