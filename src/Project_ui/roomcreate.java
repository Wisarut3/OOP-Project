package Project_ui;

import Main.GameClient;
import Main.GameServer;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * roomcreate — หน้าสร้าง/เข้าร่วมห้อง
 *
 * Create room: 1. กรอก Server IP (ถ้าเป็น host เดียวกันใช้ localhost) 2.
 * กรอกจำนวนคน (1-3) 3. กด "Create Room" → แสดง Room Code บนหน้าจอ →
 * รอคนอื่นเข้า
 *
 * Join room: 1. กรอก Server IP ของคน host 2. กรอก Room Code 4 ตัว 3. กด "Join
 * Room" → เข้าร่วมห้องทันที
 */
public class roomcreate extends JFrame {

    private final JFrame owner;
    private final String username;

    // ── UI refs ──────────────────────────────────────────────────────────
    private JTextField tfIp, tfCode, tfSize;
    private JLabel lblRoomCode, lblStatus;
    private JButton btnCreate, btnJoin;

    public roomcreate(JFrame owner, String username) {
        this.owner = owner;
        this.username = username;

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        setContentPane(bg);
        bg.setLayout(null);

        // Logo
        ImageIcon icon = new ImageIcon("logo.png");
        JLabel logo = new JLabel(icon);
        bg.add(logo);

        // Close
        JButton close = new JButton("✕");
        styleClose(close);
        close.addActionListener(e -> System.exit(0));
        bg.add(close);

        // Back
        JButton back = styledBtn("← Back", new Color(80, 80, 80));
        back.addActionListener(e -> {
            owner.setVisible(true);
            dispose();
        });
        bg.add(back);

        // Main panel
        JPanel panel = buildMainPanel();
        int pw = 480, ph = 520;
        bg.add(panel);

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = bg.getWidth(), h = bg.getHeight();
                close.setBounds(w - 50, 10, 40, 40);
                back.setBounds(10, 10, 100, 36);

                int px = (w - pw) / 2, py = (h - ph) / 2 + 40;
                panel.setBounds(px, py, pw, ph);

                int lw = 200, lh = (icon.getIconWidth() > 0)
                        ? (lw * icon.getIconHeight()) / icon.getIconWidth() : 80;
                logo.setIcon(new ImageIcon(icon.getImage().getScaledInstance(lw, lh, Image.SCALE_SMOOTH)));
                logo.setBounds((w - lw) / 2, py - lh - 10, lw, lh);
            }
        });
    }

    // ── Build main panel ─────────────────────────────────────────────────
    private JPanel buildMainPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(150, 0, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.setColor(new Color(220, 220, 220));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 46, 46);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(null);

        Font bold = new Font("Segoe UI", Font.BOLD, 16);
        Font big = new Font("Segoe UI", Font.BOLD, 22);

        // ── Server IP ─────────────────────────────────────────────────────
        JLabel lIp = lbl("Server IP:", bold);
        lIp.setBounds(40, 24, 160, 26);
        p.add(lIp);
        tfIp = field();
        tfIp.setText("localhost");
        tfIp.setBounds(40, 52, 390, 40);
        p.add(tfIp);

        // ── Divider ───────────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setBounds(30, 105, 420, 2);
        p.add(sep);

        // ── CREATE section ────────────────────────────────────────────────
        JLabel lCreate = lbl("Create Room", big);
        lCreate.setBounds(40, 115, 280, 32);
        p.add(lCreate);

        JLabel lSize = lbl("Number of Players (2-3):", bold);
        lSize.setBounds(40, 155, 200, 26);
        p.add(lSize);
        tfSize = field();
        tfSize.setText("2");
        tfSize.setBounds(250, 150, 80, 36);
        p.add(tfSize);

        lblRoomCode = lbl("Room Code : ", bold);
        lblRoomCode.setForeground(new Color(130, 0, 0));
        lblRoomCode.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblRoomCode.setBounds(40, 195, 390, 34);
        p.add(lblRoomCode);

        btnCreate = styledBtn("Create Room", new Color(120, 0, 0));
        btnCreate.setBounds(40, 237, 390, 48);
        p.add(btnCreate);

        // ── Divider 2 ─────────────────────────────────────────────────────
        JSeparator sep2 = new JSeparator();
        sep2.setBounds(30, 298, 420, 2);
        p.add(sep2);

        // ── JOIN section ──────────────────────────────────────────────────
        JLabel lJoin = lbl("Join Room", big);
        lJoin.setBounds(40, 310, 280, 32);
        p.add(lJoin);

        JLabel lCode = lbl("Room Code:", bold);
        lCode.setBounds(40, 350, 140, 26);
        p.add(lCode);
        tfCode = field();
        tfCode.setBounds(40, 378, 390, 40);
        p.add(tfCode);

        btnJoin = styledBtn("Join Room", new Color(90, 0, 0));
        btnJoin.setBounds(40, 430, 390, 48);
        p.add(btnJoin);

        // ── Status label ──────────────────────────────────────────────────
        lblStatus = lbl("", bold);
        lblStatus.setForeground(new Color(60, 60, 180));
        lblStatus.setBounds(40, 485, 390, 26);
        p.add(lblStatus);

        // ── Actions ───────────────────────────────────────────────────────
        btnCreate.addActionListener(e -> doCreate());
        btnJoin.addActionListener(e -> doJoin());

        return p;
    }

    // ── Create room ───────────────────────────────────────────────────────
    private void doCreate() {
        String ip = tfIp.getText().trim();
        if (ip.isEmpty()) {
            ip = "localhost";
        }
        int size = 3;
        try {
            size = Integer.parseInt(tfSize.getText().trim());
        } catch (Exception ignored) {
        }
        size = Math.max(2, Math.min(3, size));
        if (size < 2) {
            size = 2;
        }

        btnCreate.setEnabled(false);
        lblStatus.setText("Creating Room...");

        final String finalIp = ip;
        final int finalSize = size;

        // ถ้า host = localhost ให้รัน server บนเครื่องนี้
        if (ip.equals("localhost") || ip.equals("127.0.0.1")) {
            new Thread(() -> new GameServer().start()).start();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ignored) {
            }
        }

        // เชื่อม client + ส่ง CREATE_ROOM
        GameClient client = new GameClient();

        client.setOnRoomCode(code -> SwingUtilities.invokeLater(() -> {
            lblRoomCode.setText("Room Code: " + code);
            lblStatus.setText("Waiting for other players...");
        }));

        client.setOnRoomStatus(names -> SwingUtilities.invokeLater(()
                -> lblStatus.setText("Player in room: " + String.join(", ", names))));

        client.setOnGameStart(() -> {
            try {
                // เปลี่ยนจาก invokeLater เป็น invokeAndWait เพื่อหยุดรอให้หน้าจอสร้างเสร็จก่อน
                SwingUtilities.invokeAndWait(() -> {
                    GameUI gameUI = new GameUI(roomcreate.this, client, username);
                    client.setGameUI(gameUI); // ผูก UI เข้ากับ Client ทันที
                    gameUI.setVisible(true);
                    roomcreate.this.setVisible(false);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        client.setOnJoinFail(reason -> SwingUtilities.invokeLater(() -> {
            lblStatus.setText("Error: " + reason);
            btnCreate.setEnabled(true);
        }));

        new Thread(() -> client.connectAndCreate(finalIp, username, finalSize)).start();
    }

    // ── Join room ─────────────────────────────────────────────────────────
    private void doJoin() {
        String ip = tfIp.getText().trim();
        String code = tfCode.getText().trim().toUpperCase();

        if (ip.isEmpty()) {
            lblStatus.setText("Please enter Server IP");
            return;
        }
        if (code.isEmpty()) {
            lblStatus.setText("Please enter Room Code");
            return;
        }

        btnJoin.setEnabled(false);
        lblStatus.setText("Joining " + code + "...");

        GameClient client = new GameClient();

        client.setOnJoinOk(() -> SwingUtilities.invokeLater(()
                -> lblStatus.setText("Join successfully! Waiting for game to start...")));

        client.setOnRoomStatus(names -> SwingUtilities.invokeLater(()
                -> lblStatus.setText("Player in room: " + String.join(", ", names))));

        client.setOnGameStart(() -> {
            try {
                // เปลี่ยนเป็น invokeAndWait ตรงนี้ด้วยครับ
                SwingUtilities.invokeAndWait(() -> {
                    GameUI gameUI = new GameUI(roomcreate.this, client, username);
                    client.setGameUI(gameUI);
                    gameUI.setVisible(true);
                    roomcreate.this.setVisible(false);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        client.setOnJoinFail(reason -> SwingUtilities.invokeLater(() -> {
            lblStatus.setText("Failed to join room: " + reason);
            btnJoin.setEnabled(true);
        }));

        new Thread(() -> client.connectAndJoin(ip, username, code)).start();
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private JLabel lbl(String t, Font f) {
        JLabel l = new JLabel(t);
        l.setFont(f);
        l.setForeground(Color.DARK_GRAY);
        return l;
    }

    private JTextField field() {
        int arc = 30;
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setBorder(new RoundBorder(arc));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return tf;
    }

    private JButton styledBtn(String t, Color c) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : getBackground().darker());
                g2.fillRoundRect(0, 2, getWidth(), getHeight() - 2, 40, 40);
                g2.setColor(isEnabled() ? getBackground().brighter() : getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 4, 40, 40);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent()) / 2 - 5);
                g2.dispose();
            }
        };
        b.setBackground(c);
        b.setFont(new Font("Segoe UI", Font.BOLD, 17));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (b.isEnabled()) {
                    b.setBackground(c.brighter());
                }
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(c);
            }
        });
        return b;
    }

    private void styleClose(JButton b) {
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(255, 80, 80));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
    }

    class RoundBorder extends AbstractBorder {

        int r;

        RoundBorder(int r) {
            this.r = r;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(5, 15, 5, 15);
        }
    }

    class BackgroundPanel extends JPanel {

        int y = 0;
        Image bg = new ImageIcon("loginbg.png").getImage();
        Image card = new ImageIcon("cardsblur.png").getImage();
        int ch = 1000, ov = 140, eff = ch - ov;

        BackgroundPanel() {
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
