package servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Con {
    // esta clase va a ser para conectar con la bd

    String host = "localhost";
    String puerto = "3306";
    String bd = "pinturas";

    String url = "jdbc:mysql://"+host+":"+puerto+"/"+bd;
    String user = "root";
    String pass = "";


    public Connection conectar() {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(url, user, pass);
            if (conexion != null) {
                System.out.println("Conexión exitosa");
            }else{
                System.out.println("Conexión fallida");
            }

        } catch (SQLException e) {
            System.out.println("Error en la conexión: " + e.getMessage());
        }
        return conexion;
    }



}
