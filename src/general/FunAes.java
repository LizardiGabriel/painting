package general;

import org.json.JSONObject;

public class FunAes {

    public static void main(String[] args) {

        //

        String key = generateAesKey();
        System.out.println("Clave AES: " + key);

        // cifrar mensaje
        String mensaje = "mensaje a cifrar";
        String cifrado = encryptAes(mensaje, key);
        System.out.println("Mensaje cifrado: " + cifrado);

        // descifrar mensaje
        String descifrado = decryptAes(cifrado, key);
        System.out.println("Mensaje descifrado: " + descifrado);

        //
    }

    public static String generateAesKey() {
        // generar clave AES
        String key = "clave";

        return key;
    }

    public static String encryptAes(String data, String key) {
        // cifrar mensaje
        return data;
    }

    public static String decryptAes(String data, String key) {
        // descifrar mensaje
        return data;
    }

    
}
