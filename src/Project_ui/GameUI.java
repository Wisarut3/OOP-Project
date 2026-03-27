package Project_ui;

import Main.Bot;
import Main.Card;
import Main.Negative;
import Main.Zero;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameUI extends JFrame {

    // --- Game State Variables ---
    private int cumulativeTotal = 0; // Total points on the table
    private int currentRound = 1; // Current round number (1-5)
    private boolean isZeroActive = false; // Flag to lock the total to 0 when Zero card is used

    // Human Player Data
    private int playerMoney = 0; 
    private int playerScore = 0;
    private int[] playerTargets = new int[2]; // Two target numbers for the player [cite: 10]
    private List<String> playerInventory = new ArrayList<>(); // Store purchased items
    
    // Bot Players (using objects from Main package) [cite: 10]
    private Bot bot1;
    private Bot bot2;
    
    // Tracking bot money for UI display
    private int bot1MoneyUI = 0;
    private int bot2MoneyUI = 0;

    // Interaction Selection State
    private JButton selectedCardBtn = null; // Current selected card button
    private String selectedUseEffect = "None"; // Current selected effect to use
    
    // UI Components
    private JLabel totalLabel, targetLabel, roundLabel, totalTitle;
    private JLabel statsLabel, bot1Label, bot2Label; // Labels for player and bot stats
    private JButton buyNegBtn, buyZeroBtn, useNegBtn, useZeroBtn, playBtn;
    private JButton[] handCards = new JButton[5];

    public GameUI(JFrame owner) {
        // Setup Fullscreen Window 
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set Background Panel
        BackgroundPanel bg = new BackgroundPanel();
        setContentPane(bg);
        bg.setLayout(null);

        // Initialize bots and targets
        initializeGameLogic();

        // --- Header Stats (Top Left) ---
        statsLabel = createLeftTextLabel("", 18);
        bot1Label = createLeftTextLabel("", 16);
        bot2Label = createLeftTextLabel("", 16);
        
        bg.add(statsLabel);
        bg.add(bot1Label);
        bg.add(bot2Label);

        // --- Target & Round Info ---
        targetLabel = createTextLabel("", 24);
        roundLabel = createTextLabel("", 20);
        bg.add(targetLabel);
        bg.add(roundLabel);

        // --- Table Display ---
        totalTitle = createTextLabel("TABLE TOTAL", 24);
        totalLabel = createTextLabel("0", 100);
        bg.add(totalTitle);
        bg.add(totalLabel);

        // --- Shop Buttons (Right) ---
        buyNegBtn = createStyledButton("Buy (-)", new Color(160, 50, 50));
        buyZeroBtn = createStyledButton("Buy (0)", new Color(160, 50, 50));
        buyNegBtn.addActionListener(e -> buyItem("Negative"));
        buyZeroBtn.addActionListener(e -> buyItem("Zero"));
        bg.add(buyNegBtn);
        bg.add(buyZeroBtn);

        // --- Player Hand (Bottom) ---
        for (int i = 0; i < 5; i++) {
            handCards[i] = createCardButton(String.valueOf(i + 1));
            bg.add(handCards[i]);
        }

        // --- Action Buttons (Equip & Play) ---
        useNegBtn = createStyledButton("Use (-)", new Color(180, 120, 0));
        useZeroBtn = createStyledButton("Use (0)", new Color(180, 120, 0));
        playBtn = createStyledButton("PLAY CARD", new Color(40, 150, 40)); 
        
        useNegBtn.addActionListener(e -> toggleEffect("Negative", useNegBtn, useZeroBtn));
        useZeroBtn.addActionListener(e -> toggleEffect("Zero", useZeroBtn, useNegBtn));
        playBtn.addActionListener(e -> processTurn());

        bg.add(useNegBtn);
        bg.add(useZeroBtn);
        bg.add(playBtn);

        // Close Application Button
        JButton closeBtn = new JButton("✕");
        closeBtn.setBackground(new Color(255, 80, 80));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> {
            owner.setVisible(true);
            this.dispose();
        });
        bg.add(closeBtn);

        // Handle Responsive Layout 
        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int w = bg.getWidth(), h = bg.getHeight();
                closeBtn.setBounds(w - 50, 10, 40, 40);
                
                statsLabel.setBounds(30, 20, 300, 30);
                bot1Label.setBounds(30, 50, 300, 30);
                bot2Label.setBounds(30, 80, 300, 30);
                
                targetLabel.setBounds((w - 300) / 2, 20, 300, 40);
                roundLabel.setBounds(w - 200, 20, 150, 40);
                
                totalTitle.setBounds((w - 200) / 2, h / 2 - 140, 200, 40);
                totalLabel.setBounds((w - 200) / 2, h / 2 - 100, 200, 120);
                
                buyNegBtn.setBounds(w - 180, h / 2 - 40, 150, 50);
                buyZeroBtn.setBounds(w - 180, h / 2 + 30, 150, 50);
                
                for (int i = 0; i < 5; i++) handCards[i].setBounds((w - 580) / 2 + (i * 120), h - 180, 100, 150);
                
                useNegBtn.setBounds((w - 520) / 2, h - 250, 150, 50);
                useZeroBtn.setBounds((w - 520) / 2 + 170, h - 250, 150, 50);
                playBtn.setBounds((w - 520) / 2 + 340, h - 250, 180, 50);
            }
        });
        refreshUI();
    }

    // Initialize targets and bot instances [cite: 10]
    private void initializeGameLogic() {
        bot1 = new Bot();
        bot2 = new Bot();

        Random rand = new Random();
        playerTargets[0] = rand.nextInt(11); // 0-10
        playerTargets[1] = rand.nextInt(11) + 10; // 10-20
        
        bot1.setTarget(0, rand.nextInt(11)); 
        bot1.setTarget(1, rand.nextInt(11) + 10);
        
        bot2.setTarget(0, rand.nextInt(11)); 
        bot2.setTarget(1, rand.nextInt(11) + 10);
    }

    // Update screen text with current data
    private void refreshUI() {
        statsLabel.setText("Player 1 | Chips: " + playerMoney + " | Score: " + playerScore);
        bot1Label.setText("Bot 1      | Chips: " + bot1MoneyUI + " | Score: " + bot1.getScore());
        bot2Label.setText("Bot 2      | Chips: " + bot2MoneyUI + " | Score: " + bot2.getScore());
        
        targetLabel.setText("Targets: " + playerTargets[0] + " & " + playerTargets[1]);
        roundLabel.setText("Round: " + currentRound + " / 5");
        totalLabel.setText(String.valueOf(cumulativeTotal));
    }

    // Purchase an item from the shop
    private void buyItem(String itemName) {
        if (playerMoney >= 5) {
            playerMoney -= 5;
            playerInventory.add(itemName);
            refreshUI();
            JOptionPane.showMessageDialog(this, "Item " + itemName + " purchased successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Not enough chips! (Costs 5)");
        }
    }

    // Select or deselect an item to use with a card
    private void toggleEffect(String effect, JButton clicked, JButton other) {
        if (!playerInventory.contains(effect)) {
            JOptionPane.showMessageDialog(this, "Item not in inventory!");
            return;
        }
        if (selectedUseEffect.equals(effect)) {
            selectedUseEffect = "None";
            clicked.setBackground(new Color(180, 120, 0));
        } else {
            selectedUseEffect = effect;
            clicked.setBackground(Color.GREEN);
            other.setBackground(new Color(180, 120, 0));
        }
    }

    // Process the player and bot moves for the current round [cite: 10]
    private void processTurn() {
        if (selectedCardBtn == null) {
            JOptionPane.showMessageDialog(this, "Please select a card first.");
            return;
        }

        isZeroActive = false; // Reset Zero effect for the new round
        StringBuilder summary = new StringBuilder("--- Round " + currentRound + " Summary ---\n\n");

        // 1. Process Human Player move
        int val = Integer.parseInt(selectedCardBtn.getText());
        Card humanCard = new Card(val);
        
        if (selectedUseEffect.equals("Zero")) { 
            humanCard.setType(new Zero()); 
            playerInventory.remove("Zero"); 
        } else if (selectedUseEffect.equals("Negative")) { 
            humanCard.setType(new Negative()); 
            playerInventory.remove("Negative"); 
        }
        applyCardToTotal(humanCard, "You", summary);

        // Update UI state for cards and buttons
        selectedCardBtn.setEnabled(false);
        selectedCardBtn.setBackground(Color.DARK_GRAY);
        selectedCardBtn = null;
        selectedUseEffect = "None";
        useNegBtn.setBackground(new Color(180, 120, 0));
        useZeroBtn.setBackground(new Color(180, 120, 0));

        // 2. Process Bot moves [cite: 10]
        Card b1Card = bot1.selectMove();
        if (b1Card.getType() != null) bot1MoneyUI -= 5;
        applyCardToTotal(b1Card, "Bot 1", summary);

        Card b2Card = bot2.selectMove();
        if (b2Card.getType() != null) bot2MoneyUI -= 5;
        applyCardToTotal(b2Card, "Bot 2", summary);

        summary.append("\n>>> Current Table Total: ").append(cumulativeTotal);
        JOptionPane.showMessageDialog(this, summary.toString(), "Round Summary", JOptionPane.INFORMATION_MESSAGE);

        // 3. Score calculation [cite: 10]
        calculateRoundScores();
        
        // 4. Award chips and prepare for next round
        playerMoney += 5;
        bot1.addMoney(5); 
        bot1MoneyUI += 5; 
        bot2.addMoney(5);
        bot2MoneyUI += 5; 

        currentRound++;
        if (currentRound > 5) {
            showGameOver();
        } else {
            refreshUI();
        }
    }

    // Apply the card value and its effect to the table total
    private void applyCardToTotal(Card c, String playerName, StringBuilder summary) {
        summary.append("- ").append(playerName).append(" played card ").append(c.getValue());
        
        if (c.getType() instanceof Zero) {
            cumulativeTotal = 0;
            isZeroActive = true; // Lock table total
            summary.append(" (Used ZERO: Total locked to 0)\n");
        } else if (c.getType() instanceof Negative) {
            if (!isZeroActive) cumulativeTotal -= c.getValue();
            summary.append(" (Used NEGATIVE: Added as -").append(c.getValue()).append(")\n");
        } else {
            if (!isZeroActive) cumulativeTotal += c.getValue();
            summary.append("\n");
        }
    }

    // Calculate scores based on proximity to target numbers [cite: 10]
    private void calculateRoundScores() {
        int pDiff = Math.min(Math.abs(cumulativeTotal - playerTargets[0]), Math.abs(cumulativeTotal - playerTargets[1]));
        int b1Diff = Math.min(Math.abs(cumulativeTotal - bot1.getTarget()[0]), Math.abs(cumulativeTotal - bot1.getTarget()[1]));
        int b2Diff = Math.min(Math.abs(cumulativeTotal - bot2.getTarget()[0]), Math.abs(cumulativeTotal - bot2.getTarget()[1]));

        class Result implements Comparable<Result> {
            String name; int diff;
            Result(String n, int d) { name = n; diff = d; }
            public int compareTo(Result o) { return Integer.compare(this.diff, o.diff); }
        }

        List<Result> results = new ArrayList<>();
        results.add(new Result("Player", pDiff));
        results.add(new Result("Bot1", b1Diff));
        results.add(new Result("Bot2", b2Diff));
        Collections.sort(results); 

        // Award points based on ranking (2 for first, 1 for second) [cite: 10]
        for (int i = 0; i < 3; i++) {
            int pts = (i == 0) ? 2 : (i == 1) ? 1 : 0;
            String winner = results.get(i).name;
            if (winner.equals("Player")) playerScore += pts;
            else if (winner.equals("Bot1")) bot1.addScore(pts); 
            else if (winner.equals("Bot2")) bot2.addScore(pts);
        }
    }

    // Final game over summary and exit
    private void showGameOver() {
        String msg = "===== GAME OVER! =====\n\n"
                   + "Your Score : " + playerScore + "\n"
                   + "Bot 1 Score : " + bot1.getScore() + "\n"
                   + "Bot 2 Score : " + bot2.getScore() + "\n\n";

        int max = Math.max(playerScore, Math.max(bot1.getScore(), bot2.getScore()));
        if (playerScore == max) {
            msg += "CONGRATULATIONS! YOU WIN!";
        } else {
            msg += "DEFEAT! Better luck next time.";
        }

        JOptionPane.showMessageDialog(this, msg, "Final Results", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    // UI Helper for left-aligned labels
    private JLabel createLeftTextLabel(String text, int size) {
        JLabel l = new JLabel(text, SwingConstants.LEFT);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        return l;
    }

    // UI Helper for center-aligned labels
    private JLabel createTextLabel(String text, int size) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        return l;
    }

    // UI Helper for card hand buttons
    private JButton createCardButton(String text) {
        JButton b = createStyledButton(text, new Color(180, 30, 30));
        b.setFont(new Font("Segoe UI", Font.BOLD, 40));
        b.addActionListener(e -> {
            if (selectedCardBtn != null && selectedCardBtn.isEnabled()) 
                selectedCardBtn.setBackground(new Color(180, 30, 30));
            selectedCardBtn = b;
            b.setBackground(new Color(255, 120, 120));
        });
        return b;
    }

    // Generic styled button helper
    private JButton createStyledButton(String text, Color c) {
        JButton b = new JButton(text);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        return b;
    }

    // Custom background panel with image 
    class BackgroundPanel extends JPanel {
        Image bgImg = new ImageIcon("loginbg.png").getImage();
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}