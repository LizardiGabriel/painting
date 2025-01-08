package servidor.comandos;

import general.FunEcdsa;
import org.json.JSONObject;
import servidor.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RegistroComandos {

    static String terminos = "Aqui van los terminos y condiciones";

    public static String registrar_pintor(JSONObject request) {
        String ret = "";
        String info = "";

        String token = request.getString("token");
        String user = request.getString("user");
        String firma = request.getString("firma");
        String public_key_ecdsa = request.getString("pub");
        String nombre = request.getString("nombre");
        String password = request.getString("password");

        // si cualquier campo está vacío, retornar error
        if (token.isEmpty() || user.isEmpty() || firma.isEmpty() || public_key_ecdsa.isEmpty() || nombre.isEmpty() || password.isEmpty()) {
            ret = "400";
            info = "Bad Request";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        // Validar el token
        if (!validarTokenAdmin(token)) {
            ret = "401";
            info = "Token inválido o expirado";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        // se verifica la firma con el mensaje, la firma y la clave publica
        boolean verificado = FunEcdsa.verificarECDSA(terminos, firma, public_key_ecdsa);

        if (!verificado) {
            System.out.println("Firma no verificada");
            ret = "401";
            info = "Unauthorized";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        } else {
            System.out.println("--- --- --- Firma verificada :) --- --- ---");
        }

        Conexion con = new Conexion();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                // Insertar en la tabla Users
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
                        query = "INSERT INTO Painters (user_id, public_key_ecdsa) VALUES (?, ?)";
                        preparedStatement = conexion.prepareStatement(query);
                        preparedStatement.setInt(1, userId);
                        preparedStatement.setString(2, public_key_ecdsa);
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
                System.out.println("Error SQL: " + e.getMessage());
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

    public static String registrar_juez(JSONObject request) {
        String ret = "";
        String info = "";

        String token = request.getString("token");
        String user = request.getString("user");
        String public_key_rsa_oaep = request.getString("clave_publica_rsaOAP");
        String nombre = request.getString("nombre");
        String password = request.getString("password");

        // si cualquier campo está vacío, retornar error
        if (token.isEmpty() || user.isEmpty() || public_key_rsa_oaep.isEmpty() || nombre.isEmpty() || password.isEmpty()) {
            ret = "400";
            info = "Bad Request";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        // Validar el token
        if (!validarTokenAdmin(token)) {
            ret = "401";
            info = "Token inválido o expirado";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        Conexion con = new Conexion();
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
                        int userId = generatedKeys.getInt(1);

                        // Insertar en la tabla Judges
                        query = "INSERT INTO Judges (user_id, public_key_rsa_oaep) VALUES (?, ?)";
                        preparedStatement = conexion.prepareStatement(query);
                        preparedStatement.setInt(1, userId);
                        preparedStatement.setString(2, public_key_rsa_oaep);
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
                System.out.println("Error SQL: " + e.getMessage());
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


    public static String registrar_presidente(JSONObject request) {
        String ret = "";
        String info = "";

        String token = request.getString("token");
        String user = request.getString("user");
        String password = request.getString("password");
        String nombre = request.getString("nombre");
        String publicKey = request.getString("publicKey"); // Recibir la clave pública del cliente

        // si cualquier campo está vacío, retornar error
        if (token.isEmpty() || user.isEmpty() || password.isEmpty() || nombre.isEmpty() || publicKey.isEmpty()) {
            ret = "400";
            info = "Bad Request";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        // Validar el token
        if (!validarTokenAdmin(token)) {
            ret = "401";
            info = "Token inválido o expirado";
            JSONObject response = new JSONObject();
            response.put("response", ret);
            response.put("info", info);
            return response.toString();
        }

        Conexion con = new Conexion();
        Connection conexion = con.conectar();

        if (conexion != null) {
            try {
                // Insertar en la tabla Users
                String query = "INSERT INTO Users (user, password, type, nombre) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, user);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, "president");
                preparedStatement.setString(4, nombre);
                int rows = preparedStatement.executeUpdate();

                if (rows > 0) {
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);

                        // Insertar en la tabla Presidents
                        query = "INSERT INTO Presidents (user_id, public_key_rsa) VALUES (?, ?)";
                        preparedStatement = conexion.prepareStatement(query);
                        preparedStatement.setInt(1, userId);
                        preparedStatement.setString(2, publicKey); // Guardar la clave pública
                        rows = preparedStatement.executeUpdate();

                        if (rows > 0) {
                            System.out.println("Presidente registrado");
                            ret = "200";
                            info = "OK";
                        } else {
                            System.out.println("Error al registrar presidente");
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
                System.out.println("Error SQL: " + e.getMessage());
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



    private static boolean validarTokenAdmin(String token) {
        // si el token no está vacío y tiene el formato correcto
        if (token == null || token.isEmpty() || !token.contains("_")) {
            return false;
        }
        String[] parts = token.split("_");
        return parts.length == 3 && parts[1].equals("admin");
    }





}