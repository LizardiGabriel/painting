package jueces;

import general.SocketHandler;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import general.FunRsa;
import org.json.JSONObject;

import general.Principal;

public class JudgmentApp {

    private JPanel currentPanel;
    private Principal principal;
    String token = "";

    public JudgmentApp(Principal principal) {
        this.principal = principal;
    }

    public JPanel clavesRsaPanel(String username, String password, String nombre) {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        JLabel generandoLabel = new JLabel("Generando claves RSA-OAEP...");
        JButton descargarClavesButton = new JButton("Descargar Claves");
        JButton backButton = new JButton("Volver");

        panel.add(generandoLabel);
        panel.add(descargarClavesButton);
        panel.add(backButton);

        // generar claves rsa oaep
        String llaves = FunRsa.generateRsaKeys();
        JSONObject jsonKeys = new JSONObject(llaves);
        String privateKeyBase64 = jsonKeys.getString("private");
        String publicKeyBase64 = jsonKeys.getString("public");

        descargarClavesButton.addActionListener(e -> {
            // descargar clave privada en un archivo
            try {
                String userHome = System.getProperty("user.home");
                String downloadsFolder = Paths.get(userHome, "Downloads/juez").toString();
                Path path = Paths.get(downloadsFolder);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }

                String fileName = username + "_RSA-OAEP.txt";
                String fileToSave = Paths.get(downloadsFolder, fileName).toString();

                FileWriter fileWriter = new FileWriter(fileToSave);
                fileWriter.write(privateKeyBase64);
                fileWriter.close();
                JOptionPane.showMessageDialog(principal.getFrame(), "Clave privada descargada en: " + fileToSave, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al descargar la clave privada", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // si se descargaron -> registrar en el server
            if (SocketHandler.registrarJuez(username, password, nombre, publicKeyBase64)) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Cuenta registrada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                principal.showLoginFromAnyPanel();
            } else {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al registrar la cuenta", "Error", JOptionPane.ERROR_MESSAGE);
                principal.showLoginFromAnyPanel();
            }
        });

        backButton.addActionListener(e -> {
            principal.showLoginFromAnyPanel();
        });

        return panel;
    }

    public JPanel judgePanel(String token) {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));

        JLabel bienvenidoLabel = new JLabel("Bienvenido, Juez");
        JButton logOutButton = new JButton("Cerrar Sesión");

        panel.add(bienvenidoLabel);
        panel.add(logOutButton);

        logOutButton.addActionListener(e -> {
            principal.showLoginFromAnyPanel();
        });

        return panel;
    }
}