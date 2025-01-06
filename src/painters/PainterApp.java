package painters;

import general.FunEcdsa;
import general.SocketHandler;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import general.Principal;

public class PainterApp {

    private JPanel currentPanel;
    private Principal principal;
    String token = "";

    public PainterApp(Principal principal) {
        this.principal = principal;
        this.currentPanel = principal.getCurrentPanel();
    }
    public JPanel getCurrentPanel() {
        return currentPanel;
    }

    public JPanel getConsentimientoPanel(String username, String password, String nombre) {
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
            JOptionPane.showMessageDialog(principal.getFrame(), "Para continuar se te descargaran tus llaves privadas");

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
                JOptionPane.showMessageDialog(principal.getFrame(), "Llave privada guardada en: " + fileToSave);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al firmar y guardar la llave privada");
                // mandar al login
                principal.showLoginFromAnyPanel();
                return; // Importante agregar return para no continuar con la ejecución
            }

            if (SocketHandler.registerPainter(username, password, firma, pub, nombre)) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Registro exitoso", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                principal.showLoginFromAnyPanel();
            } else {
                // something went wrong
                JOptionPane.showMessageDialog(principal.getFrame(), "Error en el registro", "Error", JOptionPane.ERROR_MESSAGE);
                principal.showLoginFromAnyPanel();
            }
        });

        backButton.addActionListener(e -> {
            principal.showLoginFromAnyPanel();
        });

        return panel;
    }

    public JPanel painterPanel(String token) {
        MainWindow mainWindow = new MainWindow();
        return mainWindow.mainPanel(token);
    }
}