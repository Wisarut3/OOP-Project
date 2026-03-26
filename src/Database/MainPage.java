import javax.swing.*;
import java.awt.*;

public class MainPage extends JFrame {
    private JLabel lblWelcome;
    private JButton btnLogout;

    public MainPage() {
        setTitle("Main System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        lblWelcome = new JLabel(LoginForm.currentUsername, SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Tahoma", Font.BOLD, 24));
        lblWelcome.setForeground(new Color(0, 102, 204));
        add(lblWelcome, BorderLayout.CENTER);

        btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            this.dispose();
        });
        add(btnLogout, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }
}