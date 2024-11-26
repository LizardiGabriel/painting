package general;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketHandler {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    /**
     * Maneja la conexión con el servidor
     * @param jsonDatos datos en formato json
     * @return respuesta del servidor
     */
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


    /**
     * Autentica un usuario en el servidor
     * @param username nombre de usuario
     * @param password contraseña
     * @param tipo tipo de usuario
     * @return un string con un token de que tipo de usuario es y mas datos
     */
    public static String authenticateUser(String username, String password, String tipo) {
        // hacer un json con los datos.
        JSONObject json = new JSONObject();
        json.put("comando", "AUTENTICAR");
        json.put("user", username);
        json.put("password", password);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {

            if(tipo.equals(response.getString("type"))) {
                return response.getString("token");
            }
        }

        System.out.println(response.getString("info"));
        return "";

    }



    /**
     * verifica si ya existe un usuario con ese nombre en el server
     * @param user nombre de usuario
     * @return un booleano
     */
    public static boolean usuarioExiste(String user) {
        JSONObject json = new JSONObject();
        json.put("comando", "USUARIO_EXISTE");
        json.put("user", user);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);

        if (response.getString("response").equals("200")) {
            // existe
            System.out.println(response.getString("info"));
            return true;
        }

        if (response.getString("response").equals("401")) {
            // no existe
            System.out.println(response.getString("info"));
            return false;
        }


        System.out.println(response.getString("info"));
        return false;
    }



    /**
     * Registra un pintor en el servidor
     * @param username nombre de usuario
     * @param password contraseña
     * @param firma firma
     * @param pub llave publica
     * @param nombre nombre
     * @return un booleano
     */
    public static boolean registerPainter(String username, String password, String firma, String pub, String nombre) {


        FunEcdsa.generateECDSAKeys();

        JSONObject json = new JSONObject();
        json.put("comando", "REGISTRAR_PINTOR");
        json.put("user", username);
        json.put("password", password);
        json.put("firma", firma);
        json.put("pub", pub);
        json.put("nombre", nombre);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return true;
        }

        System.out.println(response.getString("info"));
        return false;


    }

    /**
     * Registra un juez en el servidor
     * @param username nombre de usuario
     * @param password contraseña
     * @param nombre nombre
     * @param pubKeyRsaOaep llave publica rsa oaep
     * @return un booleano
     */
    public static boolean registrarJuez(String username, String password, String nombre, String pubKeyRsaOaep){
        JSONObject json = new JSONObject();
        json.put("comando", "REGISTRAR_JUEZ");
        json.put("user", username);
        json.put("password", password);
        json.put("nombre", nombre);
        json.put("clave_publica_rsaOAP", pubKeyRsaOaep);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return true;
        }

        System.out.println(response.getString("info"));
        return false;
    }

    /**
     * Obtiene los términos y condiciones del servidor
     * @return los términos y condiciones
     */
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

    /**
     * Obtiene las llaves publicas de todos los jueces
     * @return las llaves publicas de los jueces
     */
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



}
