package painters;
import general.FunEcdsa;
import general.SocketHandler;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PainterApp {

    private JPanel currentPanel;
    private JFrame frame;
    String token = "";

    public void inter() {
        frame = new JFrame("Inicio de Sesión - Pintores");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        currentPanel = loginPanel();

        frame.add(currentPanel, BorderLayout.CENTER);
        frame.setVisible(true);

    }

    private JPanel loginPanel() {
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


        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            token = SocketHandler.authenticateUser(username, password, "painter");

            if (token.isEmpty()){
                JOptionPane.showMessageDialog(panel, "Error al autenticar", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Open the painter's main panel
            currentPanel.setVisible(false);
            currentPanel = painterPanel(token);
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);

        });

        registerButton.addActionListener(e -> {
            // Open the painter's registration panel
            currentPanel.setVisible(false);
            currentPanel = crearCuentaPanel();
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);
        });


        return panel;
    }

    private JPanel crearCuentaPanel() {
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

        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Por favor llena todos los campos",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 1o verificar si existe una cuenta con ese nombre de usuario
            if (SocketHandler.usuarioExiste(username)) {
                JOptionPane.showMessageDialog(frame, "El usuario ya existe",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            // pasar los datos a otro panel para firmar el consentimiento


            currentPanel.setVisible(false);
            currentPanel = consentimientoPanel(username, password, "juanito");
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);


        });

        backButton.addActionListener(e -> {
            // Open the painter's main panel
            currentPanel.setVisible(false);
            currentPanel = loginPanel();
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);
        });

        return panel;
    }


    private JPanel consentimientoPanel(String username, String password, String nombre) {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));

        JLabel consentimientoLabel = new JLabel("Firma el formulario de consentimiento");

        JTextArea termsTextArea = new JTextArea(10, 30);

        String terms = SocketHandler.getTYC();

        termsTextArea.setText(terms);
        termsTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(termsTextArea);


        JButton firmarButton = new JButton("Firmar");
        JButton backButton = new JButton("Volver");
        JButton downloadTyCButton = new JButton("Descargar Términos y Condiciones");


        panel.add(consentimientoLabel);
        panel.add(scrollPane);
        panel.add(firmarButton);
        panel.add(backButton);
        panel.add(downloadTyCButton);


        firmarButton.addActionListener(e -> {

            JSONObject json = new JSONObject(FunEcdsa.generateECDSAKeys());
            String priv = json.getString("private");
            String pub = json.getString("public");

            // descargarle al usuario la llave privada
            JOptionPane.showMessageDialog(frame, "Para continuar se te descargaran tus llaves privadas");

            String userHome = System.getProperty("user.home");
            String downloadsFolder = Paths.get(userHome, "Downloads/pintor").toString();
            Path path = Paths.get(downloadsFolder);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            String fileName = username + "_ecdsa.txt";
            String fileToSave = Paths.get(downloadsFolder, fileName).toString();
            String firma = "";

            try {
                // 1. firmar
                firma = FunEcdsa.firmarECDSA(terms, priv);
                // 2. guardar la llave privada
                FileWriter fileWriter = new FileWriter(fileToSave);
                fileWriter.write(priv);
                fileWriter.close();
                JOptionPane.showMessageDialog(frame, "Llave privada guardada en: " + fileToSave);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error al firmar y guardar la llave privada");
                // mandar al login
                currentPanel.setVisible(false);
                currentPanel = loginPanel();
                frame.add(currentPanel, BorderLayout.CENTER);
                currentPanel.setVisible(true);

            }


            if (SocketHandler.registerPainter(username, password, firma, pub, nombre)) {
                currentPanel.setVisible(false);
                currentPanel = loginPanel();
                frame.add(currentPanel, BorderLayout.CENTER);
                currentPanel.setVisible(true);

            }else {
                // something went wrong
                currentPanel.setVisible(false);
                currentPanel = crearCuentaPanel();
                frame.add(currentPanel, BorderLayout.CENTER);
                currentPanel.setVisible(true);
            }

        });

        backButton.addActionListener(e -> {
            // Open the painter's registration panel
            currentPanel.setVisible(false);
            currentPanel = crearCuentaPanel();
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);
        });

        return panel;
    }



    private JPanel painterPanel(String token) {

        MainWindow mainWindow = new MainWindow();
        return mainWindow.mainPanel(token);

    }

}
