import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class Registro extends JFrame {

    public Registro() {
        setTitle("Crear cuenta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 750);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // === Fondo con imagen (mismo estilo que InicioSesion) ===
        final Image fondoImg = cargarImagen("/resources/fondo3.jpg");
        JPanel fondo = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (fondoImg != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    int w = getWidth(), h = getHeight();
                    int iw = fondoImg.getWidth(this), ih = fondoImg.getHeight(this);
                    if (iw > 0 && ih > 0) {
                        // "Cover"
                        double scale = Math.max(w/(double)iw, h/(double)ih);
                        int dw = (int)(iw*scale), dh = (int)(ih*scale);
                        int x = (w - dw)/2, y = (h - dh)/2;
                        g2.drawImage(fondoImg, x, y, dw, dh, this);
                    }
                    g2.dispose();
                } else {
                    setBackground(new Color(15, 23, 42));
                }
            }
        };
        fondo.setOpaque(true);

        // === Tarjeta central con sombra + borde + esquinas ===
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 25;
                int sombra = 10;

                // Sombra difusa
                for (int i = sombra; i > 0; i--) {
                    g2d.setColor(new Color(0, 0, 0, 15));
                    g2d.fillRoundRect(i, i, getWidth() - i*2, getHeight() - i*2, arc, arc);
                }

                // Fondo tarjeta
                g2d.setColor(new Color(26, 35, 56, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

                // Borde suave
                g2d.setColor(new Color(255, 255, 255, 120));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);

                g2d.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(420, 560));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // === Logo dentro de recuadro blanco (70x70 en 100x100 fijo) ===
        Image logoImg = cargarImagen("/resources/Logo.jpeg"); // ajusta si tu logo se llama distinto
        Image logoEscalado = (logoImg != null) ? logoImg.getScaledInstance(70, 70, Image.SCALE_SMOOTH) : null;
        JLabel logoLabel = new JLabel(logoEscalado != null ? new ImageIcon(logoEscalado) : null);
        if (logoEscalado == null) {
            logoLabel.setText("Logo");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
            logoLabel.setForeground(new Color(51, 65, 85));
        }

        JPanel panelLogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        panelLogo.setOpaque(false);
        panelLogo.setPreferredSize(new Dimension(100, 100));
        panelLogo.setMaximumSize(new Dimension(100, 100)); // fijo
        panelLogo.setMinimumSize(new Dimension(100, 100)); // fijo
        panelLogo.setLayout(new GridBagLayout());
        panelLogo.add(logoLabel);
        panelLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // === Títulos ===
        JLabel titulo = new JLabel("Crea tu cuenta");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(new Color(248, 250, 252));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Regístrate para comenzar a organizar tus tareas.");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(203, 213, 225));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // === Campo correo ===
        JLabel lblEmail = new JLabel("Correo:");
        lblEmail.setFont(new Font("Arial", Font.PLAIN, 15));
        lblEmail.setForeground(Color.WHITE);
        lblEmail.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtEmail = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        txtEmail.setMaximumSize(new Dimension(360, 45));
        txtEmail.setPreferredSize(new Dimension(360, 45));
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setBorder(new EmptyBorder(12, 15, 12, 15));
        txtEmail.setForeground(new Color(107, 114, 128));
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        txtEmail.setOpaque(false);

        // === Campo contraseña ===
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(new Font("Arial", Font.PLAIN, 15));
        lblPass.setForeground(Color.WHITE);
        lblPass.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField txtPass = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        txtPass.setMaximumSize(new Dimension(360, 45));
        txtPass.setPreferredSize(new Dimension(360, 45));
        txtPass.setBackground(Color.WHITE);
        txtPass.setBorder(new EmptyBorder(12, 15, 12, 15));
        txtPass.setForeground(new Color(107, 114, 128));
        txtPass.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPass.setOpaque(false);
        txtPass.setEchoChar('\u2022');

        // === Botón Registrar (redondeado con hover/pressed) ===
        JButton btnRegistrar = new JButton("Registrar") {
            @Override protected void paintComponent(Graphics g) {
                ButtonModel m = getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(59, 130, 246);
                Color hover = base.brighter();
                Color pressed = base.darker();
                g2.setColor(m.isPressed() ? pressed : (m.isRollover() ? hover : base));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics(getFont());
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnRegistrar.setMaximumSize(new Dimension(360, 45));
        btnRegistrar.setPreferredSize(new Dimension(360, 45));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setOpaque(false);
        btnRegistrar.setContentAreaFilled(false);
        btnRegistrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegistrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // === Funcionalidad: registrar en BD y volver al login (idéntica a la tuya) ===
        btnRegistrar.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            String pass  = new String(txtPass.getPassword()).trim();

            // Validaciones rápidas
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa tu correo");
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(this, "Ingresa un correo válido (ej: usuario@dominio.com)");
                return;
            }
            if (pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa tu contraseña");
                return;
            }

            try {
                Long id = new UserDAO().register(email, pass, email, false);
                if (id != null) {
                    JOptionPane.showMessageDialog(this, "Registro exitoso. Ahora inicia sesión.");
                    dispose();
                    SwingUtilities.invokeLater(() -> new InicioSesion().setVisible(true));
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo registrar. Intenta de nuevo.");
                }
            } catch (java.sql.SQLException ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.toLowerCase().contains("duplicate")) {
                    JOptionPane.showMessageDialog(this, "Ese correo ya está registrado.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + msg);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error inesperado:\n" + ex.getMessage());
            }
        });

        // === Ensamblado ===
        panel.add(Box.createVerticalStrut(10));
        panel.add(panelLogo);
        panel.add(Box.createVerticalStrut(18));
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitulo);
        panel.add(Box.createVerticalStrut(25));
        panel.add(lblEmail);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtEmail);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblPass);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtPass);
        panel.add(Box.createVerticalStrut(25));
        panel.add(btnRegistrar);

        fondo.add(panel, new GridBagConstraints());
        root.add(fondo, BorderLayout.CENTER);
    }

    private Image cargarImagen(String path) {
        try {
            URL url = getClass().getResource(path);
            return (url != null) ? new ImageIcon(url).getImage() : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
