package painters;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketHandler {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;


    public  String manejoSocket(String jsonDatos) {
        String respuesta = "";
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(jsonDatos);
            respuesta = in.readLine();
        } catch (UnknownHostException e) {
            System.err.println("Host desconocido: " + SERVER_HOST);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("No se pudo obtener E/S para la conexión a: " + SERVER_HOST);
            System.exit(1);
        }
        return respuesta;
    }

    public  boolean authenticatePainter(String username, String password) {
        // hacer un json con los datos.
        JSONObject json = new JSONObject();
        json.put("comando", "AUTENTICAR");
        json.put("user", username);
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

    public boolean registerPainter(String username, String password) {
        // implementar la generacion de claves del cliente pintor


        // ...

        // Los Pintores solo generan sus claves ECDSA para firmar el formulario de consenentimiento
        // El servidor verifica la firma antes de agregar la cuenta del pintor a la base de datos.

        // el pintor genera claves AES para cifrar la pintura



        // ...



        JSONObject json = new JSONObject();
        json.put("comando", "REGISTRAR_PINTOR");
        json.put("user", username);
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


}
