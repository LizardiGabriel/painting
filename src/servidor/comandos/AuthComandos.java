package servidor.comandos;

import org.json.JSONObject;
import servidor.Conexion;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

public class AuthComandos {

    public static String autenticar(JSONObject request) {
        String code = "";
        String info = "";
        String tipo = "";
        String userId = "";
        String token = "";

        String user = request.getString("user");
        String password = request.getString("password");

        Conexion con = new Conexion();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(password.getBytes("UTF-8"));
                String query = "SELECT id, type, nombre FROM Users WHERE user = ? AND password = ?";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setString(1, user);
                preparedStatement.setString(2, Base64.getEncoder().encodeToString(hash));
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    System.out.println("Usuario autenticado");
                    code = "200";
                    info = "OK";
                    userId = resultSet.getString("id");
                    tipo = resultSet.getString("type");

                    // Generar el token
                    token = generateToken(userId, tipo);

                } else {
                    System.out.println("Usuario no autenticado");
                    code = "401";
                    info = "Unauthorized";
                }
            } catch (SQLException e) {
                System.err.println("Error al autenticar: " + e.getMessage());
                code = "500";
                info = "Internal Server Error";
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                System.err.println("Error al autenticar: " + e.getMessage());
                code = "500";
                info = "Internal Server Error";
            }finally {
                con.cerrar();
                System.out.println("Conexión cerrada");
            }
        } else {
            System.out.println("Error en la conexión");
            code = "500";
            info = "Internal Server Error";
        }

        JSONObject response = new JSONObject();
        response.put("response", code);
        response.put("info", info);
        response.put("token", token);
        response.put("userType", tipo);
        response.put("userId", userId);

        return response.toString();
    }


    public static String generateToken(String userId, String userType) {
        return userId + "_" + userType + "_" + UUID.randomUUID().toString();
    }
}