package painters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class MainWindow extends JFrame {
    private JTextField filePathField;
    private JButton browseButton;
    private JButton sendButton;
    private File selectedFile;

    public MainWindow() {
        setTitle("Imagen - Firmar, Cifrar y Enviar");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        filePathField = new JTextField();
        filePathField.setEditable(false);
        browseButton = new JButton("Seleccionar Imagen");
        sendButton = new JButton("Enviar Imagen");
        sendButton.setEnabled(false);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(browseButton);
        buttonsPanel.add(sendButton);

        panel.add(filePathField, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                    sendButton.setEnabled(true);
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    try {
                        ImageProcessor processor = new ImageProcessor();
                        processor.sendEncryptedPainting(selectedFile);
                        JOptionPane.showMessageDialog(null, "Imagen enviada correctamente.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

}