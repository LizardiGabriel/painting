package president;

import general.*;
import jueces.BlindSignatureClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.nio.file.Files;

import org.json.JSONArray;
import org.json.JSONObject;

public class PresidentApp {
    private Principal principal;
    private String token;
    private JList<String> evaluationsList;
    private DefaultListModel<String> listModel;
    private PrivateKey privateKey;
    public PublicKey publicKey;
    public String publicKeyBase64_clase;

    public PresidentApp(Principal principal) {
        this.principal = principal;
    }

    public JPanel presidentPanel(String token) {
        this.token = token;

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Estilos.SECONDARY_COLOR);

        // Panel de título
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Estilos.SECONDARY_COLOR);
        JLabel titleLabel = new JLabel("Bienvenido, Presidente");
        titleLabel.setFont(new Font(Estilos.DEFAULT_FONT.getName(), Font.BOLD, 24));
        titleLabel.setForeground(Estilos.TEXT_COLOR);
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Panel central para la lista de evaluaciones y el botón de firmar
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Estilos.SECONDARY_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Lista de evaluaciones
        listModel = new DefaultListModel<>();
        evaluationsList = new JList<>(listModel);
        evaluationsList.setFont(Estilos.DEFAULT_FONT);
        JScrollPane scrollPane = new JScrollPane(evaluationsList);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Botón de firmar
        JButton signButton = new JButton("Firmar Evaluación");
        Estilos.styleButton(signButton);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Estilos.SECONDARY_COLOR);
        buttonPanel.add(signButton);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Panel inferior para el botón de cerrar sesión
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Estilos.SECONDARY_COLOR);
        JButton logOutButton = new JButton("Cerrar Sesión");
        Estilos.styleButton(logOutButton);
        bottomPanel.add(logOutButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Solicitar al presidente que cargue su clave privada
        if (privateKey == null) {
            JOptionPane.showMessageDialog(principal.getFrame(), "Por favor, carga tu clave privada.");
            this.privateKey = loadPrivateKey(principal.getFrame());
        }

        // Acción del botón de firmar
        signButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedEvaluation = evaluationsList.getSelectedValue();
                if (selectedEvaluation != null) {
                    int evaluationId = Integer.parseInt(selectedEvaluation.split(" - ")[0]);
                    String blindedEvaluation = selectedEvaluation.split(" - ")[1];

                    // Verificar que la clave privada ya está cargada
                    if (privateKey == null) {
                        JOptionPane.showMessageDialog(mainPanel, "Clave privada no cargada.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        // Obtener la evaluación a firmar a ciegas
                        String blindSignature = FunBlindSignature.blindSign((RSAPrivateKey) privateKey, blindedEvaluation);

                        if(blindSignature != null && !blindSignature.isEmpty()){
                            if (SocketHandler.sendBlindedSignature(token, evaluationId, blindSignature)) {
                                // Actualizar la lista de evaluaciones
                                updateEvaluationsList();
                                JOptionPane.showMessageDialog(mainPanel, "Evaluación firmada con éxito.");
                            } else {
                                JOptionPane.showMessageDialog(mainPanel, "Error al enviar la firma a ciegas al servidor.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(mainPanel, "Error al firmar la evaluación.", "Error", JOptionPane.ERROR_MESSAGE);
                        }

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainPanel, "Error al firmar la evaluación: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Selecciona una evaluación de la lista.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });



        // Acción del botón de cerrar sesión
        logOutButton.addActionListener(e -> {
            SocketHandler.authToken = "";
            principal.showLoginFromAnyPanel();
        });

        // Cargar la lista de evaluaciones al inicio
        updateEvaluationsList();

        return mainPanel;
    }

    private void updateEvaluationsList() {
        String evaluationsJson = SocketHandler.getEvaluationsForPresident(token);
        if (evaluationsJson != null) {
            try {
                JSONArray evaluationsArray = new JSONArray(evaluationsJson);
                listModel.clear();
                for (int i = 0; i < evaluationsArray.length(); i++) {
                    JSONObject evaluation = evaluationsArray.getJSONObject(i);
                    String evaluationInfo = String.format("%d - %s", evaluation.getInt("id"), evaluation.get("blind_message"));
                    listModel.addElement(evaluationInfo);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al actualizar la lista de evaluaciones.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(principal.getFrame(), "No se pudieron obtener las evaluaciones.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getBlindedEvaluation(int evaluationId) {
        String blindedEvaluation = SocketHandler.getBlindedEvaluationFromDB(token, evaluationId);
        if (blindedEvaluation != null) {
            return blindedEvaluation;
        } else {
            JOptionPane.showMessageDialog(principal.getFrame(), "Error al obtener la evaluación cegada.", "Error", JOptionPane.ERROR_MESSAGE);
            return ""; // O manejar el error de otra manera
        }
    }

    public void generateAndDownloadKeyPair(String username) {
        try {
            // Generar el par de claves RSA-PSS
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();

            // Codificar las claves a Base64
            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            // Guardar la clave privada
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar clave privada");
            fileChooser.setSelectedFile(new File(username + "_private.key"));
            int userSelection = fileChooser.showSaveDialog(this.principal.getFrame());
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    fos.write(privateKeyBase64.getBytes());
                }
            }

            // Guardar la clave pública
            fileChooser.setDialogTitle("Guardar clave pública");
            fileChooser.setSelectedFile(new File(username + "_public.key"));
            userSelection = fileChooser.showSaveDialog(this.principal.getFrame());
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    fos.write(publicKeyBase64.getBytes());
                }
            }

            publicKeyBase64_clase = publicKeyBase64;




        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(this.principal.getFrame(), "Error al generar el par de claves: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.principal.getFrame(), "Error al guardar las claves en archivos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PrivateKey loadPrivateKey(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona tu clave privada");
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String privateKeyBase64 = new String(Files.readAllBytes(selectedFile.toPath()));
                return FunRsa.getPrivateKeyPSSFromBase64(privateKeyBase64);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, "Error al cargar la clave privada: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }
}