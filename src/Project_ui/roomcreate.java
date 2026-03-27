package Project_ui;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class roomcreate extends JFrame {

    public roomcreate(JFrame owner) {
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        setContentPane(bg);
        bg.setLayout(null);

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

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int w = bg.getWidth();
                int h = bg.getHeight();
                int panelX = (w - pWidth) / 2;
                int panelY = (h - pHeight) / 2 + 60;
                loginPanel.setBounds(panelX, panelY, pWidth, pHeight);

                int logoW = 200;
                int logoH = (originalIcon.getIconWidth() > 0) ? (logoW * originalIcon.getIconHeight()) / originalIcon.getIconWidth() : 100;
                Image scaledLogo = originalIcon.getImage().getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaledLogo));
                logoLabel.setBounds((w - logoW) / 2, panelY - logoH - 10, logoW, logoH);

                closeBtn.setBounds(w - 50, 10, 40, 40);
            }
        });
    }

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
            int offset = 500;
            int yRight = (y + offset) % effectiveHeight;
            g2d.drawImage(cardImg, 50, y, cardWidth, cardHeight, this);
            g2d.drawImage(cardImg, 50, y - effectiveHeight, cardWidth, cardHeight, this);
            g2d.drawImage(cardImg, w - 50 - cardWidth, yRight, cardWidth, cardHeight, this);
            g2d.drawImage(cardImg, w - 50 - cardWidth, yRight - effectiveHeight, cardWidth, cardHeight, this);

            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(null);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 18);
        int fieldArc = 35;

        JLabel uL = new JLabel();
        uL.setBounds(50, 40, 150, 25);
        uL.setFont(labelFont);
        panel.add(uL);

        JTextField uF = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, fieldArc, fieldArc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        uF.setBounds(50, 70, 300, 40);
        uF.setOpaque(false);
        uF.setBorder(new RoundedBorder(fieldArc));
        panel.add(uF);


        // ปรับสีปุ่มให้ตรงตามรูป (แดงเลือดหมูไล่เฉด)
        JButton btnL = createStyledButton("Create room", new Color(120, 0, 0));
        btnL.setBounds(50, 130, 300, 50);
        panel.add(btnL);

        JButton btnS = createStyledButton("Join room", new Color(90, 0, 0));
        btnS.setBounds(50, 190, 300, 50);
        panel.add(btnS);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int arc = 40; // ความมนเท่ากับ Field
                
                // วาดเงาหรือขอบมืดด้านล่างเล็กน้อย
                g2.setColor(color.darker());
                g2.fillRoundRect(0, 2, getWidth(), getHeight() - 2, arc, arc);
                
                // วาดสีปุ่มหลัก
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 4, arc, arc);
                
                // วาดข้อความ
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
        
        // เอฟเฟกต์ตอนเอาเมาส์วาง
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(color.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(color); }
        });
        
        return btn;
    }

    class RoundedBorder extends AbstractBorder {
        private int radius;
        RoundedBorder(int radius) { this.radius = radius; }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(5, 20, 5, 20); }
    }
}