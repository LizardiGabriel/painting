package servidor;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

public class Main {

    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("Servidor iniciado en el puerto " + SERVER_PORT);



        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String requestJson = in.readLine();
                System.out.println("Recibido: " + requestJson);

                // descomponer el json
                JSONObject request = new JSONObject(requestJson);
                String comando = request.getString("comando");

                Comandos comandos = new Comandos(requestJson);

                String respuesta = "";

                if (comando.equals("AUTENTICAR")) {
                    respuesta = comandos.autenticar();

                } else if (comando.equals("REGISTRAR_PINTOR")) {
                    respuesta = comandos.registrar("PINTOR");

                } else if (comando.equals("REGISTRAR_JUEZ")) {
                    respuesta = comandos.registrar("JUEZ");
                }


                out.println(respuesta);

            } catch (IOException e) {
                System.err.println("Error en la conexión con el cliente: " + e.getMessage());
            }
        }
    }
}