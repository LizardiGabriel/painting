package servidor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PresidentComandos {

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

        Con con = new Con();
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
}