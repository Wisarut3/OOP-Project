import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterForm extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnReg;

    public RegisterForm() {
        setTitle("Register System");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel(" Username:"));
        txtUser = new JTextField();
        add(txtUser);

        add(new JLabel(" Password:"));
        txtPass = new JPasswordField();
        add(txtPass);

        btnReg = new JButton("Register");
        add(btnReg);

        btnReg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveToDatabase();
            }
        });

        setLocationRelativeTo(null);
    }

    private void saveToDatabase() {
        String user = txtUser.getText();
        String pass = new String(txtPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Success!");
            new LoginForm().setVisible(true);
            this.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "User already exists!");
        }
    }
}