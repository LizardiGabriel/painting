package painters;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PainterApp {


    public void createLoginInterface() {

        JFrame frame = new JFrame("Inicio de Sesión - Pintores");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));

        JLabel userLabel = new JLabel("Nombre de usuario:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Contraseña:");
        JPasswordField passField = new JPasswordField();
        JButton loginButton = new JButton("Iniciar Sesión");
        JButton registerButton = new JButton("Registrar");

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(loginButton);
        panel.add(registerButton);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

        SocketHandler socketHandler = new SocketHandler();

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (socketHandler.authenticatePainter(username, password)) {
                JOptionPane.showMessageDialog(frame, "Inicio de sesión exitoso: " + username);
                frame.dispose();
                launchPainterInterface(username);
            } else {
                JOptionPane.showMessageDialog(frame, "Credenciales incorrectas.");
            }
        });

        registerButton.addActionListener(e -> {
            frame.dispose();
            createRegisterInterface();
        });
    }

    public void createRegisterInterface() {
        JFrame frame = new JFrame("Registro de Pintor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));

        JLabel userLabel = new JLabel("Nombre de usuario:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Contraseña:");
        JPasswordField passField = new JPasswordField();

        JButton registerButton = new JButton("Registrar");
        JButton backButton = new JButton("Volver");

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(registerButton);
        panel.add(backButton);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

        SocketHandler socketHandler = new SocketHandler();

        // Acción de registro
        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            if (socketHandler.registerPainter(username, password)) {
                JOptionPane.showMessageDialog(frame, "Pintor registrado exitosamente.");
                frame.dispose();
                createLoginInterface();
            } else {
                JOptionPane.showMessageDialog(frame, "El usuario ya existe.");
            }
        });

        // Acción para volver
        backButton.addActionListener(e -> {
            frame.dispose();
            createLoginInterface();
        });
    }

    public  void launchPainterInterface(String username) {
        JFrame frame = new JFrame("Pintor - Panel Principal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JLabel welcomeLabel = new JLabel("Bienvenido, " + username, SwingConstants.CENTER);
        frame.add(welcomeLabel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
