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
            try {
                System.out.println("... --- ___ --- ... --- ... --- ___ --- ...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress());

                // Crear un hilo para manejar la conexion con el cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();


            } catch (IOException e) {
                System.err.println("Error en la conexión con el cliente: " + e.getMessage());
            }

        }
    }
}