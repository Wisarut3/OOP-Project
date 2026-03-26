import java.sql.*;

public class DB {
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/user_db", "root", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}