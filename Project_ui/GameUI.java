package Project_ui;

import Main.Bot;
import Main.Card;
import Main.GameClient;
import Main.Negative;
import Main.Zero;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * GameUI — หน้าเกมที่ทำงานได้จริงทั้ง Solo และ Online
 *
 * Solo mode → สร้างผ่าน new GameUI(owner, null) แล้วจัดการ logic เอง Online
 * mode → สร้างผ่าน new GameUI(owner, client) แล้วรับข้อมูลจาก GameClient
 */
public class GameUI extends JFrame {

    // ── Mode ──────────────────────────────────────────────────────────────
    private final boolean isOnline;
    private GameClient client; // null = solo

    // ── Solo game state ───────────────────────────────────────────────────
    private int cumulativeTotal = 0;
    private int currentRound = 1;
    private boolean isZeroActive = false;

    private int playerMoney = 0;
    private int playerScore = 0;
    private int[] playerTargets = new int[2];
    private List<String> playerInventory = new ArrayList<>();

    private Bot bot1, bot2;
    private int bot1MoneyUI = 0, bot2MoneyUI = 0;

    // ── Online game state (sync จาก server) ──────────────────────────────
    private int[] onlineHand = {};
    private String[] onlineEffects = {};
    private int onlineMoney = 0;
    private int onlineScore = 0;
    private int[] onlineTargets = {0, 0};
    private boolean isMyTurn = false;
    private String pendingBuy = "None";

    // ── Shared UI state ───────────────────────────────────────────────────
    private JButton selectedCardBtn = null;
    private int selectedCardIdx = -1;
    private String selectedUseEffect = "None";

    // ── UI Components ─────────────────────────────────────────────────────
    private JLabel totalLabel, targetLabel, roundLabel;
    private JLabel statsLabel, bot1Label, bot2Label;
    private JLabel onlineStatusLabel;
    private JButton buyNegBtn, buyZeroBtn, useNegBtn, useZeroBtn, playBtn;
    private JButton[] handCards;
    private JPanel handPanel;

    // ─────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────
    public GameUI(JFrame owner, GameClient client) {
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
            refreshSoloUI();
        } else {
            // online: รอ server ส่ง GAME_STATE มา
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

    // ─────────────────────────────────────────────────────────────────────
    // BUILD UI
    // ─────────────────────────────────────────────────────────────────────
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
        JLabel totalTitle = centerLabel("TABLE TOTAL", 24);
        bg.add(targetLabel);
        bg.add(roundLabel);
        bg.add(totalTitle);
        bg.add(totalLabel);

        // Shop buttons
        buyNegBtn = styledBtn("Buy (–)  5 coins", new Color(160, 50, 50));
        buyZeroBtn = styledBtn("Buy (0)  5 coins", new Color(160, 50, 50));
        buyNegBtn.addActionListener(e -> doBuy("Negative"));
        buyZeroBtn.addActionListener(e -> doBuy("Zero"));
        bg.add(buyNegBtn);
        bg.add(buyZeroBtn);

        // Effect use buttons
        useNegBtn = styledBtn("Use (–)", new Color(180, 120, 0));
        useZeroBtn = styledBtn("Use (0)", new Color(180, 120, 0));
        useNegBtn.addActionListener(e -> toggleEffect("Negative", useNegBtn, useZeroBtn));
        useZeroBtn.addActionListener(e -> toggleEffect("Zero", useZeroBtn, useNegBtn));
        bg.add(useNegBtn);
        bg.add(useZeroBtn);

        // Play button
        playBtn = styledBtn("▶  PLAY CARD", new Color(40, 150, 40));
        playBtn.addActionListener(e -> {
            if (isOnline) {
                doOnlinePlay();
            } else {
                doSoloPlay();
            }
        });
        bg.add(playBtn);

        // Hand panel (cards จะ rebuild ทุกรอบ)
        handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        handPanel.setOpaque(false);
        bg.add(handPanel);

        // Close button
        JButton close = new JButton("✕");
        close.setBackground(new Color(255, 80, 80));
        close.setForeground(Color.WHITE);
        close.setFocusPainted(false);
        close.setBorderPainted(false);
        close.addActionListener(e -> {
            if (owner != null) {
                owner.setVisible(true);
            }
            dispose();
        });
        bg.add(close);
        close.setBounds(0, 0, 40, 40); // จะถูก layout เองใน resize

        // เก็บ reference close เพื่อ layout
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

        // Table total (กลางจอ)
        int cx = (w - 200) / 2;
        getContentPane().getComponent(4).setBounds(cx, h / 2 - 150, 200, 40); // totalTitle
        totalLabel.setBounds(cx, h / 2 - 110, 200, 120);

        // Shop (ขวา)
        buyNegBtn.setBounds(w - 190, h / 2 - 50, 165, 48);
        buyZeroBtn.setBounds(w - 190, h / 2 + 10, 165, 48);

        // Hand cards (ล่าง)
        handPanel.setBounds((w - 620) / 2, h - 200, 620, 170);

        // Action buttons (เหนือ hand)
        useNegBtn.setBounds((w - 540) / 2, h - 265, 150, 48);
        useZeroBtn.setBounds((w - 540) / 2 + 170, h - 265, 150, 48);
        playBtn.setBounds((w - 540) / 2 + 345, h - 265, 200, 48);
    }

