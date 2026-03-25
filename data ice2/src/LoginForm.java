import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginForm extends JFrame {
    public static String currentUsername = ""; 

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JButton btnGoReg;

    public LoginForm() {
        setTitle("Login System");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("  Username:"));
        txtUser = new JTextField();
        add(txtUser);

        add(new JLabel("  Password:"));
        txtPass = new JPasswordField();
        add(txtPass);

        btnLogin = new JButton("Login");
        add(btnLogin);
        btnLogin.addActionListener(e -> checkLogin());

        btnGoReg = new JButton("Go to Register");
        add(btnGoReg);
        btnGoReg.addActionListener(e -> {
            new RegisterForm().setVisible(true);
            this.dispose();
        });

        setLocationRelativeTo(null);
    }

    private void checkLogin() {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtUser.getText());
            pstmt.setString(2, new String(txtPass.getPassword()));

            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                currentUsername = txtUser.getText();
                JOptionPane.showMessageDialog(this, "Login Success! Welcome " + currentUsername);
                new MainPage().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        new LoginForm().setVisible(true);
    }
}