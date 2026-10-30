import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL = "jdbc:mysql://localhost:3306/retail_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; 

    public static Connection obtenerConexion() throws SQLException {
        try {
            // Forzar la carga del driver JDBC de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver JDBC en las librerías.");
            e.printStackTrace();
        }
        
        Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("¡Conexión a MySQL exitosa!");
        return con;
    }
}