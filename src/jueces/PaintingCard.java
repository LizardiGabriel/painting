package jueces;

import general.*;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class PaintingCard extends JPanel {

    private PrivateKey privateKey;
    private Principal principal;
    private String judgeId;
    private int paintingId;

    public PaintingCard(JSONObject painting, PrivateKey privateKey, Principal principal, String judgeId, boolean isEvaluated) throws Exception {
        this.privateKey = privateKey;
        this.principal = principal;
        this.judgeId = judgeId;


        // Configuración del panel principal
        setOpaque(false); // Importante para que se vea el fondo redondeado
        setPreferredSize(new Dimension(300, 400)); // Ajusta el tamaño según sea necesario
        setLayout(new BorderLayout());

        // Obtener la información de la pintura
        paintingId = painting.getInt("id");
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
        System.out.println("AES Key: " + aesKeyBase64);
        // Descifrar la imagen
        byte[] decryptedImageBytes = decryptImage(filePath, aesKeyBase64, ivBase64);
        ImageIcon imageIcon = new ImageIcon(decryptedImageBytes);
        Image image = imageIcon.getImage();

        // Redimensionar la imagen (ajusta el tamaño según sea necesario)
        Image scaledImage = image.getScaledInstance(280, 250, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(scaledImage);

        // Crear un JLabel para la imagen y configurarlo
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Configuración del panel de contenido
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.add(imageLabel);

        // Etiqueta para el nombre del pintor y estrellas
        JLabel painterLabel = new JLabel(painterName, SwingConstants.CENTER);
        painterLabel.setFont(Estilos.DEFAULT_FONT);
        painterLabel.setForeground(Estilos.TEXT_COLOR);
        painterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(painterLabel);

        if (isEvaluated) {
            // Mostrar estrellas, comentarios y firma
            int stars = painting.getInt("stars");
            String comments = painting.getString("comments");
            String blindSignature = painting.getString("blind_signature");

            // Puntuación en estrellas
            JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            starsPanel.setOpaque(false);
            for (int i = 0; i < 3; i++) {
                JLabel starLabel = new JLabel("★");
                starLabel.setFont(new Font(Estilos.DEFAULT_FONT.getName(), Font.PLAIN, 24));
                // Resaltar las estrellas según la puntuación
                if(i < stars){
                    starLabel.setForeground(Color.YELLOW);
                } else {
                    starLabel.setForeground(Estilos.ACCENT_COLOR);
                }

                starsPanel.add(starLabel);
            }
            contentPanel.add(starsPanel);

            // Comentarios
            JTextArea commentsArea = new JTextArea(comments);
            commentsArea.setEditable(false);
            commentsArea.setLineWrap(true);
            commentsArea.setWrapStyleWord(true);
            Estilos.applyDarkMode(commentsArea);
            contentPanel.add(commentsArea);



        } else {
            // Panel para los botones
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
            buttonsPanel.setOpaque(false);


            // Botón "Evaluar"
            JButton evaluateButton = new JButton("Evaluar");
            Estilos.styleButton(evaluateButton);
            buttonsPanel.add(evaluateButton);

            contentPanel.add(buttonsPanel);

            // Acción del botón de evaluar

            evaluateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Mostrar diálogo de evaluación
                    JPanel evaluationPanel = new JPanel(new GridBagLayout());
                    Estilos.applyDarkMode(evaluationPanel);
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
                    Estilos.applyDarkMode(commentsArea);

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

                        // Enviar la evaluación al servidor
                        if (SocketHandler.sendEvaluation(SocketHandler.authToken, paintingId, stars, comments)) {
                            JOptionPane.showMessageDialog(principal.getFrame(), "Evaluación enviada correctamente.");
                            // Recargar las pinturas no evaluadas
                            // loadPaintings();


                        } else {
                            JOptionPane.showMessageDialog(principal.getFrame(), "Error al enviar la evaluación.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
        }

        add(contentPanel, BorderLayout.CENTER);

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        int width = getWidth();
        int height = getHeight();

        // Crear un rectángulo redondeado como fondo
        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, 30, 30);

        // Rellenar el fondo con un color sólido
        g2d.setColor(Estilos.SECONDARY_COLOR);
        g2d.fill(roundedRectangle);

        // Dibujar el borde redondeado
        g2d.setColor(Estilos.ACCENT_COLOR);
        g2d.setStroke(new BasicStroke(2)); // Ajusta el grosor del borde según sea necesario
        g2d.draw(roundedRectangle);

        g2d.dispose();
    }
}