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

public class PresidentComandos {


    // Obtiene la lista de evaluaciones que están pendientes de ser firmadas por el presidente.
    public static String getEvaluationsForPresident(JSONObject request) {
        String token = request.getString("token");

        // Validar el token
        String[] tokenParts = token.split("_");
        if (tokenParts.length != 3 || !tokenParts[1].equals("president")) {
            JSONObject response = new JSONObject();
            response.put("response", "401");
            response.put("info", "Token inválido o expirado");
            return response.toString();
        }

        // se podría necesitar el ID del presidente para filtrar
        //String presidentId = tokenParts[0];

        Conexion con = new Conexion();
        Connection conexion = con.conectar();
        JSONObject response = new JSONObject();

        if (conexion != null) {
            try {
                // Consulta para obtener las evaluaciones pendientes de firma
                // Suponiendo que blind_signature es NULL o vacío cuando no está firmada

                String query = "SELECT e.id, e.painting_id, u.nombre AS judge_name " +
                        "FROM Evaluations e " +
                        "INNER JOIN Judges j ON e.judge_id = j.id " +
                        "INNER JOIN Users u ON j.user_id = u.id " +
                        "WHERE e.blind_signature IS NULL OR e.blind_signature = ''";

                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                JSONArray evaluationsArray = new JSONArray();
                while (resultSet.next()) {
                    JSONObject evaluation = new JSONObject();
                    evaluation.put("evaluation_id", resultSet.getInt("id"));
                    evaluation.put("painting_id", resultSet.getInt("painting_id"));
                    evaluation.put("judge_name", resultSet.getString("judge_name"));

                    // todo: agregar más campos si es necesario

                    evaluationsArray.put(evaluation);
                }

                response.put("response", "200");
                response.put("info", "OK");
                response.put("evaluations", evaluationsArray);

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


    // Obtiene la clave pública RSA del presidente
    public static String getPresidentPublicKey(JSONObject request) {
        JSONObject response = new JSONObject();
        Conexion con = new Conexion();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                // Obtener la clave pública del presidente
                String query = "SELECT p.public_key_rsa FROM Presidents p INNER JOIN Users u ON p.user_id = u.id WHERE u.type = 'president'";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    response.put("response", "200");
                    response.put("info", "OK");
                    response.put("publicKey", resultSet.getString("public_key_rsa"));
                } else {
                    response.put("response", "404");
                    response.put("info", "President not found");
                }

            } catch (SQLException e) {
                System.err.println("Error al obtener la clave pública del presidente: " + e.getMessage());
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


    // Recibe la firma a ciegas generada por el cliente del presidente y la guarda en la base de datos.
    public static String blindSignEvaluation(JSONObject request) {
        String token = request.getString("token");
        int evaluationId = request.getInt("evaluationId");
        String blindSignature = request.getString("blindSignature"); // Firma a ciegas generada en el cliente

        // Validar el token del presidente
        String[] tokenParts = token.split("_");
        if (tokenParts.length != 3 || !tokenParts[1].equals("president")) {
            JSONObject response = new JSONObject();
            response.put("response", "401");
            response.put("info", "Token inválido o expirado");
            return response.toString();
        }
        String presidentId = tokenParts[0]; // Podrías necesitar el ID del presidente

        JSONObject response = new JSONObject();
        try {
            // Guardar la firma a ciegas en la base de datos, en la tabla Evaluations
            if (saveBlindSignatureToDB(evaluationId, presidentId, blindSignature)) {
                response.put("response", "200");
                response.put("info", "OK");
                response.put("blindSignature", blindSignature); // Devolver la firma al cliente para confirmación (opcional)
            } else {
                response.put("response", "500");
                response.put("info", "Error al guardar la firma a ciegas en la base de datos");
            }

        } catch (Exception e) {
            System.err.println("Error al procesar la firma a ciegas: " + e.getMessage());
            response.put("response", "500");
            response.put("info", "Error al procesar la firma a ciegas");
        }

        return response.toString();
    }


    // Guarda la firma a ciegas en la base de datos, asociada a una evaluación específica.
    private static boolean saveBlindSignatureToDB(int evaluationId, String presidentId, String blindSignature) {
        Conexion con = new Conexion();
        Connection conexion = con.conectar();
        boolean success = false;

        if (conexion != null) {
            try {
                // Actualizar la evaluación con la firma a ciegas
                String updateQuery = "UPDATE Evaluations SET blind_signature = ? WHERE id = ?";
                PreparedStatement preparedStatement = conexion.prepareStatement(updateQuery);
                preparedStatement.setString(1, blindSignature);
                preparedStatement.setInt(2, evaluationId);

                int rowsUpdated = preparedStatement.executeUpdate();
                success = rowsUpdated > 0;
            } catch (SQLException e) {
                System.err.println("Error al guardar la firma a ciegas en la base de datos: " + e.getMessage());
            } finally {
                con.cerrar();
            }
        }

        return success;
    }


    // Obtiene la evaluación cegada de la base de datos para que el presidente pueda firmarla.
    public static String getBlindedEvaluationFromDB(JSONObject request) {
        String token = request.getString("token");
        int evaluationId = request.getInt("evaluationId");

        // Validar el token del presidente
        String[] tokenParts = token.split("_");
        if (tokenParts.length != 3 || !tokenParts[1].equals("president")) {
            JSONObject response = new JSONObject();
            response.put("response", "401");
            response.put("info", "Token inválido o expirado");
            return response.toString();
        }

        Conexion con = new Conexion();
        Connection conexion = con.conectar();
        JSONObject response = new JSONObject();

        if (conexion != null) {
            try {
                // Obtener la evaluación cegada de la base de datos
                String query = "SELECT blinded_message FROM Evaluations WHERE id = ?";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, evaluationId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String blindedEvaluation = resultSet.getString("blinded_message");
                    response.put("response", "200");
                    response.put("info", "OK");
                    response.put("blindedEvaluation", blindedEvaluation);
                } else {
                    response.put("response", "404");
                    response.put("info", "Evaluación no encontrada");
                }
            } catch (SQLException e) {
                System.err.println("Error al obtener la evaluación cegada: " + e.getMessage());
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


    // Recibe la firma a ciegas generada por el presidente y la guarda en la base de datos, en la columna blind_signature de la tabla Evaluations, asociada a la evaluationId correspondiente.
    public static String saveBlindedSignature(JSONObject request) {
        String token = request.getString("token");
        int evaluationId = request.getInt("evaluationId");
        String blindSignature = request.getString("blindSignature");

        // Validar el token del presidente
        String[] tokenParts = token.split("_");
        if (tokenParts.length != 3 || !tokenParts[1].equals("president")) {
            JSONObject response = new JSONObject();
            response.put("response", "401");
            response.put("info", "Token inválido o expirado");
            return response.toString();
        }
        String presidentId = tokenParts[0]; // Podrías necesitar el ID del presidente

        Conexion con = new Conexion();
        Connection conexion = con.conectar();
        JSONObject response = new JSONObject();



        if (conexion != null) {
            try {

                String query2 = "SELECT p.public_key_rsa FROM Presidents p INNER JOIN Users u ON p.user_id = u.id WHERE u.type = 'president'";
                PreparedStatement preparedStatement2 = conexion.prepareStatement(query2);
                ResultSet resultSet = preparedStatement2.executeQuery();
                resultSet.next();

                //Get the public key
                String publicKeyString = resultSet.getString("public_key_rsa");
                PublicKey publicKey = FunRsa.getPublicKeyFromBase64(publicKeyString);

                //Get the real evaluation (Server execution)
                String query3 = "SELECT painting_id, stars, comments, inv FROM Evaluations WHERE id = ?";
                PreparedStatement preparedStatement3 = conexion.prepareStatement(query3);
                preparedStatement3.setInt(1, evaluationId);
                ResultSet resultSet2 = preparedStatement3.executeQuery();
                resultSet2.next();

                //Get the public key
                String stars = resultSet2.getString("stars");
                String comments = resultSet2.getString("comments");
                String paintingId = resultSet2.getString("painting_id");
                String inv = resultSet2.getString("inv");
                String evaluationData = paintingId + ";" + stars + ";" + comments;

                String sig = FunBlindSignature.finalize((RSAPublicKey) publicKey, evaluationData, blindSignature, inv);
                System.out.println("Unblind the chairman signature");
                // Actualizar la evaluación con la firma a ciegas
                String updateQuery = "UPDATE Evaluations SET blind_signature = ?, evaluation_signature = ? WHERE id = ?";
                PreparedStatement preparedStatement = conexion.prepareStatement(updateQuery);
                preparedStatement.setString(1, blindSignature);
                preparedStatement.setString(2, sig);
                preparedStatement.setInt(3, evaluationId);

                int rowsUpdated = preparedStatement.executeUpdate();
                if (rowsUpdated > 0) {
                    response.put("response", "200");
                    response.put("info", "OK");
                } else {
                    response.put("response", "500");
                    response.put("info", "Error al guardar la firma a ciegas en la base de datos");
                }
            } catch (SQLException e) {
                System.err.println("Error al guardar la firma a ciegas en la base de datos: " + e.getMessage());
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


}