    // ─────────────────────────────────────────────────────────────────────
    // SOLO LOGIC
    // ─────────────────────────────────────────────────────────────────────
    private void initSoloLogic() {
        bot1 = new Bot();
        bot2 = new Bot();
        Random rand = new Random();
        playerTargets[0] = rand.nextInt(11);
        playerTargets[1] = rand.nextInt(11) + 10;
        bot1.setTarget(0, rand.nextInt(11));
        bot1.setTarget(1, rand.nextInt(11) + 10);
        bot2.setTarget(0, rand.nextInt(11));
        bot2.setTarget(1, rand.nextInt(11) + 10);
        buildSoloHand();
    }

    private void buildSoloHand() {
        handPanel.removeAll();
        handCards = new JButton[5];
        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            JButton btn = cardBtn(String.valueOf(val));
            btn.addActionListener(e -> selectCard(btn, val, -1));
            handCards[i] = btn;
            handPanel.add(btn);
        }
        handPanel.revalidate();
        handPanel.repaint();
    }

    private void refreshSoloUI() {
        statsLabel.setText("You   | Chips: " + playerMoney + "  Score: " + playerScore
                + "  Target: " + playerTargets[0] + " / " + playerTargets[1]);
        bot1Label.setText("Bot 1 | Chips: " + bot1MoneyUI + "  Score: " + bot1.getScore());
        bot2Label.setText("Bot 2 | Chips: " + bot2MoneyUI + "  Score: " + bot2.getScore());
        roundLabel.setText("Round: " + currentRound + " / 5");
        totalLabel.setText(String.valueOf(cumulativeTotal));
    }

    private void doBuy(String item) {
        if (isOnline) {
            pendingBuy = item;
            onlineStatusLabel.setText("Buy " + item);
            return;
        }
        // solo
        if (playerMoney >= 5) {
            playerMoney -= 5;
            playerInventory.add(item);
            refreshSoloUI();
            JOptionPane.showMessageDialog(this, "Buy" + item + "Success");
        } else {
            JOptionPane.showMessageDialog(this, "Not enough chips (requires 5)");
        }
    }

    private void toggleEffect(String effect, JButton clicked, JButton other) {
        List<String> inv = isOnline ? java.util.Arrays.asList(onlineEffects) : playerInventory;
        if (!inv.contains(effect) && !(isOnline && pendingBuy.equals(effect))) {
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

    private void doSoloPlay() {
        if (selectedCardBtn == null) {
            JOptionPane.showMessageDialog(this, "Please select a card first");
            return;
        }
        isZeroActive = false;
        StringBuilder summary = new StringBuilder("--- Round " + currentRound + " ---\n\n");

        // Human move
        int val = Integer.parseInt(selectedCardBtn.getText());
        Card humanCard = new Card(val);
        if (selectedUseEffect.equals("Zero")) {
            humanCard.setType(new Zero());
            playerInventory.remove("Zero");
        } else if (selectedUseEffect.equals("Negative")) {
            humanCard.setType(new Negative());
            playerInventory.remove("Negative");
        }
        applyCard(humanCard, "You", summary);

        selectedCardBtn.setEnabled(false);
        selectedCardBtn.setBackground(Color.DARK_GRAY);
        selectedCardBtn = null;
        selectedUseEffect = "None";
        useNegBtn.setBackground(new Color(180, 120, 0));
        useZeroBtn.setBackground(new Color(180, 120, 0));

        // Bot moves
        Card b1 = bot1.selectMove();
        if (b1.getType() != null) {
            bot1MoneyUI -= 5;
        }
        applyCard(b1, "Bot 1", summary);
        Card b2 = bot2.selectMove();
        if (b2.getType() != null) {
            bot2MoneyUI -= 5;
        }
        applyCard(b2, "Bot 2", summary);

        summary.append("\n▶ Table total :  ").append(cumulativeTotal);
        calcSoloScores();

        playerMoney += 5;
        bot1.addMoney(5);
        bot1MoneyUI += 5;
        bot2.addMoney(5);
        bot2MoneyUI += 5;
        currentRound++;

        JOptionPane.showMessageDialog(this, summary.toString(), "Round Result", JOptionPane.INFORMATION_MESSAGE);

        if (currentRound > 5) {
            showSoloGameOver();
        } else {
            refreshSoloUI();
            resetSoloHandButtons();
        }
    }

    private void applyCard(Card c, String name, StringBuilder sb) {
        sb.append("• ").append(name).append(" played ").append(c.getValue());
        if (c.getType() instanceof Zero) {
            cumulativeTotal = 0;
            isZeroActive = true;
            sb.append(" [ZERO → reset]\n");
        } else if (c.getType() instanceof Negative) {
            if (!isZeroActive) {
                cumulativeTotal -= c.getValue();
            }
            sb.append(" [–").append(c.getValue()).append("]\n");
        } else {
            if (!isZeroActive) {
                cumulativeTotal += c.getValue();
            }
            sb.append("\n");
        }
    }

    private void calcSoloScores() {
        int pD = Math.min(Math.abs(cumulativeTotal - playerTargets[0]), Math.abs(cumulativeTotal - playerTargets[1]));
        int b1D = Math.min(Math.abs(cumulativeTotal - bot1.getTarget()[0]), Math.abs(cumulativeTotal - bot1.getTarget()[1]));
        int b2D = Math.min(Math.abs(cumulativeTotal - bot2.getTarget()[0]), Math.abs(cumulativeTotal - bot2.getTarget()[1]));

        record R(String n, int d) implements Comparable<R> {

            public int compareTo(R o) {
                return Integer.compare(d, o.d);
            }
        }
        List<R> rs = new ArrayList<>(List.of(new R("P", pD), new R("B1", b1D), new R("B2", b2D)));
        Collections.sort(rs);
        for (int i = 0; i < 3; i++) {
            int pts = (i == 0) ? 2 : (i == 1) ? 1 : 0;
            switch (rs.get(i).n()) {
                case "P" ->
                    playerScore += pts;
                case "B1" ->
                    bot1.addScore(pts);
                case "B2" ->
                    bot2.addScore(pts);
            }
        }
    }

    private void resetSoloHandButtons() {
        for (JButton b : handCards) {
            if (b.isEnabled()) {
                b.setBackground(new Color(180, 30, 30));
            }
        }
    }

    private void showSoloGameOver() {
        int max = Math.max(playerScore, Math.max(bot1.getScore(), bot2.getScore()));
        String msg = "===== GAME OVER =====\n\n"
                + "You have   : " + playerScore + " points\n"
                + "Bot 1 have : " + bot1.getScore() + " points\n"
                + "Bot 2 have : " + bot2.getScore() + " points\n\n"
                + (playerScore == max ? "🏆 You Win!!" : "You lost. Better luck next time.");
        JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ONLINE CALLBACKS (เรียกจาก GameClient บน listener thread)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * server ส่ง GAME_STATE มา
     */
    public void onGameState(int[] targets, int money, int score,
            int[] hand, String[] effects) {
        SwingUtilities.invokeLater(() -> {
            onlineTargets = targets;
            onlineMoney = money;
            onlineScore = score;
            onlineHand = hand;
            onlineEffects = effects;
            pendingBuy = "None";
            selectedUseEffect = "None";
            selectedCardIdx = -1;

            statsLabel.setText("คุณ | Chips: " + money + "  Score: " + score
                    + "  Target: " + targets[0] + " / " + targets[1]);
            totalLabel.setText(String.valueOf(cumulativeTotal));
            buildOnlineHand();
            disableAllActions();
        });
    }

    /**
     * server ส่ง REQUEST_MOVE มา
     */
    public void onRequestMove(GameClient gc) {
        SwingUtilities.invokeLater(() -> {
            isMyTurn = true;
            onlineStatusLabel.setText("🎯 ถึงตาคุณ! เลือกไพ่แล้วกด PLAY CARD");
            enableActions();
        });
    }

    /**
     * server ส่ง ROUND_RESULT มา
     */
    public void onRoundResult(int total, int[] scores, String log) {
        SwingUtilities.invokeLater(() -> {
            cumulativeTotal = total;
            totalLabel.setText(String.valueOf(total));
            isMyTurn = false;
            disableAllActions();
            onlineStatusLabel.setText("รอบจบ — Total: " + total);

            StringBuilder sb = new StringBuilder(log + "\nScores:\n");
            for (int i = 0; i < scores.length; i++) {
                sb.append("P").append(i + 1).append(": ").append(scores[i]).append("  ");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "ผลรอบ", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * server ส่ง GAME_OVER มา
     */
    public void onGameOver(String[] names, int[] scores) {
        SwingUtilities.invokeLater(() -> {
            int max = java.util.Arrays.stream(scores).max().getAsInt();
            StringBuilder sb = new StringBuilder("===== GAME OVER =====\n\n");
            for (int i = 0; i < names.length; i++) {
                sb.append(names[i]).append(": ").append(scores[i]).append(" pts")
                        .append(scores[i] == max ? " 🏆" : "").append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "จบเกม", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        });
    }

    // ── Online play ───────────────────────────────────────────────────────
    private void doOnlinePlay() {
        if (selectedCardIdx < 0) {
            JOptionPane.showMessageDialog(this, "กรุณาเลือกไพ่ก่อน");
            return;
        }
        if (!isMyTurn) {
            return;
        }
        isMyTurn = false;
        disableAllActions();
        onlineStatusLabel.setText("ส่ง move แล้ว รอผู้เล่นอื่น...");
        client.sendMove(selectedCardIdx, pendingBuy, selectedUseEffect);
        pendingBuy = "None";
        selectedUseEffect = "None";
        selectedCardIdx = -1;
    }

    // ── Build online hand ─────────────────────────────────────────────────
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
        handPanel.revalidate();
        handPanel.repaint();
    }

    // ── Card selection ────────────────────────────────────────────────────
    private void selectCard(JButton btn, int val, int idx) {
        if (isOnline && !isMyTurn) {
            return;
        }
        if (selectedCardBtn != null && selectedCardBtn != btn && selectedCardBtn.isEnabled()) {
            selectedCardBtn.setBackground(new Color(180, 30, 30));
        }
        selectedCardBtn = btn;
        selectedCardIdx = idx;
        btn.setBackground(new Color(255, 160, 30));
    }

    // ── Enable / Disable ──────────────────────────────────────────────────
    private void enableActions() {
        buyNegBtn.setEnabled(true);
        buyZeroBtn.setEnabled(true);
        useNegBtn.setEnabled(true);
        useZeroBtn.setEnabled(true);
        playBtn.setEnabled(true);
        if (handCards != null) {
            for (JButton b : handCards) {
                b.setEnabled(true);
            }
        }
    }

    private void disableAllActions() {
        buyNegBtn.setEnabled(false);
        buyZeroBtn.setEnabled(false);
        useNegBtn.setEnabled(false);
        useZeroBtn.setEnabled(false);
        playBtn.setEnabled(false);
        if (handCards != null) {
            for (JButton b : handCards) {
                b.setEnabled(false);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // UI HELPERS
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
