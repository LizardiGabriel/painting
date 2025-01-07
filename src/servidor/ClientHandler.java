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
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String requestJson = in.readLine();

            //System.out.println("Recibido: " + requestJson);

            // Descomponer el json
            JSONObject request = new JSONObject(requestJson);
            String comando = request.getString("comando");

            try {
                System.out.println("Token: " + request.getString("token"));
            }
            catch (Exception e) {
                System.out.println("Token: null");
            }

            String respuesta = "";

            switch (comando) {
                case "AUTENTICAR":
                    respuesta = AuthComandos.autenticar(request);
                    break;
                case "REGISTRAR_PINTOR":
                    respuesta = RegistroComandos.registrar_pintor(request);
                    break;
                case "REGISTRAR_JUEZ":
                    respuesta = RegistroComandos.registrar_juez(request);
                    break;
                case "OBTENER_TYC":
                    respuesta = GeneralComandos.obtenerTYC(request);
                    break;
                case "USUARIO_EXISTE":
                    respuesta = UsuarioComandos.usuarioExiste(request);
                    break;
                case "GET_JUECES_RSA_PUBLIC_KEYS":
                    respuesta = JuezComandos.getJuecesRsaPublicKeys(request);
                    break;
                case "SEND_PAINTING":
                    respuesta = PinturaComandos.sendPainting(request);
                    break;
                case "GET_PAINTINGS_FOR_JUDGE":
                    respuesta = JuezComandos.getPaintingsForJudge(request);
                    break;
                case "getEncryptedAESKeyAndIV":
                    respuesta = JuezComandos.getEncryptedAESKeyAndIV(request);
                    break;

                default:
                    JSONObject response = new JSONObject();
                    response.put("response", "400");
                    response.put("info", "Bad Request - Comando desconocido");
                    respuesta = response.toString();
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