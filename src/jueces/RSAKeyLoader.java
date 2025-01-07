package jueces;

import general.FunRsa;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.security.PrivateKey;

public class RSAKeyLoader {

    public static PrivateKey loadPrivateKey(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona tu clave privada RSA");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto", "txt"));
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String privateKeyBase64 = new String(Files.readAllBytes(selectedFile.toPath()));
                return FunRsa.getPrivateKeyFromBase64(privateKeyBase64);
            } catch (Exception e) {
                System.out.println("Error al cargar la clave privada: " + e);
                return null;
            }
        }
        return null;
    }
}