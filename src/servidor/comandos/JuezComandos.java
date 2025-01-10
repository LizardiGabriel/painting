package servidor.comandos;

import general.FunBlindSignature;
import general.FunRsa;
import org.json.JSONArray;
import org.json.JSONObject;
import servidor.Conexion;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class JuezComandos {

    public static String getJuecesRsaPublicKeys(JSONObject request) {
        JSONObject response = new JSONObject();
        Conexion con = new Conexion();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                String query = "SELECT u.user, j.public_key_rsa_oaep FROM Judges j JOIN Users u ON j.user_id = u.id";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    response.put(resultSet.getString("user"), resultSet.getString("public_key_rsa_oaep"));
                }

                JSONObject responseFinal = new JSONObject();
                responseFinal.put("response", "200");
                responseFinal.put("info", "OK");
                responseFinal.put("llaves_publicas", response.toString());
                return responseFinal.toString();

            } catch (SQLException e) {
                System.out.println("Error SQL: " + e.getMessage());
                JSONObject responseFinal = new JSONObject();
                responseFinal.put("response", "500");
                responseFinal.put("info", "Internal Server Error");
                return responseFinal.toString();
            } finally {
                con.cerrar();
            }
        } else {
            System.out.println("Error en la conexión");
            JSONObject responseFinal = new JSONObject();
            responseFinal.put("response", "500");
            responseFinal.put("info", "Internal Server Error");
            return responseFinal.toString();
        }
    }


    public static String getPaintingsForJudge(JSONObject request) {
        String token = request.getString("token");
        String[] tokenParts = token.split("_");
        String judgeId = tokenParts[0];

        int judge_id = Integer.parseInt(judgeId);
        System.out.println("Juez ID: " + judge_id);

        Conexion con = new Conexion();
        Connection conexion = con.conectar();
        JSONObject response = new JSONObject();

        if (conexion != null) {
            try {
                // Obtener las pinturas asignadas al juez, incluyendo el nombre del pintor

                String query = "SELECT p.id, p.encrypted_painting_data, u.nombre AS painter_name " +
                        "FROM Paintings p " +
                        "INNER JOIN Encrypted_AES_Keys ek ON p.id = ek.painting_id " +
                        "INNER JOIN Painters pt ON p.painter_id = pt.id " +
                        "INNER JOIN Users u ON pt.user_id = u.id " +
                        "LEFT JOIN Evaluations e ON p.id = e.painting_id AND e.judge_id = ek.judge_id " +
                        "INNER JOIN Judges j ON ek.judge_id = j.id " +
                        "INNER JOIN Users u2 ON j.user_id = u2.id " +
                        "WHERE u2.id = ? AND (e.id IS NULL OR e.is_evaluated = FALSE)";

                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, judge_id);
                ResultSet resultSet = preparedStatement.executeQuery();



                JSONArray paintingsArray = new JSONArray();
                while (resultSet.next()) {
                    JSONObject painting = new JSONObject();
                    painting.put("id", resultSet.getInt("id"));
                    painting.put("file_path", resultSet.getString("encrypted_painting_data"));
                    painting.put("painter_name", resultSet.getString("painter_name"));
                    // ... agregar más información si es necesario ...
                    System.out.println("pintura: "+painting.toString());
                    paintingsArray.put(painting);
                }

                response.put("response", "200");
                response.put("info", "OK");
                response.put("paintings", paintingsArray);

            } catch (SQLException e) {
                System.out.println("Error SQL: " + e.getMessage());
                response.put("response", "500");
                response.put("info", "Internal Server Error");
            } finally {
                con.cerrar();
            }
        } else {
            response.put("response", "500");
            response.put("info", "Error en la conexión");
        }

        return response.toString();
    }


    // el juez solicita la clave AES cifrada y el IV para una pintura
    public static String getEncryptedAESKeyAndIV(JSONObject request) {
        String token = request.getString("token");
        String[] tokenParts = token.split("_");
        String userId = tokenParts[0]; // Ahora obtenemos el userId del token

        String paintingId = request.getString("painting_id");

        Conexion con = new Conexion();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                // Primero, necesitamos obtener el judgeId a partir del userId
                String getJudgeIdQuery = "SELECT j.id FROM Judges j INNER JOIN Users u ON j.user_id = u.id WHERE u.id = ?";
                PreparedStatement getJudgeIdStatement = conexion.prepareStatement(getJudgeIdQuery);
                getJudgeIdStatement.setInt(1, Integer.parseInt(userId));
                ResultSet judgeIdResult = getJudgeIdStatement.executeQuery();

                if (judgeIdResult.next()) {
                    int judgeId = judgeIdResult.getInt("id"); // Obtenemos el judgeId

                    // Ahora, usamos el judgeId para obtener la clave AES cifrada y el IV
                    String query = "SELECT ek.encrypted_aes_key, p.iv, p.encrypted_painting_data " +
                            "FROM Encrypted_AES_Keys ek " +
                            "INNER JOIN Paintings p ON ek.painting_id = p.id " +
                            "WHERE ek.painting_id = ? AND ek.judge_id = ?";
                    PreparedStatement preparedStatement = conexion.prepareStatement(query);
                    preparedStatement.setInt(1, Integer.parseInt(paintingId));
                    preparedStatement.setInt(2, judgeId); // Usamos el judgeId obtenido
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        JSONObject response = new JSONObject();
                        response.put("response", "200");
                        response.put("info", "OK");
                        response.put("encrypted_aes_key", resultSet.getString("encrypted_aes_key"));
                        response.put("iv", resultSet.getString("iv"));
                        response.put("encrypted_painting_data", resultSet.getString("encrypted_painting_data"));
                        return response.toString();
                    } else {
                        JSONObject response = new JSONObject();
                        response.put("response", "404");
                        response.put("info", "Not Found");
                        return response.toString();
                    }
                } else {
                    JSONObject response = new JSONObject();
                    response.put("response", "404");
                    response.put("info", "Judge ID not found for this user");
                    return response.toString();
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                con.cerrar();
            }
        } else {
            JSONObject response = new JSONObject();
            response.put("response", "500");
            response.put("info", "Internal Server Error");
            return response.toString();
        }
    }

    public static String evaluatePainting(JSONObject request) throws SQLException {
        String token = request.getString("token");
        int paintingId = request.getInt("paintingId");
        int stars = request.getInt("stars");
        String comments = request.getString("comments");

        // Validar el token
        String[] tokenParts = token.split("_");
        if (tokenParts.length != 3 || !tokenParts[1].equals("judge")) {
            JSONObject response = new JSONObject();
            response.put("response", "401");
            response.put("info", "Token inválido o expirado");
            return response.toString();
        }
        String userid = tokenParts[0];

        Conexion con = new Conexion();
        Connection conexion = con.conectar();
        JSONObject response = new JSONObject();

        // Obtener el judgeId a partir del userId
        String getJudgeIdQuery = "SELECT j.id FROM Judges j INNER JOIN Users u ON j.user_id = u.id WHERE u.id = ?";
        PreparedStatement getJudgeIdStatement = conexion.prepareStatement(getJudgeIdQuery);
        getJudgeIdStatement.setInt(1, Integer.parseInt(userid));
        ResultSet judgeIdResult = getJudgeIdStatement.executeQuery();
        String judgeId = "";
        if (judgeIdResult.next()) {
            judgeId = judgeIdResult.getString("id");
        }

        if (conexion != null) {
            try {

                String query2 = "SELECT p.public_key_rsa FROM Presidents p INNER JOIN Users u ON p.user_id = u.id WHERE u.type = 'president'";
                PreparedStatement preparedStatement = conexion.prepareStatement(query2);
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();

                String presindentKey = resultSet.getString("public_key_rsa");
                // Preparar la consulta SQL
                String query = "INSERT INTO Evaluations (painting_id, judge_id, stars, comments, blinded_message, inv) " +
                        "VALUES (?, ?, ?, ?, ?, ?)"; // Ajusta los campos según tu tabla
                PreparedStatement preparedStatement2 = conexion.prepareStatement(query);
                preparedStatement2.setInt(1, paintingId);
                preparedStatement2.setInt(2, Integer.parseInt(judgeId));
                preparedStatement2.setInt(3, stars);
                preparedStatement2.setString(4, comments);

                String evaluationData = paintingId + ";" + stars + ";" + comments;
                PublicKey publicKey = FunRsa.getPublicKeyFromBase64(presindentKey);

                String[] blinded = FunBlindSignature.blind(evaluationData, (RSAPublicKey) publicKey);
                // Guardar el blindedMessage en la base de datos
                preparedStatement2.setString(5, blinded[0]);
                preparedStatement2.setString(6, blinded[1]);

                int rows = preparedStatement2.executeUpdate();

                if (rows > 0) {

                    String updateQuery = "UPDATE Evaluations SET is_evaluated = TRUE WHERE painting_id = ? AND judge_id = ?";
                    PreparedStatement updateStatement = conexion.prepareStatement(updateQuery);
                    updateStatement.setInt(1, paintingId);
                    updateStatement.setInt(2, Integer.parseInt(judgeId));
                    updateStatement.executeUpdate();
                    int rows2 = updateStatement.executeUpdate();

                    if (rows2 > 0) {
                        response.put("response", "200");
                        response.put("info", "OK");
                    } else {
                        response.put("response", "500");
                        response.put("info", "Error al guardar la evaluación");
                    }

                } else {
                    response.put("response", "500");
                    response.put("info", "Error al guardar la evaluación");
                }

            } catch (SQLException e) {
                System.out.println("Error SQL: " + e.getMessage());
                response.put("response", "500");
                response.put("info", "Internal Server Error");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                con.cerrar();
            }





        } else {
            response.put("response", "500");
            response.put("info", "Error en la conexión");
        }

        return response.toString();
    }


    public static String getEvaluatedPaintingsForJudge(JSONObject request) throws SQLException {
        String token = request.getString("token");
        String[] tokenParts = token.split("_");
        String userId = tokenParts[0];

        Conexion con = new Conexion();
        Connection conexion = con.conectar();
        JSONObject response = new JSONObject();

        // Obtener el judgeId a partir del userId
        String getJudgeIdQuery = "SELECT j.id FROM Judges j INNER JOIN Users u ON j.user_id = u.id WHERE u.id = ?";
        PreparedStatement getJudgeIdStatement = conexion.prepareStatement(getJudgeIdQuery);
        getJudgeIdStatement.setInt(1, Integer.parseInt(userId));
        ResultSet judgeIdResult = getJudgeIdStatement.executeQuery();
        String judgeId = "";
        if (judgeIdResult.next()) {
            judgeId = judgeIdResult.getString("id");
        }



        if (conexion != null) {
            try {
                // Obtener las pinturas que YA han sido evaluadas por el juez
                String query = "SELECT p.id, p.encrypted_painting_data, u.nombre AS painter_name, e.stars, e.comments, e.blind_signature " +
                        "FROM Paintings p " +
                        "INNER JOIN Encrypted_AES_Keys ek ON p.id = ek.painting_id " +
                        "INNER JOIN Painters pt ON p.painter_id = pt.id " +
                        "INNER JOIN Users u ON pt.user_id = u.id " +
                        "INNER JOIN Evaluations e ON p.id = e.painting_id AND e.judge_id = ek.judge_id " +
                        "WHERE ek.judge_id = ? AND e.is_evaluated = TRUE"; // Filtrar por juez y evaluaciones completadas
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, Integer.parseInt(judgeId));
                ResultSet resultSet = preparedStatement.executeQuery();

                JSONArray paintingsArray = new JSONArray();
                while (resultSet.next()) {
                    JSONObject painting = new JSONObject();
                    painting.put("id", resultSet.getInt("id"));
                    painting.put("file_path", resultSet.getString("encrypted_painting_data"));
                    painting.put("painter_name", resultSet.getString("painter_name"));
                    painting.put("stars", resultSet.getInt("stars"));
                    painting.put("comments", resultSet.getString("comments"));
                    painting.put("blind_signature", resultSet.getString("blind_signature")); // Importante para la verificación
                    // ... agregar más información si es necesario ...
                    paintingsArray.put(painting);
                }

                response.put("response", "200");
                response.put("info", "OK");
                response.put("paintings", paintingsArray);

            } catch (SQLException e) {
                System.out.println("Error SQL: " + e.getMessage());
                response.put("response", "500");
                response.put("info", "Internal Server Error");
            } finally {
                con.cerrar();
            }
        } else {
            response.put("response", "500");
            response.put("info", "Error en la conexión");
        }

        return response.toString();
    }




}