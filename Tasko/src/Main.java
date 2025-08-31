import javax.swing.*;

public class Main {
    public static void main(String[] args) {


        // Ajusta user/pass si usaste otros al crear el usuario en MariaDB
        Database.init(
                "jdbc:mysql://127.0.0.1:3306/taskboard"
                        + "?useSSL=false&serverTimezone=UTC"
                        + "&connectTimeout=3000&socketTimeout=8000&tcpKeepAlive=true",
                "taskuser",
                "taskpass"
        );




        if (!Database.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a MariaDB.\nRevisa servicio/URL/credenciales.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new InicioSesion().setVisible(true));
    }
}
