package painters;

import general.Estilos;
import general.Principal;
import general.SocketHandler;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.Image;

public class MainWindow {

    private Principal principal;
    private JLabel imageLabel;

    public MainWindow(Principal principal) {
        this.principal = principal;
    }

    public JPanel mainPanel(String token) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Estilos.SECONDARY_COLOR);

        // Panel para la imagen
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);
        imageScrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(imageScrollPane, BorderLayout.CENTER);

        // Panel para los botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setBackground(Estilos.SECONDARY_COLOR);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        // Botón para seleccionar imagen
        JButton browseButton = new JButton("Seleccionar Imagen");
        Estilos.styleButton(browseButton);
        buttonsPanel.add(browseButton);

        // Botón para enviar imagen
        JButton sendButton = new JButton("Enviar Imagen");
        Estilos.styleButton(sendButton);
        sendButton.setEnabled(false); // Deshabilitado hasta que se seleccione una imagen
        buttonsPanel.add(sendButton);

        // Botón para cerrar sesión
        JButton logoutButton = new JButton("Cerrar Sesión");
        Estilos.styleButton(logoutButton);
        buttonsPanel.add(logoutButton);

        final File[] selectedFile = {null};

        // Acción del botón de selección de imagen
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Image Files", "png", "jpg", "jpeg", "img");
                fileChooser.setFileFilter(filter);
                int returnValue = fileChooser.showOpenDialog(principal.getFrame());
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFile[0] = fileChooser.getSelectedFile();
                    displayImage(selectedFile[0]);
                    sendButton.setEnabled(true);
                }
            }
        });

        // Acción del botón de envío de imagen
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile[0] != null) {
                    try {
                        if (ImageProcessor.sendEncryptedPainting(selectedFile[0], token)) {
                            JOptionPane.showMessageDialog(principal.getFrame(), "Imagen enviada correctamente.");
                        } else {
                            JOptionPane.showMessageDialog(principal.getFrame(), "Error al enviar la imagen.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(principal.getFrame(), "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        logoutButton.addActionListener(e -> {
            // Eliminar la información de autenticación
            SocketHandler.authToken = "";
            principal.showLoginFromAnyPanel();
        });

        return panel;
    }


    private void displayImage(File file) {
        ImageIcon imageIcon = new ImageIcon(file.getPath());
        Image image = imageIcon.getImage();
        // Ajustar la imagen al tamaño del JLabel
        Image scaledImage = image.getScaledInstance(400, 300, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(scaledImage);
        imageLabel.setIcon(imageIcon);
    }
}