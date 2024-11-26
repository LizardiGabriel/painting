package painters;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class MainWindow {

    public String token = "";




    public JPanel mainPanel(String token) {
        this.token = token;

        JPanel panel = new JPanel(new BorderLayout());
        JTextField filePathField = new JTextField();
        filePathField.setEditable(false);
        JButton browseButton = new JButton("Seleccionar Imagen");
        JButton sendButton = new JButton("Enviar Imagen");
        sendButton.setEnabled(false);

        JButton logoutButton = new JButton("Cerrar Sesión");

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(browseButton);
        buttonsPanel.add(sendButton);
        buttonsPanel.add(logoutButton);

        panel.add(filePathField, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        final File[] selectedFile = {null};


        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Image Files", "png","jpg", "jpeg", "img");
                fileChooser.setFileFilter(filter);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFile[0] = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile[0].getAbsolutePath());
                    sendButton.setEnabled(true);
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile[0] != null) {
                    try {
                        if (ImageProcessor.sendEncryptedPainting(selectedFile[0]))
                            JOptionPane.showMessageDialog(null, "Imagen enviada correctamente.");
                        else
                            JOptionPane.showMessageDialog(null, "Error al enviar la imagen.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        return panel;
    }

}