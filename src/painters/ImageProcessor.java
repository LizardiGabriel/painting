package painters;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

public class ImageProcessor {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public void sendEncryptedPainting(File imageFile) throws Exception {

        byte[] imageBytes = readFileAsBytes(imageFile);
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey aesKey = keyGen.generateKey();
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

        // Cifrar imagen con AES
        byte[] encryptedImage = encryptWithAES(imageBytes, aesKey, gcmSpec);

        // Solicitar claves RSA publicas de jueces

        // Envolver clave AES

        // Enviar datos al servidor (IV, Claves AES cifradas (una por juez), Pintura cifrada)
    }

    private byte[] readFileAsBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    private byte[] encryptWithAES(byte[] data, SecretKey aesKey, GCMParameterSpec gcmSpec) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);
        return cipher.doFinal(data);
    }

}