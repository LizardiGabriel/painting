package jueces;

import general.SocketHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JudgmentApp {


    private JPanel currentPanel;
    private JFrame frame;
    String token = "";

    public void inter(){
        frame = new JFrame("Inicio de Sesión - Jueces");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        currentPanel = loginPanel();

        frame.add(currentPanel, BorderLayout.CENTER);
        frame.setVisible(true);


    }

    private JPanel loginPanel(){
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


            token = SocketHandler.authenticateUser(username, password, "judge");

            if (token.isEmpty()){
                JOptionPane.showMessageDialog(panel, "Error al autenticar", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Open the judge's main panel
            currentPanel.setVisible(false);
            currentPanel = judgePanel(token);
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);

        });

        registerButton.addActionListener(e -> {
            // Open the judge's registration panel
            currentPanel.setVisible(false);
            currentPanel = crearCuentaPanel();
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);
        });

        return panel;


    }

    private JPanel crearCuentaPanel(){
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));

        JLabel userLabel = new JLabel("Nombre de usuario:");
        JTextField userField = new JTextField();
        JLabel nombreLabel = new JLabel("Nombre:");
        JTextField nombreField = new JTextField();
        JLabel passLabel = new JLabel("Contraseña:");
        JPasswordField passField = new JPasswordField();
        JButton registerButton = new JButton("Registrar");
        JButton backButton = new JButton("Regresar");

        panel.add(userLabel);
        panel.add(userField);
        panel.add(nombreLabel);
        panel.add(nombreField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(registerButton);
        panel.add(backButton);

        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Por favor llene todos los campos",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // verificar si existe una cuenta con ese nombre de usuario
            if (SocketHandler.usuarioExiste(username)) {
                JOptionPane.showMessageDialog(panel, "Ya existe una cuenta con ese nombre de usuario", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // panel para generar claves rsa oaep
            currentPanel.setVisible(false);
            currentPanel = clavesRsaPanel(username, password, nombreField.getText());
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);

        });

        backButton.addActionListener(e -> {
            // Open the judge's login panel
            currentPanel.setVisible(false);
            currentPanel = loginPanel();
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);
        });

        return panel;
    }

    private JPanel clavesRsaPanel(String username, String password, String nombre){

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        JLabel generandoLabel = new JLabel("Generando claves RSA-OAEP...");
        JButton descargarClavesButton = new JButton("Descargar Claves");

        panel.add(generandoLabel);
        panel.add(descargarClavesButton);


        // generar claves rsa oaep
        String clavePublica = "ejemplo de una llave publica";
        String clavePrivada = "ejemplo de una llave privada";

        descargarClavesButton.addActionListener(e -> {

            // descargar claves en un archivo

            // ...

            // si se descargaron -> registrar en el server
            if (SocketHandler.registrarJuez(username, password, nombre, clavePublica)){
                JOptionPane.showMessageDialog(panel, "Cuenta registrada exitosamente", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel, "Error al registrar la cuenta", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            currentPanel.setVisible(false);
            currentPanel = loginPanel();
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);

        });

        return panel;
    }

    private JPanel judgePanel(String token){
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));

        JLabel bienvenidoLabel = new JLabel("Bienvenido, Juez");
        JButton logOutButton = new JButton("Cerrar Sesión");

        panel.add(bienvenidoLabel);
        panel.add(logOutButton);

        logOutButton.addActionListener(e -> {
            // Open the judge's login panel
            currentPanel.setVisible(false);
            currentPanel = loginPanel();
            frame.add(currentPanel, BorderLayout.CENTER);
            currentPanel.setVisible(true);
        });

        return panel;
    }
}
