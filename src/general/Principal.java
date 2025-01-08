package general;

import jueces.JudgmentApp;
import painters.PainterApp;
import president.PresidentApp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static general.Estilos.DEFAULT_FONT;
import static servidor.comandos.AuthComandos.generateToken;

public class Principal {

    private JFrame frame;
    private JPanel currentPanel;



    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Establecer el Look and Feel de Nimbus para una apariencia moderna
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
        frame.setTitle("Sistema de Pintura");
        frame.setBounds(100, 100, 500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new CardLayout(0, 0));
        frame.getContentPane().setBackground(Estilos.SECONDARY_COLOR);

        showLogin();
    }

    private void showLogin() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        Estilos.applyDarkMode(loginPanel);

        loginPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        // Componentes del formulario de login
        JLabel userLabel = new JLabel("Nombre de usuario:");
        userLabel.setForeground(Estilos.TEXT_COLOR);
        userLabel.setFont(DEFAULT_FONT);
        JTextField userField = new JTextField();
        userField.setFont(DEFAULT_FONT);
        userField.setBorder(Estilos.FIELD_BORDER);

        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setForeground(Estilos.TEXT_COLOR);
        passLabel.setFont(DEFAULT_FONT);
        JPasswordField passField = new JPasswordField();
        passField.setFont(DEFAULT_FONT);
        passField.setBorder(Estilos.FIELD_BORDER);

        JButton loginButton = new JButton("Iniciar Sesión");
        Estilos.styleMainButton(loginButton);
        JButton crearCuentaButton = new JButton("Crear Cuenta");
        Estilos.styleButton(crearCuentaButton);

        // Agregar componentes al panel usando GridBagConstraints
        loginPanel.add(userLabel, gbc);
        loginPanel.add(userField, gbc);
        loginPanel.add(passLabel, gbc);
        loginPanel.add(passField, gbc);
        // Espacio vertical
        loginPanel.add(Box.createVerticalStrut(15), gbc);
        loginPanel.add(loginButton, gbc);
        loginPanel.add(crearCuentaButton, gbc);

        // Acciones de los botones
        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            // Autenticar al usuario
            String[] response = SocketHandler.authenticateUser(username, password);
            String token = response[0];
            String userType = response[1];
            String userId = response[2];

            if (token.isEmpty() || userType.isEmpty() || userId.isEmpty()) {
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
            } else if ("president".equals(userType)) {
                PresidentApp presidentApp = new PresidentApp(this);
                showPanel(presidentApp.presidentPanel(token));
            } else {
                JOptionPane.showMessageDialog(loginPanel, "Tipo de usuario desconocido", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        crearCuentaButton.addActionListener(e -> {
            showCrearCuenta();
        });

        // Panel para centrar el loginPanel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        Estilos.applyDarkMode(centerPanel);

        centerPanel.add(loginPanel);

        frame.getContentPane().add(centerPanel, "login");
        ((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "login");
        currentPanel = centerPanel;
    }

    private void showCrearCuenta() {
        JPanel crearCuentaPanel = new JPanel(new GridBagLayout());
        Estilos.applyDarkMode(crearCuentaPanel);

        crearCuentaPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        // Componentes del formulario de creación de cuenta
        JLabel userLabel = new JLabel("Nombre de usuario:");
        userLabel.setForeground(Estilos.TEXT_COLOR);
        userLabel.setFont(DEFAULT_FONT);
        JTextField userField = new JTextField();
        userField.setFont(DEFAULT_FONT);
        userField.setBorder(Estilos.FIELD_BORDER);

        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setForeground(Estilos.TEXT_COLOR);
        passLabel.setFont(DEFAULT_FONT);
        JPasswordField passField = new JPasswordField();
        passField.setFont(DEFAULT_FONT);
        passField.setBorder(Estilos.FIELD_BORDER);

        JLabel nameLabel = new JLabel("Nombre:");
        nameLabel.setForeground(Estilos.TEXT_COLOR);
        nameLabel.setFont(DEFAULT_FONT);
        JTextField nameField = new JTextField();
        nameField.setFont(DEFAULT_FONT);
        nameField.setBorder(Estilos.FIELD_BORDER);

        JLabel userTypeLabel = new JLabel("Tipo de usuario:");
        userTypeLabel.setForeground(Estilos.TEXT_COLOR);
        userTypeLabel.setFont(DEFAULT_FONT);
        JComboBox<String> userTypeComboBox = new JComboBox<>(new String[]{"judge", "painter", "president"});
        userTypeComboBox.setFont(DEFAULT_FONT);
        userTypeComboBox.setBackground(Color.WHITE);

        JButton registerButton = new JButton("Registrar");
        Estilos.styleMainButton(registerButton);
        JButton backButton = new JButton("Volver");
        Estilos.styleButton(backButton);

        // Agregar componentes al panel usando GridBagConstraints
        crearCuentaPanel.add(userLabel, gbc);
        crearCuentaPanel.add(userField, gbc);
        crearCuentaPanel.add(passLabel, gbc);
        crearCuentaPanel.add(passField, gbc);
        crearCuentaPanel.add(nameLabel, gbc);
        crearCuentaPanel.add(nameField, gbc);
        crearCuentaPanel.add(userTypeLabel, gbc);
        crearCuentaPanel.add(userTypeComboBox, gbc);
        // Espacio vertical
        crearCuentaPanel.add(Box.createVerticalStrut(15), gbc);
        crearCuentaPanel.add(registerButton, gbc);
        crearCuentaPanel.add(backButton, gbc);

        // Acciones de los botones
        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            String name = nameField.getText();
            String userType = (String) userTypeComboBox.getSelectedItem();

            // Asignar un token de administrador temporal
            String adminToken = null; // Usamos "0" como ID temporal para el admin
            adminToken = generateToken("0", "admin");
            SocketHandler.authToken = adminToken;

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || userType == null) {
                JOptionPane.showMessageDialog(crearCuentaPanel, "Por favor, completa todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("painter".equals(userType)) {
                PainterApp painterApp = new PainterApp(this);
                showPanel(painterApp.getConsentimientoPanel(username, password, name));
            } else if ("judge".equals(userType)) {
                JudgmentApp judgmentApp = new JudgmentApp(this);
                showPanel(judgmentApp.clavesRsaPanel(username, name, password));
            } else if ("president".equals(userType)) {
                // Registrar presidente
                PresidentApp presidentApp = new PresidentApp(this);
                presidentApp.generateAndDownloadKeyPair(username); // Generar y descargar llaves del presidente
                if (SocketHandler.registerPresident(SocketHandler.authToken, username, name, password, presidentApp.publicKeyBase64_clase)) {
                    JOptionPane.showMessageDialog(crearCuentaPanel, "Cuenta de presidente registrada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                    showLoginFromAnyPanel();
                } else {
                    JOptionPane.showMessageDialog(crearCuentaPanel, "Error al registrar la cuenta de presidente", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(crearCuentaPanel, "Tipo de usuario no soportado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        backButton.addActionListener(e -> {
            showLogin();
        });

        // Panel para centrar el crearCuentaPanel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        Estilos.applyDarkMode(centerPanel);
        centerPanel.add(crearCuentaPanel);

        showPanel(centerPanel);
    }

    private void showPanel(JPanel panel) {
        frame.getContentPane().add(panel, "panel");
        ((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "panel");
        currentPanel = panel;
    }

    public void showLoginFromAnyPanel() {
        ((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "login");
        if (currentPanel != null) {
            frame.getContentPane().remove(currentPanel);
            currentPanel = null;
        }
    }

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