package Project_ui;

import javax.swing.*;
import java.awt.*;
import Database.DB;

public class HomePage extends JFrame {
    private String gamewin;
    public HomePage(String username, String gamewin) {
        this.gamewin = String.valueOf(gamewin);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        setContentPane(bg);
        bg.setLayout(null);

        InfoLabel userLabel = new InfoLabel("User: " + username);
        InfoLabel winLabel = new InfoLabel("Games won: " + gamewin);
        bg.add(userLabel);
        bg.add(winLabel);

        ImageIcon icon = new ImageIcon("logo.png");
        JLabel logo = new JLabel(icon);
        bg.add(logo);

        JButton close = new JButton("✕");
        close.setForeground(Color.WHITE);
        close.setBackground(new Color(255, 80, 80));
        close.setFocusPainted(false);
        close.setBorderPainted(false);
        close.addActionListener(e -> System.exit(0));
        bg.add(close);

        JPanel panel = buildPanel(username);
        int pw = 400, ph = 320;
        bg.add(panel);

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = bg.getWidth(), h = bg.getHeight();
                userLabel.setBounds(0, 0, 220, 50);
                winLabel.setBounds(w - 220, 0, 220, 50);
                close.setBounds(w - 50, 10, 50, 40);

                int px = (w - pw) / 2, py = (h - ph) / 2 + 60;
                panel.setBounds(px, py, pw, ph);

                int lw = 200, lh = (icon.getIconWidth() > 0)
                        ? (lw * icon.getIconHeight()) / icon.getIconWidth() : 80;
                logo.setIcon(new ImageIcon(icon.getImage().getScaledInstance(lw, lh, Image.SCALE_SMOOTH)));
                logo.setBounds((w - lw) / 2, py - lh - 10, lw, lh);
            }
        });
    }

    private JPanel buildPanel(String username) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
            }
        };
        p.setOpaque(false);
        p.setLayout(null);

        JButton btnSolo = btn("Play with Bot", new Color(120, 0, 0));
        btnSolo.setBounds(50, 80, 300, 55);
        btnSolo.addActionListener(e -> {
            // Solo mode: GameUI ไม่มี client → null
            GameUI ui = new GameUI(this, null, username);
            ui.setVisible(true);
            setVisible(false);
        });
        p.add(btnSolo);

        JButton btnOnline = btn("Play with Friends", new Color(90, 0, 0));
        btnOnline.setBounds(50, 160, 300, 55);
        btnOnline.addActionListener(e -> {
            new RoomCreate(this, username).setVisible(true);
            setVisible(false);
        });
        p.add(btnOnline);

        return p;
    }

    private JButton btn(String t, Color c) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.darker());
                g2.fillRoundRect(0, 2, getWidth(), getHeight() - 2, 40, 40);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 4, 40, 40);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent()) / 2 - 5);
                g2.dispose();
            }
        };
        b.setBackground(c);
        b.setFont(new Font("Segoe UI", Font.BOLD, 18));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(c.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(c);
            }
        });
        return b;
    }

    class InfoLabel extends JPanel {

        String text;

        InfoLabel(String t) {
            text = t;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() + fm.getAscent()) / 2 - 4);
            g2.dispose();
        }
    }

    class BackgroundPanel extends JPanel {

        int y = 0;
        Image bg, card;
        int ch = 1000, ov = 140, eff = ch - ov;

        BackgroundPanel() {
            bg = new ImageIcon("loginbg.png").getImage();
            card = new ImageIcon("cardsblur.png").getImage();
            new Timer(16, e -> {
                y = (y + 1) % eff;
                repaint();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight(), cw = 350;
            g2.drawImage(bg, 0, 0, w, h, this);
            g2.drawImage(card, 50, y, cw, ch, this);
            g2.drawImage(card, 50, y - eff, cw, ch, this);
            g2.drawImage(card, w - 50 - cw, y, cw, ch, this);
            g2.drawImage(card, w - 50 - cw, y - eff, cw, ch, this);
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }
}
