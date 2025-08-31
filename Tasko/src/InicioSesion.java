// Librerías necesarias
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class InicioSesion extends JFrame {
    // Panel principal donde se encuentra el inicio de sesión
    private JPanel PanelPrincipal;

    public InicioSesion() {
        InicarTask();
        PantallaInicioS();            // <- construye y agrega la UI
    }

    private void InicarTask() {
        setTitle("El mejor gestor de Tareas - Organiza tus ideas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 750);
        setLocationRelativeTo(null);

        PanelPrincipal = new JPanel(new BorderLayout());
        setContentPane(PanelPrincipal);
    }

    private void PantallaInicioS() {
        // === FONDO CON IMAGEN ===
        Image fondoImg = new ImageIcon(getClass().getResource("/resources/fondo3.jpg")).getImage();

        JPanel fondo = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(fondoImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        fondo.setOpaque(true);

        // === PANEL DE LOGIN con bordes redondeados + sombra difusa + borde ===
        JPanel PnlInicio = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 25;
                int sombra = 10;

                // Sombras difusas
                for (int i = sombra; i > 0; i--) {
                    g2d.setColor(new Color(0, 0, 0, 15));
                    g2d.fillRoundRect(i, i, getWidth() - i*2, getHeight() - i*2, arc, arc);
                }

                // Fondo
                g2d.setColor(new Color(26, 35, 56, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

                // Borde
                g2d.setColor(new Color(255, 255, 255, 120));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);

                g2d.dispose();
            }
        };
        PnlInicio.setPreferredSize(new Dimension(420, 600));
        PnlInicio.setLayout(new BoxLayout(PnlInicio, BoxLayout.Y_AXIS));
        PnlInicio.setOpaque(false);
        PnlInicio.setBorder(new EmptyBorder(30, 30, 30, 30));

        // ---- LOGO (70x70) dentro de recuadro blanco (100x100) fijo ----
        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource("/resources/Logo.jpeg"));
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        JLabel etiquetaLogo = new JLabel(new ImageIcon(imagenEscalada));

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
        panelLogo.setMaximumSize(new Dimension(100, 100));   // <- ¡fijo!
        panelLogo.setMinimumSize(new Dimension(100, 100));   // <- ¡fijo!
        panelLogo.setLayout(new GridBagLayout());
        panelLogo.add(etiquetaLogo);
        panelLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- TÍTULOS ----
        JLabel LblTitulo = new JLabel("Anota, clasifica y resuelve tus pendientes.");
        LblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        LblTitulo.setForeground(new Color(248, 250, 252));
        LblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel LblSubtitulo = new JLabel("Libérate del caos y activa tu productividad.");
        LblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        LblSubtitulo.setForeground(new Color(203, 213, 225));
        LblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- CAMPOS ----
        JLabel JlbEmail = new JLabel("Correo:");
        JlbEmail.setFont(new Font("Arial", Font.PLAIN, 15));
        JlbEmail.setForeground(Color.WHITE);
        JlbEmail.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField TxtEmail = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        TxtEmail.setMaximumSize(new Dimension(360, 45));
        TxtEmail.setPreferredSize(new Dimension(360, 45));
        TxtEmail.setBackground(Color.WHITE);
        TxtEmail.setBorder(new EmptyBorder(12, 15, 12, 15));
        TxtEmail.setForeground(new Color(107, 114, 128));
        TxtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        TxtEmail.setOpaque(false);

        JLabel lblContrasena = new JLabel("Contraseña:");
        lblContrasena.setFont(new Font("Arial", Font.PLAIN, 15));
        lblContrasena.setForeground(Color.WHITE);
        lblContrasena.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField TxtPassword = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        TxtPassword.setMaximumSize(new Dimension(360, 45));
        TxtPassword.setPreferredSize(new Dimension(360, 45));
        TxtPassword.setBackground(Color.WHITE);
        TxtPassword.setBorder(new EmptyBorder(12, 15, 12, 15));
        TxtPassword.setForeground(new Color(107, 114, 128));
        TxtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        TxtPassword.setEchoChar('•');
        TxtPassword.setOpaque(false);

        // ---- OLVIDASTE CONTRASEÑA ----
        JPanel forgotPasswordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        forgotPasswordPanel.setOpaque(false);
        forgotPasswordPanel.setMaximumSize(new Dimension(360, 25));
        JLabel LblForgotPassword = new JLabel("¿Olvidaste tu contraseña?");
        LblForgotPassword.setFont(new Font("Arial", Font.PLAIN, 12));
        LblForgotPassword.setForeground(new Color(59, 130, 246));
        LblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordPanel.add(LblForgotPassword);

        // ---- BOTÓN INICIAR SESIÓN (con funcionalidad restaurada) ----
        JButton BtnSignIn = new JButton("Inicio de sesión") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(59, 130, 246));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics(getFont());
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        BtnSignIn.setMaximumSize(new Dimension(360, 45));
        BtnSignIn.setPreferredSize(new Dimension(360, 45));
        BtnSignIn.setForeground(Color.WHITE);
        BtnSignIn.setFont(new Font("Arial", Font.BOLD, 14));
        BtnSignIn.setBorderPainted(false);
        BtnSignIn.setFocusPainted(false);
        BtnSignIn.setOpaque(false);
        BtnSignIn.setContentAreaFilled(false);
        BtnSignIn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // === FUNCIÓN DE LOGIN (restaurada tal cual tenías) ===
        BtnSignIn.addActionListener(e -> {
            String email = TxtEmail.getText().trim();
            String password = new String(TxtPassword.getPassword()).trim();

            // Validaciones básicas de UI
            if (email.isEmpty()) { JOptionPane.showMessageDialog(this, "Por favor, ingresa tu correo"); return; }
            if (password.isEmpty()) { JOptionPane.showMessageDialog(this, "Por favor, ingresa tu contraseña"); return; }
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa un correo válido"); return;
            }

            try {
                UserDAO dao = new UserDAO();

                Long uid = dao.login(email, password);
                boolean isAdmin = dao.isAdmin(uid);

                SwingUtilities.invokeLater(() -> {
                    if (isAdmin) {
                        new AdminFrame().setVisible(true);
                    } else {
                        // >>> CAMBIO AQUÍ: pasa uid además del email
                        new TableroDeTrabajo(uid.longValue(), email).setVisible(true);

                    }
                    dispose();
                });


            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error inesperado:\n" + ex.getMessage());
            }
        });

        JLabel Jlbor = new JLabel("O");
        Jlbor.setFont(new Font("Arial", Font.PLAIN, 13));
        Jlbor.setForeground(new Color(203, 213, 225));
        Jlbor.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- BOTÓN REGISTRARSE (abre Registro) ----
        JButton BtnRegistrarse = new JButton("Registrarse") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(59, 130, 246));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics(getFont());
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        BtnRegistrarse.setMaximumSize(new Dimension(360, 45));
        BtnRegistrarse.setPreferredSize(new Dimension(360, 45));
        BtnRegistrarse.setForeground(Color.WHITE);
        BtnRegistrarse.setFont(new Font("Arial", Font.BOLD, 14));
        BtnRegistrarse.setBorderPainted(false);
        BtnRegistrarse.setFocusPainted(false);
        BtnRegistrarse.setOpaque(false);
        BtnRegistrarse.setContentAreaFilled(false);
        BtnRegistrarse.setAlignmentX(Component.CENTER_ALIGNMENT);

        // === FUNCIÓN REGISTRO (restaurada) ===
        BtnRegistrarse.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                Registro reg = new Registro();
                reg.setLocationRelativeTo(this); // centrar respecto a la ventana actual
                reg.setVisible(true);
                reg.toFront();
            });
        });

        // ---- ENSAMBLADO ----
        PnlInicio.add(panelLogo);
        PnlInicio.add(Box.createVerticalStrut(25));
        PnlInicio.add(LblTitulo);
        PnlInicio.add(Box.createVerticalStrut(20));
        PnlInicio.add(LblSubtitulo);
        PnlInicio.add(Box.createVerticalStrut(20));
        PnlInicio.add(JlbEmail);
        PnlInicio.add(Box.createVerticalStrut(10));
        PnlInicio.add(TxtEmail);
        PnlInicio.add(Box.createVerticalStrut(15));
        PnlInicio.add(lblContrasena);
        PnlInicio.add(Box.createVerticalStrut(10));
        PnlInicio.add(TxtPassword);
        PnlInicio.add(Box.createVerticalStrut(10));
        PnlInicio.add(forgotPasswordPanel);
        PnlInicio.add(Box.createVerticalStrut(25));
        PnlInicio.add(BtnSignIn);
        PnlInicio.add(Box.createVerticalStrut(15));
        PnlInicio.add(Jlbor);
        PnlInicio.add(Box.createVerticalStrut(15));
        PnlInicio.add(BtnRegistrarse);

        // Centrar el cuadro sobre el fondo
        fondo.add(PnlInicio, new GridBagConstraints());

        PanelPrincipal.add(fondo, BorderLayout.CENTER);
        PanelPrincipal.revalidate();
        PanelPrincipal.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InicioSesion().setVisible(true);
        });
    }
}
