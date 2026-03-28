package Project_ui;

import Main.*;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import Database.DB;

public class GameUI extends JFrame {

    private final boolean isOnline;
    private GameClient client;
    private LocalHuman localPlayer; // ตัวแทนเราในโหมด Solo
    private int currentTableTotal = 0;
    private String username;

    private int[] onlineHand = {};
    private String[] onlineEffects = {};
    private int[] onlineTargets = {0, 0};
    private boolean isMyTurn = false;
    private String pendingBuy = "None";
    private int selectedCardIdx = -1;
    private String selectedUseEffect = "None";

    private JButton selectedCardBtn = null;
    private JLabel totalTitleLabel, totalLabel, targetLabel, roundLabel;
    private JLabel statsLabel, bot1Label, bot2Label, onlineStatusLabel;
    private JButton buyNegBtn, buyZeroBtn, useNegBtn, useZeroBtn, playBtn;
    private JButton[] handCards;
    private JPanel handPanel;

    public GameUI(JFrame owner, GameClient client, String username) {
        this.username = username;
        this.client = client;
        this.isOnline = (client != null);

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        setContentPane(bg);
        bg.setLayout(null);

        buildUI(bg, owner);

        if (!isOnline) {
            initSoloLogic();
        } else {
            onlineStatusLabel.setText("Waiting for game start...");
            disableAllActions();
        }

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutComponents(bg.getWidth(), bg.getHeight());
            }
        });
    }

    private void buildUI(JPanel bg, JFrame owner) {
        statsLabel = label("", 18, Color.WHITE);
        bot1Label = label("", 16, Color.WHITE);
        bot2Label = label("", 16, Color.WHITE);
        bg.add(statsLabel);
        bg.add(bot1Label);
        bg.add(bot2Label);

        onlineStatusLabel = label("", 16, new Color(255, 220, 80));
        bg.add(onlineStatusLabel);

        targetLabel = centerLabel("", 24);
        roundLabel = centerLabel("Round: 1/5", 20);
        totalLabel = centerLabel("0", 100);
        totalTitleLabel = centerLabel("TABLE TOTAL", 24);
        bg.add(targetLabel);
        bg.add(roundLabel);
        bg.add(totalTitleLabel);
        bg.add(totalLabel);

        buyNegBtn = styledBtn("Buy (–)  5 coins", new Color(160, 50, 50));
        buyZeroBtn = styledBtn("Buy (0)  5 coins", new Color(160, 50, 50));
        buyNegBtn.addActionListener(e -> doBuy("Negative"));
        buyZeroBtn.addActionListener(e -> doBuy("Zero"));
        bg.add(buyNegBtn);
        bg.add(buyZeroBtn);

        useNegBtn = styledBtn("Use (–)", new Color(180, 120, 0));
        useZeroBtn = styledBtn("Use (0)", new Color(180, 120, 0));
        useNegBtn.addActionListener(e -> toggleEffect("Negative", useNegBtn, useZeroBtn));
        useZeroBtn.addActionListener(e -> toggleEffect("Zero", useZeroBtn, useNegBtn));
        bg.add(useNegBtn);
        bg.add(useZeroBtn);

        playBtn = styledBtn("▶  PLAY CARD", new Color(40, 150, 40));
        playBtn.addActionListener(e -> processPlayCard());
        bg.add(playBtn);

        handPanel = new JPanel(null); // ใช้ Null Layout จัดพิกัดเอง 100%
        handPanel.setOpaque(false);
        bg.add(handPanel);

        JButton close = new JButton("✕");
        close.setBackground(new Color(255, 80, 80));
        close.setForeground(Color.WHITE);
        close.setFocusPainted(false);
        close.setBorderPainted(false);
        close.addActionListener(e -> {
            if (owner != null) owner.setVisible(true);
            dispose();
        });
        bg.add(close);
        close.setBounds(0, 0, 40, 40);

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                close.setBounds(bg.getWidth() - 50, 10, 40, 40);
            }
        });
    }

    private void layoutComponents(int w, int h) {
        statsLabel.setBounds(30, 20, 320, 28);
        bot1Label.setBounds(30, 52, 320, 24);
        bot2Label.setBounds(30, 76, 320, 24);
        onlineStatusLabel.setBounds(30, 104, 500, 24);

        targetLabel.setBounds((w - 320) / 2, 20, 320, 40);
        roundLabel.setBounds(w - 220, 20, 180, 36);

        int cx = (w - 200) / 2;
        totalTitleLabel.setBounds(cx, h / 2 - 150, 200, 40);
        totalLabel.setBounds(cx, h / 2 - 110, 200, 120);

        buyNegBtn.setBounds(w - 190, h / 2 - 50, 165, 48);
        buyZeroBtn.setBounds(w - 190, h / 2 + 10, 165, 48);

        handPanel.setBounds(0, h - 200, w, 170);
        if (handCards != null) {
            int cardW = 95, cardH = 145, gap = 15;
            int totalW = (handCards.length * cardW) + ((handCards.length - 1) * gap);
            int startX = (w - totalW) / 2;
            for (int i = 0; i < handCards.length; i++) {
                if (handCards[i] != null) {
                    handCards[i].setBounds(startX + (i * (cardW + gap)), 0, cardW, cardH);
                }
            }
        }

        useNegBtn.setBounds((w - 540) / 2, h - 265, 150, 48);
        useZeroBtn.setBounds((w - 540) / 2 + 170, h - 265, 150, 48);
        playBtn.setBounds((w - 540) / 2 + 345, h - 265, 200, 48);
    }

    // ─────────────────────────────────────────────────────────────────────
    // CORE SYSTEM (รวม Solo และ Online ไว้ด้วยกัน)
    // ─────────────────────────────────────────────────────────────────────
    
    private void initSoloLogic() {
        localPlayer = new LocalHuman(this);

        // สร้าง Server จำลองมารับผลลัพธ์จาก GameEngine แล้วยิงเข้าหน้าจอ
        GameServer dummyServer = new GameServer() {
            @Override
            public void broadcastState(GameMessage msg) {
                if (msg.getType() == GameMessage.Type.ROUND_RESULT) {
                    onRoundResult(msg.getCumulativeTotal(), msg.getAllScores(), msg.getRoundLog());
                } else if (msg.getType() == GameMessage.Type.GAME_OVER) {
                    onGameOver(msg.getPlayerNames(), msg.getFinalScores());
                }
            }
        };

        // สตาร์ท GameEngine 100% เต็มสูบ!
        new Thread(new GameEngine(localPlayer, dummyServer)).start();
    }

    private void doBuy(String item) {
        pendingBuy = item;
        onlineStatusLabel.setText("Queued Buy: " + item);
    }

    private void toggleEffect(String effect, JButton clicked, JButton other) {
        List<String> inv = Arrays.asList(onlineEffects);
        if (!inv.contains(effect) && !pendingBuy.equals(effect)) {
            JOptionPane.showMessageDialog(this, "ไม่มี " + effect + " ใน inventory");
            return;
        }
        if (selectedUseEffect.equals(effect)) {
            selectedUseEffect = "None";
            clicked.setBackground(new Color(180, 120, 0));
        } else {
            selectedUseEffect = effect;
            clicked.setBackground(new Color(50, 180, 50));
            other.setBackground(new Color(180, 120, 0));
        }
    }

    private void processPlayCard() {
        if (selectedCardIdx < 0) {
            JOptionPane.showMessageDialog(this, "กรุณาเลือกไพ่ก่อน");
            return;
        }
        if (!isMyTurn) return;

        isMyTurn = false;
        disableAllActions();
        onlineStatusLabel.setText("ส่ง move แล้ว รอการประมวลผล...");

        if (isOnline) {
            client.sendMove(selectedCardIdx, pendingBuy, selectedUseEffect);
        } else {
            // โหมด Solo โยนให้ Local Player จัดการ (Engine กำลังรอรับอยู่)
            localPlayer.submitMove(selectedCardIdx, pendingBuy, selectedUseEffect);
        }

        pendingBuy = "None";
        selectedUseEffect = "None";
        selectedCardIdx = -1;
    }

    // ─────────────────────────────────────────────────────────────────────
    // CALLBACKS (สำหรับให้ Engine และ Client เรียกมาอัปเดตจอ)
    // ─────────────────────────────────────────────────────────────────────

    public void onGameState(int[] targets, int money, int score, int[] hand, String[] effects) {
        SwingUtilities.invokeLater(() -> {
            onlineTargets = targets;
            onlineHand = hand;
            onlineEffects = effects;
            pendingBuy = "None";
            selectedUseEffect = "None";
            selectedCardIdx = -1;

            statsLabel.setText(username + " | Chips: " + money + "  Score: " + score + "  Target: " + targets[0] + " / " + targets[1]);
            totalLabel.setText(String.valueOf(currentTableTotal));
            buildOnlineHand();
            disableAllActions();
        });
    }

    public void onRequestMove(GameClient gc) {
        SwingUtilities.invokeLater(() -> {
            isMyTurn = true;
            onlineStatusLabel.setText("It's your turn! play your card.");
            enableActions();
        });
    }

    public void onRoundResult(int total, int[] scores, String log) {
        SwingUtilities.invokeLater(() -> {
            currentTableTotal = total;
            totalLabel.setText(String.valueOf(total));
            isMyTurn = false;
            disableAllActions();
            onlineStatusLabel.setText("รอบจบ — Total: " + total);

            if (scores.length >= 3) {
                bot1Label.setText("P2 | Score: " + scores[1]);
                bot2Label.setText("P3 | Score: " + scores[2]);
            }

            StringBuilder sb = new StringBuilder(log + "\nScores:\n");
            for (int i = 0; i < scores.length; i++) {
                sb.append("P").append(i + 1).append(": ").append(scores[i]).append("  ");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "ผลรอบ", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void onGameOver(String[] names, int[] scores) {
        SwingUtilities.invokeLater(() -> {
            int max = Arrays.stream(scores).max().getAsInt();
            int index = isOnline ? client.getPlayerIndex() : 0;
            if(scores[index] == max){
                DB.addWin(this.username);
            }
            StringBuilder sb = new StringBuilder("===== GAME OVER =====\n\n");
            for (int i = 0; i < names.length; i++) {
                sb.append(names[i]).append(": ").append(scores[i]).append(" pts")
                        .append(scores[i] == max ? " 🏆" : "").append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "จบเกม", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        });
    }

    private void buildOnlineHand() {
        handPanel.removeAll();
        handCards = new JButton[onlineHand.length];
        for (int i = 0; i < onlineHand.length; i++) {
            final int idx = i;
            final int val = onlineHand[i];
            JButton btn = cardBtn(String.valueOf(val));
            btn.addActionListener(e -> selectCard(btn, val, idx));
            handCards[i] = btn;
            handPanel.add(btn);
        }
        if (getWidth() > 0) layoutComponents(getWidth(), getHeight());
        handPanel.revalidate();
        handPanel.repaint();
    }

    private void selectCard(JButton btn, int val, int idx) {
        if (!isMyTurn) return;
        if (selectedCardBtn != null && selectedCardBtn != btn && selectedCardBtn.isEnabled()) {
            selectedCardBtn.setBackground(new Color(180, 30, 30));
        }
        selectedCardBtn = btn;
        selectedCardIdx = idx;
        btn.setBackground(new Color(255, 160, 30));
    }

    private void enableActions() {
        buyNegBtn.setEnabled(true);
        buyZeroBtn.setEnabled(true);
        useNegBtn.setEnabled(true);
        useZeroBtn.setEnabled(true);
        playBtn.setEnabled(true);
        if (handCards != null) {
            for (JButton b : handCards) b.setEnabled(true);
        }
    }

    private void disableAllActions() {
        buyNegBtn.setEnabled(false);
        buyZeroBtn.setEnabled(false);
        useNegBtn.setEnabled(false);
        useZeroBtn.setEnabled(false);
        playBtn.setEnabled(false);
        if (handCards != null) {
            for (JButton b : handCards) b.setEnabled(false);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // UI HELPERS (ละไว้เหมือนเดิม)
    // ─────────────────────────────────────────────────────────────────────
    private JLabel label(String t, int s, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, s));
        l.setForeground(c);
        return l;
    }

    private JLabel centerLabel(String t, int s) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, s));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JButton styledBtn(String t, Color c) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : getBackground().darker());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent()) / 2 - 4);
                g2.dispose();
            }
        };
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (b.isEnabled()) b.setBackground(c.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(c);
            }
        });
        return b;
    }

    private JButton cardBtn(String val) {
        JButton b = new JButton(val) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : Color.DARK_GRAY);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 38));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, (getHeight() + fm.getAscent()) / 2 - 8);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(95, 145));
        b.setBackground(new Color(180, 30, 30));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 38));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    class BackgroundPanel extends JPanel {
        Image bg = new ImageIcon("loginbg.png").getImage();
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}