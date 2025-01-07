package general;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class SocketHandler {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    // Variables para almacenar la información del usuario autenticado
    public static String authToken = "";
    public static String authUserType = "";
    public static String authUserId = "";

    public  static String manejoSocket(String jsonDatos) {
        String respuesta = "";
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(jsonDatos);
            respuesta = in.readLine();

            // cerrar la conexión
            out.close();
            in.close();
            socket.close();
            System.out.println("conexion cerrada");
        } catch (UnknownHostException e) {
            System.err.println("Host desconocido: " + SERVER_HOST);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("No se pudo obtener E/S para la conexión a: " + SERVER_HOST);
            System.exit(1);
        }
        return respuesta;
    }


    public static String[] authenticateUser(String username, String password) {
        String[] result = new String[3];
        Arrays.fill(result, "");

        try {
            JSONObject request = new JSONObject();
            request.put("comando", "AUTENTICAR");
            request.put("user", username);
            request.put("password", password);

            String respuesta = manejoSocket(request.toString());

            if (respuesta != null && !respuesta.isEmpty()) {
                JSONObject response = new JSONObject(respuesta);
                if (response.getString("response").equals("200")) {
                    // Guardar el token, userType y userId
                    authToken = response.getString("token");
                    // retornamos
                    result[0] = authToken;
                    result[1] = response.getString("userType");
                    result[2] = response.getString("userId");
                }
            } else {
                System.err.println("Respuesta del servidor vacía o nula.");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }



    public static boolean usuarioExiste(String user) {
        JSONObject json = new JSONObject();
        json.put("comando", "USUARIO_EXISTE");
        json.put("user", user);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);

        if (response.getString("response").equals("200")) {
            System.out.println(response.getString("info"));
            return true;
        }

        if (response.getString("response").equals("401")) {
            System.out.println(response.getString("info"));
            return false;
        }

        System.out.println(response.getString("info"));
        return false;
    }

    public static boolean registerPainter(String token, String username, String firma, String pub, String nombre, String password) {
        JSONObject json = new JSONObject();
        json.put("comando", "REGISTRAR_PINTOR");
        json.put("token", token);
        json.put("user", username);
        json.put("firma", firma);
        json.put("pub", pub);
        json.put("nombre", nombre);
        json.put("password", password);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return true;
        }

        System.out.println(response.getString("info"));
        return false;
    }

    public static boolean registrarJuez(String token, String username, String nombre, String pubKeyRsaOaep, String password) {
        JSONObject json = new JSONObject();
        json.put("comando", "REGISTRAR_JUEZ");
        json.put("token", token);
        json.put("user", username);
        json.put("nombre", nombre);
        json.put("clave_publica_rsaOAP", pubKeyRsaOaep);
        json.put("password", password);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return true;
        }

        System.out.println(response.getString("info"));
        return false;
    }

    public static String getTYC() {
        JSONObject json = new JSONObject();
        json.put("comando", "OBTENER_TYC");
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return response.getString("tyc");
        }

        System.out.println(response.getString("info"));
        return "";
    }

    public static String getRsaJuecesLlaves() {
        JSONObject json = new JSONObject();
        json.put("comando", "GET_JUECES_RSA_PUBLIC_KEYS");
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return response.getString("llaves_publicas");
        }

        System.out.println(response.getString("info"));
        return "";
    }

    public static boolean sendPainting(String token, String imagen, String iv, String aesKeys) {
        JSONObject json = new JSONObject();
        json.put("comando", "SEND_PAINTING");
        json.put("token", token);
        json.put("imagen", imagen);
        json.put("iv", iv);
        json.put("aesKeys", aesKeys);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return true;
        }

        System.out.println(response.getString("info"));
        return false;
    }


    public static String getPaintingsForJudge(String token) {
        JSONObject json = new JSONObject();
        json.put("comando", "GET_PAINTINGS_FOR_JUDGE");
        json.put("token", token);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        // Manejar la respuesta
        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return response.getJSONArray("paintings").toString(); // Devuelve el array de pinturas en formato JSON
        } else {
            System.out.println("Error al obtener la lista de pinturas: " + response.getString("info"));
            return null;
        }
    }


    public static String getEncryptedAESKeyAndIV(String token, String painting_id) {
        JSONObject json = new JSONObject();
        json.put("comando", "getEncryptedAESKeyAndIV");
        json.put("token", token);
        json.put("painting_id", painting_id);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return response.toString();
        }

        System.out.println(response.getString("info"));
        return "";
    }


    public static boolean sendEvaluation(String token, int paintingId, int stars, String comments, String evaluationSignature) {
        JSONObject json = new JSONObject();
        json.put("comando", "EVALUATE_PAINTING");
        json.put("token", token);
        json.put("paintingId", paintingId);
        json.put("stars", stars);
        json.put("comments", comments);
        json.put("evaluationSignature", evaluationSignature);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        return response.getString("response").equals("200");
    }





}