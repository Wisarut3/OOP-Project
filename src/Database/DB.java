package Database;
import java.sql.*;

public class DB {
    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://root:WbDIAdoHGvuSDMQYzsdgdGLKQNiSyKll@gondola.proxy.rlwy.net:34173/railway";
            String user = "root";
            String pass = "WbDIAdoHGvuSDMQYzsdgdGLKQNiSyKll";
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}