package general;

import org.json.JSONObject;


import javax.crypto.*;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class FunRsa {

    public static void main(String[] args) throws Exception {
        // Generar claves RSA en formato Base64
        String llaves = generateRsaKeys();
        JSONObject jsonKeys = new JSONObject(llaves);
        String privateKeyBase64 = jsonKeys.getString("private");
        String publicKeyBase64 = jsonKeys.getString("public");
        System.out.println("Clave privada: " + privateKeyBase64);
        System.out.println("Clave pública: " + publicKeyBase64);

        // Mensaje a cifrar
        String mensaje = "esto es un emeplo para cifrar usando rsa oaep";

        // Cifrar el mensaje
        String cifrado = encryptRsa(mensaje, publicKeyBase64);
        System.out.println("Mensaje cifrado: " + cifrado);

        // Descifrar el mensaje
        String descifrado = decryptRsa(cifrado, privateKeyBase64);
        System.out.println("Mensaje descifrado: " + descifrado);
    }

    /**
     * Genera un par de claves RSA de 2048 bits
     * @return JSONObject con las claves en formato Base64
     * @throws NoSuchAlgorithmException
     */
    public static String generateRsaKeys()  {

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();


            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();


            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());


            JSONObject jsonKeys = new JSONObject();
            jsonKeys.put("private", privateKeyBase64);
            jsonKeys.put("public", publicKeyBase64);

            return jsonKeys.toString();

        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Cifra un mensaje con una clave pública RSA
     * @param data mensaje a cifrar
     * @param publicKeyBase64 clave pública RSA en formato Base64
     * @return mensaje cifrado en formato Base64
     * @throws Exception
     */
    public static String encryptRsa(String data, String publicKeyBase64) {
        try {
            byte[] dataBytes = data.getBytes("UTF-8");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            //  clave codificada en formato X.509
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            // generar la clave publica
            PublicKey pubKey = factory.generatePublic(spec);

            // instancia para cifrar con RSA
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey, oaepParams);

            byte[] encryptedBytes = cipher.doFinal(dataBytes);
            String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);

            return encrypted;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Descifra un mensaje con una clave privada RSA
     * @param cipherText mensaje cifrado en formato Base64
     * @param privateKeyBase64 clave privada RSA en formato Base64
     * @return mensaje descifrado
     * @throws Exception
     */
    public static String decryptRsa(String cipherText, String privateKeyBase64) {
        try {
            byte[] cipherData = Base64.getDecoder().decode(cipherText);
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey privKey = factory.generatePrivate(spec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, privKey, oaepParams);

            byte[] plainText = cipher.doFinal(cipherData);
            String decrypted = new String(plainText, "UTF-8");
            return decrypted;

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }




}
