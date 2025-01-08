package jueces;

import general.Estilos;
import general.FunRsa;
import general.Principal;
import general.SocketHandler;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RegisterPanel extends JPanel {

    private Principal principal;

    public RegisterPanel(Principal principal, String username, String nombre, String password) {
        this.principal = principal;

        setBackground(Estilos.SECONDARY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Registro de Juez");
        titleLabel.setFont(new Font(Estilos.DEFAULT_FONT.getName(), Font.BOLD, 18));
        titleLabel.setForeground(Estilos.TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel generatingLabel = new JLabel("Generando claves RSA-OAEP...");
        generatingLabel.setFont(Estilos.DEFAULT_FONT);
        generatingLabel.setForeground(Estilos.TEXT_COLOR);
        generatingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton downloadKeysButton = new JButton("Descargar Claves");
        Estilos.styleButton(downloadKeysButton);

        JButton backButton = new JButton("Volver");
        Estilos.styleButton(backButton);

        add(titleLabel, gbc);
        add(generatingLabel, gbc);
        add(downloadKeysButton, gbc);
        add(backButton, gbc);

        // generar claves rsa oaep
        String llaves = FunRsa.generateRsaKeys();
        JSONObject jsonKeys = new JSONObject(llaves);
        String privateKeyBase64 = jsonKeys.getString("private");
        String publicKeyBase64 = jsonKeys.getString("public");

        downloadKeysButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

                // si se descargaron -> registrar en el server usando el token de administrador temporal
                if (SocketHandler.registrarJuez(SocketHandler.authToken, username, nombre, publicKeyBase64, password)) {
                    JOptionPane.showMessageDialog(principal.getFrame(), "Cuenta registrada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    principal.showLoginFromAnyPanel();
                } else {
                    JOptionPane.showMessageDialog(principal.getFrame(), "Error al registrar la cuenta", "Error", JOptionPane.ERROR_MESSAGE);
                    principal.showLoginFromAnyPanel();
                }
            }
        });

        backButton.addActionListener(e -> {
            principal.showLoginFromAnyPanel();
        });
    }
}