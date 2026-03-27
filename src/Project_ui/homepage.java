package Project_ui;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class homepage extends JFrame {

    public homepage(String user, String gamewin) {

        // Fullscreen window
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Root panel
        BackgroundPanel bg = new BackgroundPanel();
        setContentPane(bg);
        bg.setLayout(null);

        ////////////////// UI ELEMENTS //////////////////

        InfoLabel userLabel = new InfoLabel("User : " + user);
        InfoLabel winrateLabel = new InfoLabel("Games won : " + gamewin);
        bg.add(userLabel);
        bg.add(winrateLabel);

        ImageIcon originalIcon = new ImageIcon("logo.png");
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

        ////////////////// RESPONSIVE LAYOUT //////////////////

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {

                int w = bg.getWidth();
                int h = bg.getHeight();

                userLabel.setBounds(0, 0, 200, 50);
                winrateLabel.setBounds(w - 200, 0, 200, 50);

                int panelX = (w - pWidth) / 2;
                int panelY = (h - pHeight) / 2 + 60;
                loginPanel.setBounds(panelX, panelY, pWidth, pHeight);

                int logoW = 200;
                int logoH = (originalIcon.getIconWidth() > 0)
                        ? (logoW * originalIcon.getIconHeight()) / originalIcon.getIconWidth()
                        : 100;

                Image scaledLogo = originalIcon.getImage()
                        .getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);

                logoLabel.setIcon(new ImageIcon(scaledLogo));
                logoLabel.setBounds((w - logoW) / 2, panelY - logoH - 10, logoW, logoH);

                closeBtn.setBounds(w - 50, 60, 40, 40);
            }
        });
    }

    ////////////////// CUSTOM LABEL //////////////////

        class InfoLabel extends JPanel {

    private String text;

    public InfoLabel(String text) {
        this.text = text;
    }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            // text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();

            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 4;

            g2.drawString(text, x, y);

            g2.dispose();
        }
    }

    ////////////////// BACKGROUND + ANIMATION //////////////////

    class BackgroundPanel extends JPanel {

        int y = 0;

        Image bgImg, cardImg;

        int cardHeight = 1000;
        int overlap = 140;
        int effectiveHeight = cardHeight - overlap;

        public BackgroundPanel() {

            bgImg = new ImageIcon("loginbg.png").getImage();
            cardImg = new ImageIcon("cardsblur.png").getImage();

            new Timer(16, e -> {
                y += 1;
                if (y >= effectiveHeight) y = 0;
                repaint();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();

            g2d.drawImage(bgImg, 0, 0, w, h, this);

            int cardWidth = 350;

            g2d.drawImage(cardImg, 50, y, cardWidth, cardHeight, this);
            g2d.drawImage(cardImg, 50, y - effectiveHeight, cardWidth, cardHeight, this);

            g2d.drawImage(cardImg, w - 50 - cardWidth, y, cardWidth, cardHeight, this);
            g2d.drawImage(cardImg, w - 50 - cardWidth, y - effectiveHeight, cardWidth, cardHeight, this);

            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(0, 0, w, h);

            g2d.dispose();
        }
    }

    ////////////////// CENTER PANEL //////////////////

    private JPanel createLoginPanel() {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
            }
        };

        panel.setOpaque(false);
        panel.setLayout(null);

        JButton btnL = createStyledButton("Play with Bot", new Color(120, 0, 0));
        btnL.setBounds(50, 150, 300, 50);
        panel.add(btnL);

        JButton btnS = createStyledButton("Play with Friends", new Color(90, 0, 0));
        btnS.setBounds(50, 220, 300, 50);
        panel.add(btnS);

        return panel;
    }

    ////////////////// CUSTOM BUTTON //////////////////

    private JButton createStyledButton(String text, Color color) {

        JButton btn = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    ////////////////// ENTRY POINT //////////////////

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new homepage().setVisible(true));
//    }
}