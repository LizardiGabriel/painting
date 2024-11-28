package general;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FunAes {
    private static final int KEY_SIZE = 256;
    private static final int TAG_SIZE = 128;
    private static final int IV_SIZE = 12;

    public static void main(String[] args) {

        String mensaje = "mensaje a cifrar";

        String key = FunAes.keyGeneration();

        byte[] iv = FunAes.genIV();
        String cifrado = FunAes.encrypt(key, mensaje.getBytes(), iv);
        System.out.println("Mensaje cifrado: " + cifrado);
        String descifrado = FunAes.decrypt(key, Base64.getDecoder().decode(cifrado), iv);
        System.out.println("Mensaje descifrado: " + descifrado);

    }

    public static String keyGeneration() {

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE);
            return Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded()) ;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encrypt(String key, byte[] message, byte[] iv){

        try{
            SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), 0, Base64.getDecoder().decode(key).length, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(message));

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    public static String decrypt(String key, byte[] encryptedMessage, byte[] iv){
        try {
            SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), 0, Base64.getDecoder().decode(key).length, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
            return new String(cipher.doFinal(encryptedMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static byte[] genIV(){
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] iv = new byte[IV_SIZE];
            random.nextBytes(iv);
            return iv;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}