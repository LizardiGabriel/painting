package painters;

import general.Estilos;
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
    String userId = "";
    String userType = "";

    public PainterApp(Principal principal) {
        this.principal = principal;
        this.currentPanel = principal.getCurrentPanel();
    }


    public static void main(String[] args) {


        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Principal principal = new Principal();
        principal.getFrame().setVisible(false);


        PainterApp painterApp = new PainterApp(principal);

        JPanel panelDePrueba = painterApp.getConsentimientoPanel("usuarioEjemplo", "passwordEjemplo", "Nombre Ejemplo");

        JFrame frameDePrueba = new JFrame("Prueba de Consentimiento Panel");
        frameDePrueba.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameDePrueba.getContentPane().add(panelDePrueba);
        frameDePrueba.pack();
        frameDePrueba.setLocationRelativeTo(null);
        frameDePrueba.setVisible(true);
    }



    public JPanel getCurrentPanel() {
        return currentPanel;
    }

    public JPanel getConsentimientoPanel(String username, String password, String nombre) {
        JPanel panel = new JPanel(new GridBagLayout()); // Usar GridBagLayout
        Estilos.applyDarkMode(panel);

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel consentimientoLabel = new JLabel("Firma el formulario de consentimiento");
        consentimientoLabel.setFont(new Font(Estilos.DEFAULT_FONT.getName(), Font.BOLD, 16));
        consentimientoLabel.setForeground(Estilos.TEXT_COLOR);
        consentimientoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(consentimientoLabel, gbc);

        JTextArea termsTextArea = new JTextArea(10, 30);
        Estilos.applyDarkMode(termsTextArea);
        String terms = SocketHandler.getTYC();
        termsTextArea.setText(terms);
        termsTextArea.setEditable(false);
        termsTextArea.setFont(Estilos.DEFAULT_FONT);
        termsTextArea.setLineWrap(true);
        termsTextArea.setWrapStyleWord(true);
        termsTextArea.setBorder(Estilos.FIELD_BORDER);


        JScrollPane scrollPane = new JScrollPane(termsTextArea);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Para que el JTextArea ocupe el espacio disponible
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        JButton firmarButton = new JButton("Firmar");
        Estilos.styleMainButton(firmarButton);
        panel.add(firmarButton, gbc);

        JButton backButton = new JButton("Volver");
        Estilos.styleButton(backButton);
        panel.add(backButton, gbc);

        JButton downloadTyCButton = new JButton("Descargar Términos y Condiciones");
        Estilos.styleButton(downloadTyCButton);
        panel.add(downloadTyCButton, gbc);



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

            // usar el token de administrador temporal
            if (SocketHandler.registerPainter(SocketHandler.authToken, username, firma, pub, nombre, password)) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Registro exitoso", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                principal.showLoginFromAnyPanel();
            } else {
                // something went wrong
                JOptionPane.showMessageDialog(principal.getFrame(), "Error en el registro", "Error", JOptionPane.ERROR_MESSAGE);
                principal.showLoginFromAnyPanel();
            }
        });

        downloadTyCButton.addActionListener(e -> {
            try {
                String userHome = System.getProperty("user.home");
                String downloadsFolder = Paths.get(userHome, "Downloads").toString();
                Path path = Paths.get(downloadsFolder);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }

                String fileName = "Terminos_y_Condiciones.txt";
                String fileToSave = Paths.get(downloadsFolder, fileName).toString();

                FileWriter fileWriter = new FileWriter(fileToSave);
                fileWriter.write(terms);
                fileWriter.close();
                JOptionPane.showMessageDialog(principal.getFrame(), "Términos y Condiciones guardados en: " + fileToSave);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al descargar los Términos y Condiciones");
            }
        });

        backButton.addActionListener(e -> {
            principal.showLoginFromAnyPanel();
        });

        return panel;
    }

    public JPanel painterPanel(String token) {
        this.token = token;
        MainWindow mainWindow = new MainWindow(principal);
        return mainWindow.mainPanel(token);
    }
}