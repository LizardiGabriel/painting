package servidor;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioComandos {

    public static String usuarioExiste(JSONObject request) {
        String code = "";
        String info = "";

        String user = request.getString("user");

        Con con = new Con();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                String query = "SELECT * FROM Users WHERE user = ?";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setString(1, user);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    System.out.println("usuario ya existe");
                    code = "200";
                    info = "OK";
                } else {
                    System.out.println("usuario no existe");
                    code = "401";
                    info = "Usuario no existe";

                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                con.cerrar();
                System.out.println("conexion cerrado");
            }
        } else {
            System.out.println("error en la conexion");
            // internal server error
            code = "500";
            info = "Internal Server Error";
        }

        JSONObject response = new JSONObject();
        response.put("response", code);
        response.put("info", info);
        return response.toString();
    }
}