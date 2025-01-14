package painters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import general.FunAes;
import general.FunRsa;
import general.SocketHandler;
import org.json.JSONArray;
import org.json.JSONObject;

public class ImageProcessor {

    public static boolean sendEncryptedPainting(File imageFile, String token) {
        try{
            // transformar la imagen a bytes a string
            FileInputStream fileInputStream = new FileInputStream(imageFile);
            byte[] bytes = new byte[(int) imageFile.length()];
            fileInputStream.read(bytes);
            fileInputStream.close();
            String imageB64 = Base64.getEncoder().encodeToString(bytes);

            // generar llave AES
            String key = FunAes.keyGeneration();
            String iv = FunAes.genIV();

            // cifrar la imagen
            String encryptedImage = FunAes.encrypt(key, imageB64, iv);
            //System.out.println("Mensaje cifrado: " + encryptedImage);

            // descifrar la imagen
            //String descifrado = FunAes.decrypt(key, encryptedImage, iv);
            //System.out.println("Mensaje descifrado: " + descifrado);

            // pedir al socket las llaves RSA OAEP publicas de todos los jueces
            String jsonJuezClave = SocketHandler.getRsaJuecesLlaves(token);
            JSONArray jsonArray = new JSONArray(jsonJuezClave);
            System.out.println("Judges public keys: " + jsonArray.toString());
            List<Integer> juezes = new ArrayList<>();
            List<String> claves = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject judgeKey = jsonArray.getJSONObject(i);
                juezes.add(judgeKey.getInt("id"));
                claves.add(judgeKey.getString("publicKey"));
            }

            // cifrar la llave AES con las llaves RSA OAEP de los jueces
            List<String> encryptedAesKeys = new ArrayList<>();
            for (String clave : claves) {
                String encryptedAesKey = FunRsa.encryptRsa(key, clave);
                encryptedAesKeys.add(encryptedAesKey);
            }

            // hacer un json con juez: llave cifrada
            JSONArray jsonAesKeys = new JSONArray();
            for (int i = 0; i < juezes.size(); i++) {
                JSONObject jsonAesKey = new JSONObject();
                jsonAesKey.put("id", juezes.get(i));
                jsonAesKey.put("aesKey", encryptedAesKeys.get(i));
                jsonAesKeys.put(jsonAesKey);
            }

            System.out.println("jsonAesKeys: " + jsonAesKeys.toString());


            // enviar el json
            if (SocketHandler.sendPainting(token, encryptedImage, iv, jsonAesKeys.toString())){
                return true;
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}