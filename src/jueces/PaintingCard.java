package jueces;

import general.*;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.util.Base64;

public class PaintingCard extends JPanel {

    private PrivateKey privateKey;
    private Principal principal;
    private String judgeId;

    public PaintingCard(JSONObject painting, PrivateKey privateKey, Principal principal, String judgeId) throws Exception {
        this.privateKey = privateKey;
        this.principal = principal;
        this.judgeId = judgeId;

        setBorder(BorderFactory.createLineBorder(Estilos.BORDER_COLOR, 1));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 350)); // Tamaño fijo para la card
        setLayout(new GridBagLayout());

        // Obtener la información de la pintura
        int paintingId = painting.getInt("id");
        String filePath = painting.getString("file_path");
        String painterName = painting.getString("painter_name");

        // Obtener la clave AES cifrada y el IV
        String jsonAesData = SocketHandler.getEncryptedAESKeyAndIV(SocketHandler.authToken, String.valueOf(paintingId));
        if (jsonAesData == null) {
            throw new Exception("Error al obtener la clave AES y el IV.");
        }

        JSONObject aesData = new JSONObject(jsonAesData);
        String encryptedAesKeyBase64 = aesData.getString("encrypted_aes_key");
        String ivBase64 = aesData.getString("iv");

        // Descifrar la clave AES
        String aesKeyBase64 = FunRsa.decryptRsa(encryptedAesKeyBase64, this.privateKey);
        // Descifrar la imagen
        byte[] decryptedImageBytes = decryptImage(filePath, aesKeyBase64, ivBase64);
        ImageIcon imageIcon = new ImageIcon(decryptedImageBytes);
        Image image = imageIcon.getImage();
        Image scaledImage = image.getScaledInstance(280, 200, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(scaledImage);

        // Configurar GridBagConstraints para la imagen
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3; // La imagen ocupa tres columnas
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Panel para la imagen
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(imageLabel, gbc);

        // Configurar GridBagConstraints para el nombre del pintor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3; // El nombre del pintor ocupa tres columnas
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 0, 5); // Sin inset inferior

        // Etiqueta para el nombre del pintor
        JLabel painterLabel = new JLabel("Pintor: " + painterName);
        painterLabel.setFont(Estilos.DEFAULT_FONT);
        add(painterLabel, gbc);

        // Configurar GridBagConstraints para el botón de evaluar
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1; // El botón ocupa una columna
        gbc.weightx = 0.3; // Botón ocupa menos espacio
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 0, 5, 5); // Insets para el botón

        // Botón de evaluar
        JButton evaluateButton = new JButton("Evaluar");
        Estilos.styleButton(evaluateButton);
        add(evaluateButton, gbc);

        // Acción del botón de evaluar
        evaluateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Mostrar diálogo de evaluación
                JPanel evaluationPanel = new JPanel(new GridBagLayout());
                evaluationPanel.setBackground(Estilos.SECONDARY_COLOR);
                evaluationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(5, 5, 5, 5);

                JLabel starsLabel = new JLabel("Estrellas (1-3):");
                starsLabel.setFont(Estilos.DEFAULT_FONT);
                starsLabel.setForeground(Estilos.TEXT_COLOR);
                evaluationPanel.add(starsLabel, gbc);

                SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 3, 1);
                JSpinner spinner = new JSpinner(model);
                spinner.setFont(Estilos.DEFAULT_FONT);
                Dimension spinnerSize = new Dimension(50, spinner.getPreferredSize().height);
                spinner.setPreferredSize(spinnerSize);
                evaluationPanel.add(spinner, gbc);

                JLabel commentsLabel = new JLabel("Comentarios:");
                commentsLabel.setFont(Estilos.DEFAULT_FONT);
                commentsLabel.setForeground(Estilos.TEXT_COLOR);
                evaluationPanel.add(commentsLabel, gbc);

                JTextArea commentsArea = new JTextArea(5, 20);
                commentsArea.setLineWrap(true);
                commentsArea.setWrapStyleWord(true);
                commentsArea.setFont(Estilos.DEFAULT_FONT);
                commentsArea.setBorder(Estilos.FIELD_BORDER);
                JScrollPane scrollPane = new JScrollPane(commentsArea);
                evaluationPanel.add(scrollPane, gbc);

                int result = JOptionPane.showConfirmDialog(principal.getFrame(), evaluationPanel, "Evaluar Pintura " + paintingId,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    int stars = (int) spinner.getValue();
                    String comments = commentsArea.getText();

                    // Firmar la evaluación
                    String evaluationSignature = "";
                    try {
                        String evaluationData = paintingId + ";" + stars + ";" + comments;
                        evaluationSignature = signEvaluation(evaluationData, privateKey);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(principal.getFrame(), "Error al firmar la evaluación.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Enviar la evaluación al servidor
                    if (SocketHandler.sendEvaluation(SocketHandler.authToken, paintingId, stars, comments, evaluationSignature)) {
                        JOptionPane.showMessageDialog(principal.getFrame(), "Evaluación enviada correctamente.");
                    } else {
                        JOptionPane.showMessageDialog(principal.getFrame(), "Error al enviar la evaluación.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private String signEvaluation(String data, PrivateKey privateKey) throws Exception {
        // Implementar la firma de la evaluación aquí
        return "Firma de la evaluacion";
    }

    private byte[] decryptImage(String filePath, String aesKeyBase64, String ivBase64) {
        try {
            // Leer el archivo cifrado
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            byte[] encryptedBytes = new byte[(int) file.length()];
            fis.read(encryptedBytes);
            fis.close();

            // Descifrar
            String decryptedStr = FunAes.decrypt(aesKeyBase64, Base64.getEncoder().encodeToString(encryptedBytes), ivBase64);
            byte[] decryptedBytes = Base64.getDecoder().decode(decryptedStr);

            return decryptedBytes;
        } catch (Exception e) {
            System.out.println("Error al descifrar la imagen: " + e);
            return null;
        }
    }
}