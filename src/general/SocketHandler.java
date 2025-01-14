package general;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SocketHandler {

    private static final String SERVER_HOST = "http://localhost:8000";
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private static final int SERVER_PORT = 8000;

    // Variables para almacenar la información del usuario autenticado
    public static String authToken = "";

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
            //System.out.println("conexion cerrada");
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
        String[] result = new String[2];
        Arrays.fill(result, "");

        String url = SERVER_HOST + "/auth";
        try {
            JSONObject requestJSON = new JSONObject();
            requestJSON.put("email", username);
            requestJSON.put("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestJSON.toString().getBytes(StandardCharsets.UTF_8)))
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject responseJSON;
            if(response.statusCode() != 200) {
                responseJSON = new JSONObject(response.body());
                System.out.println(responseJSON.get("error"));
            }

            responseJSON = new JSONObject(response.body());
            result[0] = responseJSON.getString("token");
            result[1] = responseJSON.getString("type");

            return result;
        } catch (JSONException | IOException | InterruptedException e) {
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

    public static boolean registerPainter(String username, String firma, String pub, String nombre, String password) {
        String url = SERVER_HOST + "/key";
        JSONObject json = new JSONObject();
        json.put("email", username);
        json.put("publicKey", pub);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(json.toString().getBytes(StandardCharsets.UTF_8)))
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject responseJson;
            if(response.statusCode() != 200) {
                responseJson = new JSONObject(response.body());
                System.out.println(responseJson.get("error"));
                return false;
            }

            json.clear();
            json.put("email", username);
            json.put("password", password);
            json.put("name", nombre);
            json.put("signature", firma);

            url = SERVER_HOST + "/user";
            request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(json.toString().getBytes(StandardCharsets.UTF_8)))
                    .uri(URI.create(url))
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200) {
                responseJson = new JSONObject(response.body());
                System.out.println(responseJson.get("error"));
                return false;
            }

            return true;
        }catch (IOException | InterruptedException e) {
            System.out.println("No se pudo conectar al server");
            return false;
        }
    }

    public static boolean registrarJuez(String username, String nombre, String pubKeyRsaOaep, String password) {
        String url = SERVER_HOST + "/key";

        JSONObject req = new JSONObject();

        req.put("email", username);
        req.put("publicKey", pubKeyRsaOaep);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(req.toString().getBytes(StandardCharsets.UTF_8)))
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject res;
            if(response.statusCode() != 200) {
                res = new JSONObject(response.body());
                System.out.println(res.get("error"));
                return false;
            }

            req.clear();
            req.put("email", username);
            req.put("password", password);
            req.put("name", nombre);
            req.put("type", "judge");

            url = SERVER_HOST + "/admin";

            request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(req.toString().getBytes(StandardCharsets.UTF_8)))
                    .uri(URI.create(url))
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200) {
                res = new JSONObject(response.body());
                System.out.println(res.get("error"));
                return false;
            }

            return true;
        }catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public static String getTYC() {
        try{
            String url = SERVER_HOST + "/consent";
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if(response.statusCode() != 200) {
                return "Error recuperando los terminos y condiciones";
            }

            JSONObject responseJSON = new JSONObject(response.body());
            return responseJSON.getString("consent");
        }catch (IOException | InterruptedException e) {
            return "Error recuperando los terminos y condiciones";
        }
    }

    public static String getRsaJuecesLlaves(String token) {
        String url = SERVER_HOST + "/judge";
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Authorization", String.format("Bearer %s", token))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject res;
            if(response.statusCode() != 200) {
                res = new JSONObject(response.body());
                System.out.println(res.getString("error"));
                return "error";
            }

            res = new JSONObject(response.body());
            System.out.println("Se recibio: " + res.toString());

            return  res.get("keys").toString();

        }catch (IOException | InterruptedException e) {
            System.out.println("Error al conectarse al server");
            return "error";
        }
    }

    public static boolean sendPainting(String token, String imagen, String iv, String aesKeys) {
        String url = SERVER_HOST + "/painting";
        JSONObject req = new JSONObject();
        req.put("painting", imagen);
        req.put("iv", iv);
        req.put("encrypted_keys", new JSONArray(aesKeys));
        req.put("title", "in progress");
        req.put("description", "in progress");

        System.out.println("Subiendo imagen: " + req.toString());

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(req.toString().getBytes(StandardCharsets.UTF_8)))
                .uri(URI.create(url))
                .header("Authorization", String.format("Bearer %s", token))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject res;
            if(response.statusCode() != 200) {
                res = new JSONObject(response.body());
                System.out.println(res.getString("error"));
                return false;
            }

            return true;
        }catch (IOException | InterruptedException e) {
            System.out.println("No se pudo conectar al server");
            System.out.println(e.getMessage());
            return false;
        }
    }


    public static String getPaintingsForJudge(String token) {
        String url = SERVER_HOST + "/painting";

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Authorization", String.format("Bearer %s", token))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject res = new JSONObject(response.body());
            if(response.statusCode() != 200) {
                System.out.println(res.getString("error"));
                return "Error";
            }

            return res.getJSONArray("paintings").toString();
        }catch (IOException | InterruptedException e) {
            System.out.println("No se pudo conectar al server");
            System.out.println(e.getMessage());
            return "Error";
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


    public static boolean sendEvaluation(String token, int paintingId, int stars, String comments) {
        String url = SERVER_HOST + "/evaluation";
        JSONObject req = new JSONObject();

        req.put("id", paintingId);
        req.put("stars", stars);
        req.put("comments", comments);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(req.toString().getBytes(StandardCharsets.UTF_8)))
                .uri(URI.create(url))
                .header("Authorization", String.format("Bearer %s", token))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject res;
            if(response.statusCode() != 200) {
                res = new JSONObject(response.body());
                System.out.println(res.getString("error"));
                return false;
            }

            return true;
        }catch (IOException | InterruptedException e) {
            System.out.println("No se pudo conectar al server");
            System.out.println(e.getMessage());
            return false;
        }
    }




    public static boolean registerPresident( String username, String name, String password, String publicKey) {
        String url = SERVER_HOST + "/key";

        JSONObject req = new JSONObject();

        req.put("email", username);
        req.put("publicKey", publicKey);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(req.toString().getBytes(StandardCharsets.UTF_8)))
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject res;
            if(response.statusCode() != 200) {
                res = new JSONObject(response.body());
                System.out.println(res.get("error"));
                return false;
            }

            req.clear();
            req.put("email", username);
            req.put("password", password);
            req.put("name", name);
            req.put("type", "chairman");

            url = SERVER_HOST + "/admin";

            request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(req.toString()))
                    .uri(URI.create(url))
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200) {
                res = new JSONObject(response.body());
                System.out.println(res.get("error"));
                return false;
            }

            return true;
        }catch (IOException | InterruptedException e) {
            return false;
        }
    }



    public static String getEvaluationsForPresident(String token) {
        String url = SERVER_HOST + "/evaluation";

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Authorization", String.format("Bearer %s", token))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject res = new JSONObject(response.body());
            if(response.statusCode() != 200) {
                System.out.println(res.getString("error"));
                return null;
            }

            return res.getJSONArray("evaluations").toString();
        }catch (IOException | InterruptedException e) {
            System.out.println("No se pudo conectar al server");
            System.out.println(e.getMessage());
            return null;
        }
    }
    public static String sendBlindedEvaluation(String token, int paintingId, String blindedEvaluation) {
        JSONObject json = new JSONObject();
        json.put("comando", "BLIND_SIGN_EVALUATION");
        json.put("token", token);
        json.put("paintingId", paintingId);
        json.put("blindedEvaluation", blindedEvaluation);


        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return response.getString("blindSignature");
        } else {
            System.out.println("Error al solicitar la firma a ciegas: " + response.getString("info"));
            return null;
        }
    }


    public static String getBlindedEvaluationFromDB(String token, int evaluationId) {


        JSONObject json = new JSONObject();
        json.put("comando", "GET_BLINDED_EVALUATION");
        json.put("token", token);
        json.put("evaluationId", evaluationId);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        if (response.getString("response").equals("200")) {
            return response.getString("blindedEvaluation");
        } else {
            System.out.println("Error al obtener la evaluación cegada: " + response.getString("info"));
            return null;
        }
    }






    public static boolean sendBlindedSignature(String token, int evaluationId, String blindSignature) {
        String url = SERVER_HOST + "/evaluation";

        JSONObject req = new JSONObject();
        req.put("blindedSignature", blindSignature);
        req.put("id", evaluationId);

        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofByteArray(req.toString().getBytes(StandardCharsets.UTF_8)))
                .uri(URI.create(url))
                .header("Authorization", String.format("Bearer %s", token))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject res;
            if(response.statusCode() != 200) {
                res = new JSONObject(response.body());
                System.out.println(res.getString("error"));
                return false;
            }

            return true;
        }catch (IOException | InterruptedException e) {
            return false;
        }
    }


    public static boolean sendPublicKey(String token, String publicKey) {
        JSONObject json = new JSONObject();
        json.put("comando", "SAVE_PRESIDENT_PUBLIC_KEY");
        json.put("token", token);
        json.put("publicKey", publicKey);
        String jsonDatos = json.toString();

        String respuesta = manejoSocket(jsonDatos);

        JSONObject response = new JSONObject(respuesta);
        return response.getString("response").equals("200");
    }


}