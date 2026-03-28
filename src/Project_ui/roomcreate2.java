package Project_ui;

import Main.GameServer2;
import Main.GameClient;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class roomcreate2 extends JFrame {

    private static final int PORT      = 9999;
    private static final int MAX_PLAYERS = 3;

    // ── ใช้แชร์รหัสห้องระหว่าง Create / Join (ในเครื่องเดียวกัน) ──
    private static String currentRoomCode = null;

    public roomcreate2(JFrame owner) {
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

        // ── Panel หลัก ──
        JPanel mainPanel = createMainPanel(owner);
        int pWidth = 420, pHeight = 480;
        bg.add(mainPanel);

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int w = bg.getWidth(), h = bg.getHeight();
                int panelX = (w - pWidth) / 2;
                int panelY = (h - pHeight) / 2 + 60;
                mainPanel.setBounds(panelX, panelY, pWidth, pHeight);

                int logoW = 200;
                int logoH = (originalIcon.getIconWidth() > 0)
                        ? (logoW * originalIcon.getIconHeight()) / originalIcon.getIconWidth() : 100;
                Image scaledLogo = originalIcon.getImage().getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaledLogo));
                logoLabel.setBounds((w - logoW) / 2, panelY - logoH - 10, logoW, logoH);
                closeBtn.setBounds(w - 50, 10, 40, 40);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  CREATE / JOIN PANEL
    // ══════════════════════════════════════════════════════════════
    private JPanel createMainPanel(JFrame owner) {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(null);

        int fieldArc = 35;
        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);

        // Label
        JLabel roomLabel = new JLabel("Room ID");
        roomLabel.setBounds(60, 40, 300, 25);
        roomLabel.setFont(labelFont);
        roomLabel.setForeground(Color.WHITE);
        panel.add(roomLabel);

        // Field กรอก Room ID
        JTextField roomField = createField(fieldArc);
        roomField.setBounds(60, 70, 300, 42);
        roomField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(roomField);

        // Status label
        JLabel statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setBounds(60, 122, 300, 22);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(255, 200, 100));
        panel.add(statusLabel);

        // ── ปุ่ม Create Room ──
        JButton btnCreate = createStyledButton("Create Room", new Color(120, 0, 0));
        btnCreate.setBounds(60, 155, 300, 52);
        panel.add(btnCreate);

        // ── ปุ่ม Join Room ──
        JButton btnJoin = createStyledButton("Join Room", new Color(80, 0, 0));
        btnJoin.setBounds(60, 220, 300, 52);
        panel.add(btnJoin);

        // ══════════════════════════════════
        //  ACTION: Create Room
        // ══════════════════════════════════
        btnCreate.addActionListener(e -> {
            // สร้าง Room ID สุ่ม 4 หลัก
            String code = String.valueOf(1000 + new Random().nextInt(9000));
            currentRoomCode = code;

            roomField.setText(code);
            roomField.setEditable(false);
            statusLabel.setText("กำลังเปิดห้อง...");
            btnCreate.setEnabled(false);
            btnJoin.setEnabled(false);

            // เปิด Server บน thread แยก แล้วเปิด Lobby
            new Thread(() -> {
                GameServer2 server = new GameServer2(PORT, MAX_PLAYERS, code);

                // callback เมื่อมีคนเข้าห้อง → Lobby จะอัปเดต
                List<String> playerNames = new ArrayList<>();
                playerNames.add("You (Host)");

                SwingUtilities.invokeLater(() -> {
                    openLobby(owner, code, playerNames, server, true);
                });

                server.setOnPlayerJoined(num -> {
                    playerNames.add("Player " + (num + 1));
                    // ส่ง event ไปที่ Lobby ผ่าน custom event (ใช้ property change)
                    SwingUtilities.invokeLater(() ->
                        firePropertyChange("playerJoined", null, new ArrayList<>(playerNames))
                    );
                });

                server.startServer();   // block จนเกมจบ

            }).start();
        });

        // ══════════════════════════════════
        //  ACTION: Join Room
        // ══════════════════════════════════
        btnJoin.addActionListener(e -> {
            String code = roomField.getText().trim();

            if (code.isEmpty()) {
                statusLabel.setText("⚠ กรุณากรอก Room ID ก่อน");
                return;
            }

            // ตรวจสอบรหัสห้อง
            if (!GameServer2.roomExists(code)) {
                statusLabel.setForeground(new Color(255, 80, 80));
                statusLabel.setText("✗ ไม่พบห้อง \"" + code + "\" กรุณาลองใหม่");
                // เขย่า field เพื่อ feedback
                shakeComponent(roomField);
                return;
            }

            statusLabel.setForeground(new Color(100, 220, 100));
            statusLabel.setText("✓ พบห้อง! กำลังเข้า...");
            btnJoin.setEnabled(false);
            btnCreate.setEnabled(false);

            // เชื่อมต่อ Server
            new Thread(() -> {
                try {
                    List<String> names = new ArrayList<>();
                    names.add("You");
                    SwingUtilities.invokeLater(() -> openLobby(owner, code, names, null, false));
                    new GameClient().connect("localhost", PORT);
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setForeground(new Color(255, 80, 80));
                        statusLabel.setText("เชื่อมต่อไม่ได้: " + ex.getMessage());
                        btnJoin.setEnabled(true);
                        btnCreate.setEnabled(true);
                    });
                }
            }).start();
        });

        return panel;
    }

    // ══════════════════════════════════════════════════════════════
    //  LOBBY SCREEN  (เปิดเป็น JFrame ใหม่)
    // ══════════════════════════════════════════════════════════════
    private void openLobby(JFrame owner, String code,
                           List<String> initialPlayers,
                           GameServer2 server, boolean isHost) {

        JFrame lobby = new JFrame();
        lobby.setUndecorated(true);
        lobby.setExtendedState(JFrame.MAXIMIZED_BOTH);
        lobby.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ── Background ──
        BackgroundPanel bg = new BackgroundPanel();
        lobby.setContentPane(bg);
        bg.setLayout(null);

        // ── Close btn ──
        JButton closeBtn = new JButton("✕");
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(255, 80, 80));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(ev -> System.exit(0));
        bg.add(closeBtn);

        // ── Card Panel ตรงกลาง ──
        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // พื้นหลัง panel กึ่งโปร่งใส
                g2.setColor(new Color(15, 10, 10, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                // ขอบสีแดงเข้ม
                g2.setColor(new Color(140, 0, 0, 180));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 40, 40);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        bg.add(card);

        // ── "WAITING FOR PLAYERS" title ──
        JLabel titleLabel = new JLabel("WAITING FOR PLAYERS", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        card.add(titleLabel);

        // ── Animated dots ──
        JLabel dotsLabel = new JLabel("...", JLabel.CENTER);
        dotsLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        dotsLabel.setForeground(new Color(180, 0, 0));
        card.add(dotsLabel);

        Timer dotsTimer = new Timer(500, null);
        String[] dotFrames = {".", "..", "..."};
        int[] frame = {0};
        dotsTimer.addActionListener(ev -> {
            dotsLabel.setText(dotFrames[frame[0] % 3]);
            frame[0]++;
        });
        dotsTimer.start();

        // ── Room Code display ──
        JLabel codeTitle = new JLabel("ROOM CODE", JLabel.CENTER);
        codeTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        codeTitle.setForeground(new Color(180, 180, 180));
        card.add(codeTitle);

        JLabel codeLabel = new JLabel(code, JLabel.CENTER);
        codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 56));
        codeLabel.setForeground(new Color(255, 80, 80));
        card.add(codeLabel);

        // ── Separator ──
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80, 0, 0));
        card.add(sep);

        // ── Player list ──
        JLabel playersTitle = new JLabel("PLAYERS", JLabel.LEFT);
        playersTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        playersTitle.setForeground(new Color(160, 160, 160));
        card.add(playersTitle);

        // Player slots (max 3)
        JLabel[] slotLabels = new JLabel[MAX_PLAYERS];
        for (int i = 0; i < MAX_PLAYERS; i++) {
            slotLabels[i] = makeSlotLabel(i < initialPlayers.size()
                    ? initialPlayers.get(i) : null);
            card.add(slotLabels[i]);
        }

        // ── Layout ──
        int cW = 440, cH = 520;
        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int w = bg.getWidth(), h = bg.getHeight();
                closeBtn.setBounds(w - 50, 10, 40, 40);

                int cx = (w - cW) / 2, cy = (h - cH) / 2;
                card.setBounds(cx, cy, cW, cH);

                titleLabel.setBounds(20, 30, cW - 40, 36);
                dotsLabel.setBounds(20, 66, cW - 40, 28);
                codeTitle.setBounds(20, 110, cW - 40, 20);
                codeLabel.setBounds(20, 128, cW - 40, 72);
                sep.setBounds(40, 215, cW - 80, 2);
                playersTitle.setBounds(40, 228, cW - 80, 20);

                for (int i = 0; i < MAX_PLAYERS; i++) {
                    slotLabels[i].setBounds(40, 258 + i * 72, cW - 80, 58);
                }
            }
        });

        // ── รับ callback เมื่อมีผู้เล่นใหม่ ──
        if (isHost && server != null) {
            server.setOnPlayerJoined(num -> {
                SwingUtilities.invokeLater(() -> {
                    if (num < MAX_PLAYERS) {
                        slotLabels[num].setText("  ● Player " + (num + 1));
                        slotLabels[num].setForeground(new Color(100, 220, 100));
                        slotLabels[num].setBackground(new Color(0, 60, 0, 120));
                    }
                    // ถ้าครบ → ปิด Lobby รอเกมเริ่ม (GameEngine จะจัดการต่อ)
                    if (num + 1 >= MAX_PLAYERS) {
                        dotsTimer.stop();
                        titleLabel.setText("GAME STARTING!");
                        titleLabel.setForeground(new Color(255, 200, 50));
                    }
                });
            });
        }

        lobby.setVisible(true);
        roomcreate2.this.setVisible(false);
    }

    // ── สร้าง slot label สำหรับแต่ละช่องผู้เล่น ──
    private JLabel makeSlotLabel(String name) {
        JLabel lbl = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground() != null && getBackground().getAlpha() > 0) {
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                } else {
                    g2.setColor(new Color(40, 40, 40, 140));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                }
                g2.setColor(new Color(60, 60, 60, 100));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lbl.setOpaque(false);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));

        if (name != null) {
            lbl.setText("  ● " + name);
            lbl.setForeground(new Color(100, 220, 100));
            lbl.setBackground(new Color(0, 60, 0, 120));
        } else {
            lbl.setText("  ○ Waiting...");
            lbl.setForeground(new Color(120, 120, 120));
            lbl.setBackground(new Color(0, 0, 0, 0));
        }
        return lbl;
    }

    // ══════════════════════════════════════════════════════════════
    //  SHAKE ANIMATION  สำหรับ field ตอนรหัสผิด
    // ══════════════════════════════════════════════════════════════
    private void shakeComponent(JComponent c) {
        Point origin = c.getLocation();
        Timer t = new Timer(30, null);
        int[] count = {0};
        int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        t.addActionListener(e -> {
            if (count[0] < offsets.length) {
                c.setLocation(origin.x + offsets[count[0]], origin.y);
                count[0]++;
            } else {
                c.setLocation(origin);
                t.stop();
            }
        });
        t.start();
    }

    // ══════════════════════════════════════════════════════════════
    //  BACKGROUND PANEL
    // ══════════════════════════════════════════════════════════════
    class BackgroundPanel extends JPanel {
        int y = 0;
        Image bgImg, cardImg;
        int cardHeight = 1000, overlap = 140;
        int effectiveHeight = cardHeight - overlap;

        public BackgroundPanel() {
            bgImg  = new ImageIcon("loginbg.png").getImage();
            cardImg = new ImageIcon("cardsblur.png").getImage();
            new Timer(16, e -> { y = (y + 1) % effectiveHeight; repaint(); }).start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            g2.drawImage(bgImg, 0, 0, w, h, this);
            int cw = 350, offset = 500;
            int yr = (y + offset) % effectiveHeight;
            g2.drawImage(cardImg, 50,         y,  cw, cardHeight, this);
            g2.drawImage(cardImg, 50,         y - effectiveHeight, cw, cardHeight, this);
            g2.drawImage(cardImg, w-50-cw, yr,    cw, cardHeight, this);
            g2.drawImage(cardImg, w-50-cw, yr - effectiveHeight,  cw, cardHeight, this);
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════
    private JTextField createField(int arc) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false);
        f.setBorder(new RoundedBorder(arc));
        return f;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 40;
                g2.setColor(color.darker());
                g2.fillRoundRect(0, 2, getWidth(), getHeight()-2, arc, arc);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight()-4, arc, arc);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()-fm.stringWidth(getText()))/2;
                int y = (getHeight()+fm.getAscent())/2 - 5;
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
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(color); }
        });
        return btn;
    }

    class RoundedBorder extends AbstractBorder {
        private final int r;
        RoundedBorder(int r) { this.r = r; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(5, 20, 5, 20); }
    }
}
