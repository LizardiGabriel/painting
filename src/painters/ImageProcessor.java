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
import org.json.JSONObject;

public class ImageProcessor {


    public static boolean sendEncryptedPainting(File imageFile) {
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
            System.out.println("Mensaje cifrado: " + encryptedImage);

            // descifrar la imagen
            String descifrado = FunAes.decrypt(key, encryptedImage, iv);
            System.out.println("Mensaje descifrado: " + descifrado);


            // pedir al socket las llaves RSA OAEP publicas de todos los jueces
            String jsonJuezClave = SocketHandler.getRsaJuecesLlaves();
            JSONObject jsonObject = new JSONObject(jsonJuezClave);
            List<String> juezes = new ArrayList<>();
            List<String> claves = new ArrayList<>();
            for (String juez : jsonObject.keySet()) {
                System.out.println("nombre del juez: " + juez);
                System.out.println("clave publica del juez: " + jsonObject.getString(juez));

                juezes.add(juez);
                claves.add(jsonObject.getString(juez));

            }

            // cifrar la llave AES con las llaves RSA OAEP de los jueces
            List<String> encryptedAesKeys = new ArrayList<>();
            for (String clave : claves) {
                String encryptedAesKey = FunRsa.encryptRsa(key, clave);
                encryptedAesKeys.add(encryptedAesKey);
            }

            // hacer un json con juez: llave cifrada
            JSONObject jsonAesKeys = new JSONObject();
            for (int i = 0; i < juezes.size(); i++) {
                jsonAesKeys.put(juezes.get(i), encryptedAesKeys.get(i));
            }

            System.out.println("jsonAesKeys: " + jsonAesKeys.toString());


            // todo:
            // en un json mandar
            // *. la imagen cifrada
            // * el iv
            // 2. las llaves RSA OAEP cifradas con las llaves RSA OAEP de los jueces
            // 3. el token del pintor
            JSONObject jsonSend = new JSONObject();
            jsonSend.put("imagen", encryptedImage);
            jsonSend.put("iv", iv);
            jsonSend.put("aesKeys", jsonAesKeys.toString());
            //jsonSend.put("token", Pintor.getToken());

            // enviar el json
            if (SocketHandler.sendPainting("enviar datos")){
                
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
