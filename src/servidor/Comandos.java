package servidor;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Comandos {

    String requestJson;
    JSONObject request;

    public Comandos(String requestJson) {
        this.requestJson = requestJson;
        request = new JSONObject(requestJson);
    }

    public String autenticar() {

        String code = "";
        String info = "";

        String user = request.getString("user");
        String password = request.getString("password");

        Con con = new Con();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                String query = "SELECT * FROM Users WHERE user = ? AND password = ?";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setString(1, user);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    System.out.println("usuario autenticado");
                    // ok
                    code = "200";
                    info = "OK";
                } else {
                    System.out.println("usuario no autenticado");
                    // unauthorized
                    code = "401";
                    info = "Unauthorized";

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

    public String registrar(String tipo) {
        String ret = "";
        String info = "";

        String user = request.getString("user");
        String password = request.getString("password");

        Con con = new Con();
        Connection conexion = con.conectar();

        if (conexion != null){
            try {
                String query = "INSERT INTO Users (user, password) VALUES (?, ?)";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setString(1, user);
                preparedStatement.setString(2, password);
                int rows = preparedStatement.executeUpdate();

                if (rows > 0){
                    System.out.println("usuario registrado");
                    ret = "200";
                    info = "OK";
                }else{
                    System.out.println("error al registrar");
                    ret = "500";
                    info = "Internal Server Error";
                }

                // insertar dentro de la tabla de pintores o de jueces segun el tipo


                // ...


                // ...


            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                con.cerrar();
                System.out.println("conexion cerrada");
            }
        }else{
            System.out.println("error en la conexion");
            ret = "500";
            info = "Internal Server Error";
        }

        JSONObject response = new JSONObject();
        response.put("response", ret);
        response.put("info", info);
        return response.toString();

    }


}
