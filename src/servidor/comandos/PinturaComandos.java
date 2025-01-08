package servidor.comandos;

import org.json.JSONObject;
import servidor.Conexion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

public class PinturaComandos {

    private static final String PAINTINGS_DIR = "./src/servidor/paintings/"; // Directorio para guardar las imágenes

    public static String sendPainting(JSONObject request) {
        String ret = "";
        String info = "";

        String token = request.getString("token");
        String imagen = request.getString("imagen");
        String iv = request.getString("iv");
        String aes_keys = request.getString("aesKeys");

        // si cualquier campo está vacío, retornar error
        if (token.isEmpty() || imagen.isEmpty() || iv.isEmpty() || aes_keys.isEmpty()) {
            ret = "400";
            info = "Bad Request";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        // Validar el token
        String[] tokenParts = token.split("_");
        if (tokenParts.length != 3 || !tokenParts[1].equals("painter")) {
            ret = "401";
            info = "Token inválido o expirado";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        // Obtener el user_id del pintor a partir del token
        String userId = tokenParts[0];
        int userIdInt;
        try {
            userIdInt = Integer.parseInt(userId);
            System.out.println("---> User ID (int): " + userIdInt);
        } catch (NumberFormatException e) {
            System.out.println("Error: El ID del usuario no es un número válido: " + userId);
            ret = "400";
            info = "Bad Request - Token inválido";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        Conexion con = new Conexion();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                // Obtener el painter_id a partir del user_id
                String query = "SELECT id FROM Painters WHERE user_id = ?";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, userIdInt);
                ResultSet resultSet = preparedStatement.executeQuery();

                int painterId;
                if (resultSet.next()) {
                    painterId = resultSet.getInt("id");
                    System.out.println("---> Painter ID: " + painterId);
                } else {
                    System.out.println("Error: No se encontró el painter_id para el user_id " + userIdInt);
                    ret = "404";
                    info = "Painter not found";
                    JSONObject response = new JSONObject();
                    response.put("response", ret);
                    response.put("info", info);
                    return response.toString();
                }

                // Crear el directorio de las pinturas si no existe
                File paintingsDir = new File(PAINTINGS_DIR);
                if (!paintingsDir.exists()) {
                    paintingsDir.mkdirs();
                }

                // Generar un nombre de archivo único para la pintura
                String paintingFileName = System.currentTimeMillis() + "_" + painterId + ".dat";
                String paintingFilePath = PAINTINGS_DIR + paintingFileName;

                // Decodificar la imagen en base64 y escribirla en el archivo
                byte[] decodedImage = Base64.getDecoder().decode(imagen);
                try (FileOutputStream fos = new FileOutputStream(paintingFilePath)) {
                    fos.write(decodedImage);
                }

                // Insertar en la tabla Paintings
                query = "INSERT INTO Paintings (painter_id, encrypted_painting_data, iv) VALUES (?, ?, ?)";
                preparedStatement = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setInt(1, painterId); // Usar el painter_id obtenido de la tabla Painters
                preparedStatement.setString(2, paintingFilePath);
                preparedStatement.setString(3, iv);
                int rows = preparedStatement.executeUpdate();

                if (rows > 0) {
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int paintingId = generatedKeys.getInt(1);

                        // Insertar en la tabla Encrypted_AES_Keys
                        JSONObject aesKeysJson = new JSONObject(aes_keys);
                        for (String judge_user : aesKeysJson.keySet()) {
                            String encrypted_aes_key = aesKeysJson.getString(judge_user);

                            // Obtener el id del juez a partir del nombre de usuario
                            query = "SELECT id FROM Users WHERE user = ?";
                            PreparedStatement preparedStatement2 = conexion.prepareStatement(query);
                            preparedStatement2.setString(1, judge_user);
                            ResultSet resultSet2 = preparedStatement2.executeQuery();
                            String user_judge_id = "";
                            if (resultSet2.next()) {
                                user_judge_id = resultSet2.getString("id");
                            }

                            // el judge id no es lo mismo que el user id
                            // Obtener el judge_id a partir del user_id
                            query = "SELECT id FROM Judges WHERE user_id = ?";
                            preparedStatement = conexion.prepareStatement(query);
                            preparedStatement.setInt(1, Integer.parseInt(user_judge_id));
                            resultSet = preparedStatement.executeQuery();

                            int judge_id = 0;

                            if (resultSet.next()) {
                                judge_id = resultSet.getInt("id");
                                System.out.println("???????  ---> Judge ID: " + judge_id);
                            } else {
                                System.out.println("Error: No se encontró el judge_id para el user_id " + user_judge_id);
                                ret = "404";
                                info = "Judge not found";
                                JSONObject response = new JSONObject();
                                response.put("response", ret);
                                response.put("info", info);
                                return response.toString();
                            }




                            query = "INSERT INTO Encrypted_AES_Keys (painting_id, judge_id, encrypted_aes_key) VALUES (?, ?, ?)";
                            preparedStatement = conexion.prepareStatement(query);
                            preparedStatement.setInt(1, paintingId);
                            preparedStatement.setInt(2, judge_id);
                            preparedStatement.setString(3, encrypted_aes_key);
                            rows = preparedStatement.executeUpdate();
                            if (rows == 0) {
                                System.out.println("Error al registrar clave aes cifrada");
                                ret = "500";
                                info = "Internal Server Error";
                            }
                        }
                        ret = "200";
                        info = "OK";
                    }
                } else {
                    System.out.println("Error al registrar la pintura");
                    ret = "500";
                    info = "Internal Server Error";
                }
            } catch (SQLException e) {
                System.out.println("Error SQL: " + e.getMessage());
                ret = "500";
                info = "Internal Server Error";
            } catch (IOException e) {
                System.out.println("Error al guardar la imagen: " + e.getMessage());
                ret = "500";
                info = "Internal Server Error";
            } finally {
                con.cerrar();
            }
        } else {
            System.out.println("Error en la conexión");
            ret = "500";
            info = "Internal Server Error";
        }

        JSONObject response = new JSONObject();
        response.put("response", ret);
        response.put("info", info);
        return response.toString();
    }
}