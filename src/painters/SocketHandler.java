package painters;

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
        String jsonDatos = "{\"comando\":\"AUTENTICAR\",\"usuario\":\"" + username + "\",\"contrasena\":\"" + password + "\"}";
        String respuesta = manejoSocket(jsonDatos);

        System.out.println(respuesta);
        return true;
    }

    public boolean registerPainter(String username, String password) {
        // hacer un json con los datos.
        String jsonDatos = "{\"comando\":\"REGISTRAR\",\"usuario\":\"" + username + "\",\"contrasena\":\"" + password + "\"}";
        String respuesta = manejoSocket(jsonDatos);

        System.out.println(respuesta);
        return true;
    }


}
