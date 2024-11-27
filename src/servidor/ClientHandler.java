package servidor;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;

    }


    @Override
    public void run() {

        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())) ) {
            String requestJson = in.readLine();
            System.out.println("Recibido: " + requestJson);

            // descomponer el json
            JSONObject request = new JSONObject(requestJson);
            String comando = request.getString("comando");

            Comandos comandos = new Comandos(requestJson);

            String respuesta = "";

            if (comando.equals("AUTENTICAR")) {
                respuesta = comandos.autenticar();
            }

            if (comando.equals("REGISTRAR_PINTOR")) {
                respuesta = comandos.registrar_pintor();
            }

            if (comando.equals("REGISTRAR_JUEZ")) {
                respuesta = comandos.registrar_juez();
            }

            if (comando.equals("OBTENER_TYC")) {
                respuesta = comandos.obtenerTYC();
            }

            if (comando.equals("USUARIO_EXISTE")) {
                respuesta = comandos.usuarioExiste();
            }

            if (comando.equals("GET_JUECES_RSA_PUBLIC_KEYS")) {
                respuesta = comandos.getJuecesRsaPublicKeys();
            }

            out.println(respuesta);
            System.out.println("Enviado: " + respuesta);

            // cerrar la conexión
            out.close();
            in.close();
            clientSocket.close();
            System.out.println("conexion cerrada");


        } catch (IOException e) {
            System.err.println("Error en la conexión con el cliente: " + e.getMessage());
        }



    }
}
