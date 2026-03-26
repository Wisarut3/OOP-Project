import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class loginpage2 extends JFrame {

    public loginpage2() {

        // fullscreen
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        setContentPane(bg);
        bg.setLayout(null);

        ImageIcon originalIcon = new ImageIcon("src\\Project_ui\\logo.png");
        JLabel logoLabel = new JLabel(originalIcon);
        bg.add(logoLabel);

        JButton closeBtn = new JButton("✕");
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(255, 80, 80));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(e -> System.exit(0));
        bg.add(closeBtn);

        JPanel loginPanel = createLoginPanel();
        int pWidth = 400;
        int pHeight = 450;
        bg.add(loginPanel);

        ////////////////// RESIZE OK //////////////////
        
        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {

                int w = bg.getWidth();
                int h = bg.getHeight();

                loginPanel.setBounds((w - pWidth) / 2, (h - pHeight) / 2 + 60, pWidth, pHeight);

                int logoW = 200;
                int logoH = (originalIcon.getIconWidth() > 0)
                        ? (logoW * originalIcon.getIconHeight()) / originalIcon.getIconWidth()
                        : 100;

                Image scaledLogo = originalIcon.getImage()
                        .getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);

                logoLabel.setIcon(new ImageIcon(scaledLogo));
                logoLabel.setBounds((w - logoW) / 2, (h - pHeight) / 2 - logoH, logoW, logoH);

                closeBtn.setBounds(w - 50, 10, 50, 40);
            }
        });
    }

    ////////////////// BACKGROUND ////////////////// for animation + custom draw

    class BackgroundPanel extends JPanel {

        int y = 0;

        Image bgImg = new ImageIcon("src\\Project_ui\\loginbg.png").getImage();
        Image cardImg = new ImageIcon("src\\Project_ui\\cardsblur.png").getImage();

        int cardHeight = 1000;
        int overlap = 140;
        int effectiveHeight = cardHeight - overlap;

        public BackgroundPanel() {

            // loop
            new Timer(16, e -> {
                y += 1;
                if (y >= effectiveHeight) y = 0;
                repaint();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();

            g2.drawImage(bgImg, 0, 0, w, h, this);

            int cardWidth = 350;

            // loop card
            g2.drawImage(cardImg, 50, y, cardWidth, cardHeight, this);
            g2.drawImage(cardImg, 50, y - effectiveHeight, cardWidth, cardHeight, this);

            g2.drawImage(cardImg, w - 50 - cardWidth, y, cardWidth, cardHeight, this);
            g2.drawImage(cardImg, w - 50 - cardWidth, y - effectiveHeight, cardWidth, cardHeight, this);

            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    }

    ////////////////// LOGIN PANEL ////////////////// for grouping UI

    private JPanel createLoginPanel() {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setColor(new Color(150, 0, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);

                g2.setColor(new Color(210, 210, 210));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 45, 45);

                g2.dispose();
            }
        };

        panel.setOpaque(false);
        panel.setLayout(null);

        Font font = new Font("Segoe UI", Font.BOLD, 18);
        int arc = 35;

        JLabel uL = new JLabel("username");
        uL.setBounds(50, 40, 150, 25);
        uL.setFont(font);
        panel.add(uL);

        JTextField uF = createField(arc);
        uF.setBounds(50, 70, 300, 40);
        panel.add(uF);

        JLabel pL = new JLabel("password");
        pL.setBounds(50, 130, 150, 25);
        pL.setFont(font);
        panel.add(pL);

        JTextField pF = createField(arc);
        pF.setBounds(50, 160, 300, 40);
        panel.add(pF);

        JButton btnL = createStyledButton("Login", new Color(120, 0, 0));
        btnL.setBounds(50, 250, 300, 50);
        panel.add(btnL);

        JButton btnS = createStyledButton("Signin", new Color(90, 0, 0));
        btnS.setBounds(50, 320, 300, 50);
        panel.add(btnS);

        return panel;
    }

    ////////////////// FIELD ////////////////// for reusable rounded input

    private JTextField createField(int arc) {

        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        field.setOpaque(false);
        field.setBorder(new RoundedBorder(arc));

        return field;
    }

    ////////////////// BUTTON ////////////////// for custom UI button

    private JButton createStyledButton(String text, Color color) {

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                int arc = 40;

                g2.setColor(color.darker());
                g2.fillRoundRect(0, 2, getWidth(), getHeight() - 2, arc, arc);

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 4, arc, arc);

                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();

                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 5;

                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setBackground(color);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));

        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(color.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(color); }
        });

        return btn;
    }

    ////////////////// BORDER ////////////////// for padding inside #Userfield

    class RoundedBorder extends AbstractBorder {

        int r;

        RoundedBorder(int r) { this.r = r; }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(5, 20, 5, 20);
        }
    }

    ////////////////// MAIN //////////////////

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new loginpage2().setVisible(true));
    }
} 