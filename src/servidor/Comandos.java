package servidor;

import general.FunEcdsa;


import org.json.JSONObject;

import java.sql.*;

public class Comandos {

    String requestJson;
    JSONObject request;
    String terminos = "Aqui van los terminos y condiciones";

    public Comandos(String requestJson) {
        this.requestJson = requestJson;
        request = new JSONObject(requestJson);
    }



    /**
    * Modificar para que el token te regrese: el tipo de usuario, el user y el nombre
     * @return JSON con la respuesta
     *
    * */
    public String autenticar() {

        String code = "";
        String info = "";
        String tipo = "";
        String nombre = "";


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
                    code = "200";
                    info = "OK";
                    nombre = resultSet.getString("nombre");
                    tipo = resultSet.getString("type");

                } else {
                    System.out.println("usuario no autenticado");
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

        JSONObject token = new JSONObject();
        token.put("type", tipo);
        token.put("user", user);
        token.put("nombre", nombre);

        JSONObject response = new JSONObject();
        response.put("response", code);
        response.put("info", info);
        response.put("type", tipo);
        response.put("token", token.toString());

        return response.toString();


    }

    public String usuarioExiste(){

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


    public String registrar_pintor() {
        String ret = "";
        String info = "";

        String user = request.getString("user");
        String password = request.getString("password");
        String firma = request.getString("firma");
        String clave_publica_ecdsa = request.getString("pub");
        String nombre = request.getString("nombre");

        // si cualquiera de los campos esta vacio retornar error
        if (user.equals("") || password.equals("") || firma.equals("") || clave_publica_ecdsa.equals("") || nombre.equals("")) {
            ret = "400";
            info = "Bad Request";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }


        // se verifica la firma con el mensaje, la firma y la clave publica
        boolean verificado = FunEcdsa.verificarECDSA(terminos, firma, clave_publica_ecdsa);

        if (!verificado) {
            System.out.println("Firma no verificada");
            ret = "401";
            info = "Unauthorized";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }else {
            System.out.println("--- --- --- Firma verificada :) --- --- ---");
        }



        Con con = new Con();
        Connection conexion = con.conectar();


        if (conexion != null) {
            try {
                String query = "INSERT INTO Users (user, password, type, nombre) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, user);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, "painter");
                preparedStatement.setString(4, nombre);
                int rows = preparedStatement.executeUpdate();

                if (rows > 0) {
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {

                        int userId = generatedKeys.getInt(1);
                        // Insertar en la tabla Painters
                        query = "INSERT INTO Painters (user_id, clave_publica_ecdsa) VALUES (?, ?)";
                        preparedStatement = conexion.prepareStatement(query);
                        preparedStatement.setInt(1, userId);
                        preparedStatement.setString(2, clave_publica_ecdsa);
                        rows = preparedStatement.executeUpdate();

                        if (rows > 0) {
                            System.out.println("Pintor registrado");
                            ret = "200";
                            info = "OK";
                        } else {
                            System.out.println("Error al registrar pintor");
                            ret = "500";
                            info = "Internal Server Error";
                        }
                    }
                } else {
                    System.out.println("Error al registrar usuario");
                    ret = "500";
                    info = "Internal Server Error";
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                con.cerrar();
                System.out.println("Conexion cerrada");
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


    public String registrar_juez() {
        String ret = "";
        String info = "";

        String user = request.getString("user");
        String password = request.getString("password");
        String clave_publica_rsaOAP = request.getString("clave_publica_rsaOAP");
        String nombre = request.getString("nombre");

        if (user.equals("") || password.equals("") || clave_publica_rsaOAP.equals("") || nombre.equals("")) {
            ret = "400";
            info = "Bad Request";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        Con con = new Con();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                // Insertar en la tabla Users
                String query = "INSERT INTO Users (user, password, type, nombre) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, user);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, "judge");
                preparedStatement.setString(4, nombre);
                int rows = preparedStatement.executeUpdate();

                if (rows > 0) {
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {

                        // Obtener el ID del usuario recién insertado
                        int userId = generatedKeys.getInt(1);
                        // Insertar en la tabla Judges
                        query = "INSERT INTO Judges (user_id, clave_publica_rsaOAP) VALUES (?, ?)";
                        preparedStatement = conexion.prepareStatement(query);
                        preparedStatement.setInt(1, userId);
                        preparedStatement.setString(2, clave_publica_rsaOAP);
                        rows = preparedStatement.executeUpdate();

                        if (rows > 0) {
                            System.out.println("Juez registrado");
                            ret = "200";
                            info = "OK";
                        } else {
                            System.out.println("Error al registrar juez");
                            ret = "500";
                            info = "Internal Server Error";
                        }
                    }
                } else {
                    System.out.println("Error al registrar usuario");
                    ret = "500";
                    info = "Internal Server Error";
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                con.cerrar();
                System.out.println("Conexion cerrada");
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







    public String obtenerTYC() {
        JSONObject response = new JSONObject();
        response.put("response", "200");
        response.put("info", "OK");
        response.put("tyc", terminos);


        return response.toString();
    }


    /*  ejemplo de json que se debe regresar
        {
            "user_Juez1": "clave123",
            "Juez2": "clave456",
            "Juez3": "clave789"
        }
    */
    public String getJuecesRsaPublicKeys() {

        String ret = "";
        String info = "";


        JSONObject response = new JSONObject();

        Con con = new Con();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                String query = "SELECT Users.user, Judges.clave_publica_rsaOAP FROM Judges " +
                        "JOIN Users ON Judges.user_id = Users.id";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String user = resultSet.getString("user");
                    String clave = resultSet.getString("clave_publica_rsaOAP");
                    response.put(user, clave);

                }

                ret = "200";
                info = "OK";

                JSONObject responseFinal = new JSONObject();
                responseFinal.put("response", ret);
                responseFinal.put("info", info);
                responseFinal.put("llaves_publicas", response.toString());

                return responseFinal.toString();


            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                con.cerrar();
                System.out.println("Conexion cerrada");
            }
        } else {
            System.out.println("Error en la conexión");
        }

        JSONObject retError = new JSONObject();
        retError.put("response", "500");
        retError.put("info", "Internal Server Error");
        return retError.toString();

    }
}
