package jueces;

import general.Estilos;
import general.FunAes;
import general.FunRsa;
import general.SocketHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import general.Principal;

public class JudgmentApp {

    private Principal principal;
    String token = "";
    private String judgeId;
    private PrivateKey privateKey;
    private JPanel cardsPanel; // Panel para las cards
    private int currentCardIndex = 0; // Índice de la card actual
    private JSONArray paintingsArray; // Array de pinturas

    public JudgmentApp(Principal principal) {
        this.principal = principal;
    }

    public JPanel clavesRsaPanel(String username, String nombre, String password) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Estilos.SECONDARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Registro de Juez");
        titleLabel.setFont(new Font(Estilos.DEFAULT_FONT.getName(), Font.BOLD, 18));
        titleLabel.setForeground(Estilos.PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel generatingLabel = new JLabel("Generando claves RSA-OAEP...");
        generatingLabel.setFont(Estilos.DEFAULT_FONT);
        generatingLabel.setForeground(Estilos.TEXT_COLOR);
        generatingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton downloadKeysButton = new JButton("Descargar Claves");
        Estilos.styleButton(downloadKeysButton);

        JButton backButton = new JButton("Volver");
        Estilos.styleButton(backButton);

        panel.add(titleLabel, gbc);
        panel.add(generatingLabel, gbc);
        panel.add(downloadKeysButton, gbc);
        panel.add(backButton, gbc);

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

        return panel;
    }


    public JPanel judgePanel(String token) {
        this.token = token;

        // Obtener el ID del juez a partir del token
        String[] tokenParts = token.split("_");
        this.judgeId = tokenParts[0];

        // Solicitar al juez que cargue su clave privada
        if (!loadPrivateKey()) {
            JOptionPane.showMessageDialog(principal.getFrame(), "Error al cargar la clave privada. Intente de nuevo.", "Error", JOptionPane.ERROR_MESSAGE);
            return new JPanel(); // Retorna un panel vacío
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Estilos.SECONDARY_COLOR);

        // Panel de título
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Estilos.SECONDARY_COLOR);
        JLabel titleLabel = new JLabel("Bienvenido, Juez");
        titleLabel.setFont(new Font(Estilos.DEFAULT_FONT.getName(), Font.BOLD, 24));
        titleLabel.setForeground(Estilos.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Panel para la navegación y la card
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Estilos.SECONDARY_COLOR);

        // Panel para los botones de navegación
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navigationPanel.setBackground(Estilos.SECONDARY_COLOR);
        JButton prevButton = new JButton("Anterior");
        Estilos.styleButton(prevButton);
        JButton nextButton = new JButton("Siguiente");
        Estilos.styleButton(nextButton);
        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        contentPanel.add(navigationPanel, BorderLayout.NORTH); // Navegación en la parte superior

        // Panel para las cards
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        cardsPanel.setBackground(Estilos.SECONDARY_COLOR);
        contentPanel.add(cardsPanel, BorderLayout.CENTER); // Panel de cards en el centro

        // Agregar contentPanel al panel principal
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Panel inferior para el botón de cerrar sesión
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Estilos.SECONDARY_COLOR);
        JButton logOutButton = new JButton("Cerrar Sesión");
        Estilos.styleButton(logOutButton);
        bottomPanel.add(logOutButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Acción del botón de cerrar sesión
        logOutButton.addActionListener(e -> {
            SocketHandler.authToken = "";
            principal.showLoginFromAnyPanel();
        });

        // Acciones de los botones de navegación
        prevButton.addActionListener(e -> {
            if (paintingsArray != null && paintingsArray.length() > 0) {
                currentCardIndex = (currentCardIndex - 1 + paintingsArray.length()) % paintingsArray.length();
                updateCardsPanel();
            }
        });

        nextButton.addActionListener(e -> {
            if (paintingsArray != null && paintingsArray.length() > 0) {
                currentCardIndex = (currentCardIndex + 1) % paintingsArray.length();
                updateCardsPanel();
            }
        });

        // Cargar la lista de pinturas al inicio
        loadPaintings();

        return mainPanel;
    }

    private void loadPaintings() {
        String paintingsJson = SocketHandler.getPaintingsForJudge(token);
        if (paintingsJson != null) {
            try {
                this.paintingsArray = new JSONArray(paintingsJson);
                this.currentCardIndex = 0;
                updateCardsPanel();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al cargar la lista de pinturas.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(principal.getFrame(), "No se pudieron obtener las pinturas.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCardsPanel() {
        cardsPanel.removeAll();
        if (paintingsArray != null && paintingsArray.length() > 0) {
            try {
                JSONObject painting = paintingsArray.getJSONObject(currentCardIndex);
                JPanel cardPanel = createPaintingCard(painting);
                cardsPanel.add(cardPanel);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al crear la card de la pintura.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel createPaintingCard(JSONObject painting) throws Exception {
        JPanel cardPanel = new JPanel(new GridBagLayout()); // Cambio a GridBagLayout
        cardPanel.setBorder(BorderFactory.createLineBorder(Estilos.BORDER_COLOR, 1));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setPreferredSize(new Dimension(400, 350)); // Tamaño fijo para la card

        // Obtener la información de la pintura
        int paintingId = painting.getInt("id");
        String filePath = painting.getString("file_path");
        String painterName = painting.getString("painter_name");

        // Obtener la clave AES cifrada y el IV
        String jsonAesData = SocketHandler.getEncryptedAESKeyAndIV(token, String.valueOf(paintingId));
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
        cardPanel.add(imageLabel, gbc);

        // Configurar GridBagConstraints para el nombre del pintor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3; // El nombre del pintor ocupa tres columnas
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 0, 5); // Sin inset inferior

        // Etiqueta para el nombre del pintor
        JLabel painterLabel = new JLabel("Pintor: " + painterName);
        painterLabel.setFont(Estilos.DEFAULT_FONT);
        cardPanel.add(painterLabel, gbc);

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
        cardPanel.add(evaluateButton, gbc);

        // Acción del botón de evaluar
        evaluateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Mostrar diálogo de evaluación
                JPanel evaluationPanel = new JPanel(new GridBagLayout()); // Usar GridBagLayout
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
                // Establecer un tamaño preferido para el JSpinner
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
                commentsArea.setBorder(Estilos.FIELD_BORDER); // Aplicar el borde definido en Estilos
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
                        String evaluationData = paintingId + ";" + stars + ";" + comments; // Datos a firmar
                        evaluationSignature = signEvaluation(evaluationData, privateKey);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(principal.getFrame(), "Error al firmar la evaluación.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Enviar la evaluación al servidor
                    if (SocketHandler.sendEvaluation(token, paintingId, stars, comments, evaluationSignature)) {
                        JOptionPane.showMessageDialog(principal.getFrame(), "Evaluación enviada correctamente.");
                    } else {
                        JOptionPane.showMessageDialog(principal.getFrame(), "Error al enviar la evaluación.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });



        return cardPanel;
    }


    // todo firmar evaluacion
    private String signEvaluation(String data, PrivateKey privateKey) throws Exception {
        return "Firma de evaluación";
    }



    // para cargar la clave privada desde un archivo
    private boolean loadPrivateKey() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona tu clave privada RSA");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto", "txt"));
        int result = fileChooser.showOpenDialog(principal.getFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String privateKeyBase64 = new String(Files.readAllBytes(selectedFile.toPath()));
                this.privateKey = FunRsa.getPrivateKeyFromBase64(privateKeyBase64);
                return true;
            } catch (Exception e) {
                System.out.println("Error al cargar la clave privada: " + e);
                return false;
            }
        }
        return false;
    }



    //todo
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