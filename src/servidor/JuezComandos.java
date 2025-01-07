package servidor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JuezComandos {

    public static String getJuecesRsaPublicKeys(JSONObject request) {
        JSONObject response = new JSONObject();
        Con con = new Con();
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

        Con con = new Con();
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
                        "WHERE ek.judge_id = ?";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, Integer.parseInt(judgeId));
                ResultSet resultSet = preparedStatement.executeQuery();

                JSONArray paintingsArray = new JSONArray();
                while (resultSet.next()) {
                    JSONObject painting = new JSONObject();
                    painting.put("id", resultSet.getInt("id"));
                    painting.put("file_path", resultSet.getString("encrypted_painting_data"));
                    painting.put("painter_name", resultSet.getString("painter_name"));
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


    // el juez solicita la clave AES cifrada y el IV para una pintura
    public static String getEncryptedAESKeyAndIV(JSONObject request){
        String token = request.getString("token");
        String[] tokenParts = token.split("_");
        String judgeId = tokenParts[0];

        String paintingId = request.getString("painting_id");

        Con con = new Con();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                // Obtener la clave AES cifrada y el IV para la pintura
                String query = "SELECT ek.encrypted_aes_key, p.iv, p.encrypted_painting_data " +
                        "FROM Encrypted_AES_Keys ek " +
                        "INNER JOIN Paintings p ON ek.painting_id = p.id " +
                        "WHERE ek.painting_id = ? AND ek.judge_id = ?";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, Integer.parseInt(paintingId));
                preparedStatement.setInt(2, Integer.parseInt(judgeId));
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

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            JSONObject response = new JSONObject();
            response.put("response", "500");
            response.put("info", "Internal Server Error");
            return response.toString();
        }

    }
}