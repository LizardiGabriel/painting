package general;

import jueces.JudgmentApp;
import painters.PainterApp;

import javax.swing.*;
import java.awt.*;

public class Principal {

    private JFrame frame;
    private JPanel currentPanel;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Principal window = new Principal();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Principal() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new CardLayout(0, 0));

        showLogin(); // Mostrar el login general al iniciar
    }

    private void showLogin() {
        JPanel loginPanel = new JPanel(new GridLayout(6, 1, 10, 10));

        JLabel userLabel = new JLabel("Nombre de usuario:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Contraseña:");
        JPasswordField passField = new JPasswordField();
        JButton loginButton = new JButton("Iniciar Sesión");
        JButton crearCuentaButton = new JButton("Crear Cuenta");

        loginPanel.add(userLabel);
        loginPanel.add(userField);
        loginPanel.add(passLabel);
        loginPanel.add(passField);
        loginPanel.add(loginButton);
        loginPanel.add(crearCuentaButton);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            // Autenticar al usuario
            String[] response = SocketHandler.authenticateUser(username, password);
            String token = response[0];
            String userType = response[1]; // Obtener el tipo de usuario

            if (token.isEmpty() || userType.isEmpty()) {
                JOptionPane.showMessageDialog(loginPanel, "Error al autenticar", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Redirigir según el tipo de usuario
            if ("judge".equals(userType)) {
                JudgmentApp judgmentApp = new JudgmentApp(this);
                showPanel(judgmentApp.judgePanel(token));
            } else if ("painter".equals(userType)) {
                PainterApp painterApp = new PainterApp(this);
                showPanel(painterApp.painterPanel(token));
            } else {
                JOptionPane.showMessageDialog(loginPanel, "Tipo de usuario desconocido", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        crearCuentaButton.addActionListener(e -> {
            showCrearCuenta();
        });

        frame.getContentPane().add(loginPanel, "login");
        ((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "login");
        currentPanel = loginPanel;
    }

    private void showCrearCuenta() {
        JPanel crearCuentaPanel = new JPanel(new GridLayout(8, 2, 10, 10));

        JLabel userLabel = new JLabel("Nombre de usuario:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Contraseña:");
        JPasswordField passField = new JPasswordField();
        JLabel nameLabel = new JLabel("Nombre:");
        JTextField nameField = new JTextField();
        JLabel userTypeLabel = new JLabel("Tipo de usuario:");
        JComboBox<String> userTypeComboBox = new JComboBox<>(new String[]{"judge", "painter"});

        JButton registerButton = new JButton("Registrar");
        JButton backButton = new JButton("Volver");

        crearCuentaPanel.add(userLabel);
        crearCuentaPanel.add(userField);
        crearCuentaPanel.add(passLabel);
        crearCuentaPanel.add(passField);
        crearCuentaPanel.add(nameLabel);
        crearCuentaPanel.add(nameField);
        crearCuentaPanel.add(userTypeLabel);
        crearCuentaPanel.add(userTypeComboBox);
        crearCuentaPanel.add(registerButton);
        crearCuentaPanel.add(backButton);

        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            String name = nameField.getText();
            String userType = (String) userTypeComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || userType == null) {
                JOptionPane.showMessageDialog(crearCuentaPanel, "Por favor, completa todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("painter".equals(userType)) {
                // Para pintores, primero necesitamos el consentimiento
                PainterApp painterApp = new PainterApp(this);
                showPanel(painterApp.getConsentimientoPanel(username, password, name));
            } else if ("judge".equals(userType)) {
                // Para jueces, mostrar el panel de generación de claves RSA
                JudgmentApp judgmentApp = new JudgmentApp(this);
                showPanel(judgmentApp.clavesRsaPanel(username, password, name));

            } else {
                // Manejar otros tipos de usuario si es necesario
                JOptionPane.showMessageDialog(crearCuentaPanel, "Tipo de usuario no soportado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            showLogin();
        });

        showPanel(crearCuentaPanel);
    }



    private void showPanel(JPanel panel) {
        frame.getContentPane().add(panel, "panel");
        ((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "panel");
        currentPanel = panel;
    }

    //  volver al login desde cualquier panel
    public void showLoginFromAnyPanel() {
        ((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "login");
        // Limpiar el panel actual si es necesario
        if (currentPanel != null) {
            frame.getContentPane().remove(currentPanel);
            currentPanel = null;
        }
    }

    // Getters para que PainterApp pueda acceder al JFrame y al panel actual
    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }


    public void setCurrentPanel(JPanel currentPanel) {
        this.currentPanel = currentPanel;
    }

    public JPanel getCurrentPanel() {
        return currentPanel;
    }


}