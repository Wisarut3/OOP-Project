package Database;
import java.sql.*;
import javax.swing.*;

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
    
    public static int checkLogin(String user, String pass) {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user);
            pstmt.setString(2, pass);

            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String currentUsername = rs.getString("username");
                JOptionPane.showMessageDialog(null, "Login Success! Welcome " + currentUsername);
                return rs.getInt("win_rate");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Username or Password!");
                return -1;
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
            return -1;
        }
    }
    
    public static void saveToDatabase(String user, String pass) {
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields!");
            return;
        }

        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Success!");
        } catch (SQLException ex) {
            //ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "User already exists!");
        }
    }
}