package Project_ui;

import Main.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class OfflineMode extends JFrame {

//    private final JFrame owner;
    private int selectedValue = -1;
    public int getSelectedCardValue() { return selectedValue; }
    private final GameEngine engine;
    private final Human player;

    // UI
    public JLabel target1Label, target2Label;
    public JLabel moneyLabel, squadStatusLabel, scoreLabel;
    public JPanel cardContainer;
    
    // CARD STATE
    private CardUI currentSelectedCard = null;
    
    private final Color COLOR_PANEL = new Color(30, 30, 30, 220);
    private final Color COLOR_ACCENT = new Color(120, 0, 0);
    private final Color COLOR_RED = new Color(180, 0, 0);


    public OfflineMode() {
        player = new Human();
        engine = new GameEngine(player);
        new Thread(engine).start();
//        this.owner = owner;
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // link engine ****
        target1Label = new JLabel(String.valueOf(player.getTarget()[0]));
        target2Label = new JLabel(String.valueOf(player.getTarget()[1]));
        moneyLabel = new JLabel(String.valueOf(player.getMoney()));
        squadStatusLabel = new JLabel("READY");
        scoreLabel = new JLabel("0", JLabel.CENTER);
        scoreLabel.setText("XX"); // engine

        // main BACKGROUND 
        BackgroundPanel mainBg = new BackgroundPanel();
        mainBg.setLayout(new BorderLayout(20, 20));
        mainBg.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setContentPane(mainBg);

        mainBg.add(createTopBar(), BorderLayout.NORTH);
        mainBg.add(createMainLayout(), BorderLayout.CENTER);
        cardContainer = createBottomBar();
        mainBg.add(cardContainer, BorderLayout.SOUTH);
    }

    ////////////////////////// 3-column layout /////////////////////////////
    private JPanel createMainLayout() {

        JPanel grid = new JPanel(new GridLayout(1, 3, 30, 0));
        grid.setOpaque(false);

        //LEFT 
        JPanel leftCol = new JPanel(new GridLayout(2, 1, 0, 20));
        leftCol.setOpaque(false);

        RoundedPanel targetP = new RoundedPanel("YOUR TARGETS");
        addInfoRow(targetP, "TARGET 1 :", target1Label);
        addInfoRow(targetP, "TARGET 2 :", target2Label);
        leftCol.add(targetP);

        RoundedPanel squadP = new RoundedPanel("SQUAD STATUS");
        addInfoRow(squadP, "SOLO-PPP :", squadStatusLabel);
        leftCol.add(squadP);

        //CENTER
        RoundedPanel midCol = new RoundedPanel("");
        midCol.setLayout(new BorderLayout());

        // Big score display
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 150));
        scoreLabel.setForeground(new Color(255, 80, 80));
        midCol.add(scoreLabel, BorderLayout.CENTER);

        // Execute button 
        JButton btnExecute = createStyledButton("EXECUTE TURN", COLOR_ACCENT);
        btnExecute.setPreferredSize(new Dimension(0, 60));

        btnExecute.addActionListener(e -> {
            System.out.println("EXECUTE TURN pressed");

            if (selectedValue == -1) {
                System.out.println("No card selected");
            } else {
                System.out.println("Using card: " + selectedValue);
                Iterator<Card> it = player.getCardList().iterator();
                while (it.hasNext()){
                    Card c = it.next();
                    if (c.getValue() == selectedValue){
                        it.remove();
                        if(player.getCardList().size() < 4 & engine.getTotal() == 0){
                            scoreLabel.setText(String.valueOf(engine.getTotal()));
                        }
                        else{
                            scoreLabel.setText(String.valueOf(engine.getTotal() + c.getValue()));
                        }
                        player.setUse(c);
                        break;
                    }
                }
                updateUI();
                selectedValue = -1;
            }
        });

        midCol.add(btnExecute, BorderLayout.SOUTH);

        //RIGHT
        JPanel rightCol = new JPanel(new GridLayout(2, 1, 0, 20));
        rightCol.setOpaque(false);

        // Assets panel
        RoundedPanel assetP = new RoundedPanel("ASSETS");
        addInfoRow(assetP, "TOTAL MONEY :", moneyLabel);

        JButton negBtn = createStyledButton("BUY NEGATIVE", COLOR_ACCENT);
        JButton zeroBtn = createStyledButton("BUY ZERO", COLOR_ACCENT);
        negBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        negBtn.addActionListener(e -> {
            if(player.buyItem("Negative")){
                updateUI();
            }
        });

        zeroBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        zeroBtn.addActionListener(e -> {
            if(player.buyItem("Zero")){
                updateUI();   
            }
        });
        
        assetP.addContentRow(Box.createVerticalStrut(15));
        assetP.addContentRow(zeroBtn);
        assetP.addContentRow(negBtn);

        rightCol.add(assetP);

        // Items panel
        RoundedPanel itemP = new RoundedPanel("TACTICAL ITEMS");
        JLabel efcList = new JLabel("Empty");
        addInfoRow(itemP, "ITEM SLOTS :", efcList);
        rightCol.add(itemP);

        grid.add(leftCol);
        grid.add(midCol);
        grid.add(rightCol);

        return grid;
    }

    ////////////////////////// CARD BAR ///////////////////////////////

    private JPanel createBottomBar() {

        JPanel cardContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 70));
        cardContainer.setOpaque(false);
        cardContainer.setPreferredSize(new Dimension(0, 300));

        for(int i = 0; i < player.getCardList().size(); i++){
            int cardValue = player.getCardList().get(i).getValue();
            cardContainer.add(new CardUI(cardValue));
        }
        
        return cardContainer;
    }
    
    public void updateUI(){
        cardContainer.removeAll();
        
        for(int i = 0; i < player.getCardList().size(); i++){
            int cardValue = player.getCardList().get(i).getValue();
            cardContainer.add(new CardUI(cardValue));
        }
        
        cardContainer.revalidate();
        cardContainer.repaint();
        
        moneyLabel.setText(String.valueOf(player.getMoney()));
        
    }

    ////////////////////////// CARD CLASS /////////////////////////////
    class CardUI extends JPanel {

        private int value;
        private boolean isSelected = false;
        private Image cardBg;

        public CardUI(int val) {
            this.value = val;
            
            //card background
            cardBg = new ImageIcon("card_bg.png").getImage();

            setPreferredSize(new Dimension(90, 130));
            setOpaque(false);

            //CLICK 
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {

                    System.out.println("Card " + value + " clicked");

                    if (isSelected) deselectCard();
                    else {
                        if (currentSelectedCard != null)
                            currentSelectedCard.deselectCard();
                        selectCard();
                    }
                }
            });
        }

        
        private void selectCard() {
            isSelected = true;
            currentSelectedCard = this;
            selectedValue = value;
            repaint();
        }

        
        private void deselectCard() {
            isSelected = false;

            if (currentSelectedCard == this) {
                currentSelectedCard = null;
                selectedValue = -1;
            }

            repaint();
        }

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 15;

            // Draw rounded
            if (cardBg != null) {
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                g2.drawImage(cardBg, 0, 0, getWidth(), getHeight(), this);
                g2.setClip(null);
            }

            //CARD NUMBE
            String text = String.valueOf(value);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 30));

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 5;

            // Shadow
            g2.setColor(new Color(0,0,0,150));
            g2.drawString(text, x+2, y+2);

            // Text
            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);

            // SELECTED BORDER
            if (isSelected) {
                g2.setColor(COLOR_RED);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, arc, arc);
            }

            g2.dispose();
        }
    }

    ////////////////////////// INFO ROW ///////////////////////////////
    // label + value

    private void addInfoRow(RoundedPanel panel, String title, JLabel valueLabel) {

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel tL = new JLabel(title);
        tL.setForeground(Color.LIGHT_GRAY);

        valueLabel.setForeground(Color.WHITE);

        row.add(tL, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);

        panel.addContentRow(row);
    }

    ////////////////////////// TOP BAR ////////////////////////////////

    private JPanel createTopBar() {

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JButton backBtn = createStyledButton("←", new Color(80, 80, 80));
        backBtn.addActionListener(e -> {
            System.out.println("BACK pressed");
//            owner.setVisible(true);
            this.dispose();
        });

        JButton close = new JButton("✕");
        close.setForeground(Color.WHITE);
        close.setBackground(new Color(255, 80, 80));
        close.setFocusPainted(false);
        close.setBorderPainted(false);

        close.addActionListener(e -> {
            System.out.println("EXIT pressed");
            System.exit(0);
        });


        p.add(backBtn, BorderLayout.WEST);
        p.add(close, BorderLayout.EAST);

        return p;
    }

    ////////////////////////// PANEL CLASS ////////////////////////////
    // Custom rounded panel with title + content area
    
    class RoundedPanel extends JPanel {

        private String title;
        private JPanel inner = new JPanel();

        public RoundedPanel(String title) {

            this.title = title;

            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(50, 15, 15, 15));

            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            inner.setOpaque(false);

            add(inner, BorderLayout.CENTER);
        }

        public void addContentRow(Component c) {
            inner.add(c);
            inner.add(Box.createVerticalStrut(10));
        }

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setColor(COLOR_PANEL);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

            g2.setColor(Color.WHITE);
            if (!title.isEmpty()) g2.drawString(title, 25, 30);

            g2.dispose();
        }
    }

    ////////////////////////// BACKGROUND /////////////////////////////
    // background image 
    class BackgroundPanel extends JPanel {

        private Image bgImg = new ImageIcon("loginbg.png").getImage();

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);

            // Dark overlay for readability
            g.setColor(new Color(0,0,0,120));
            g.fillRect(0,0,getWidth(),getHeight());
        }
    }

    ////////////////////////// BUTTON STYLE ///////////////////////////

    private JButton createStyledButton(String text, Color color) {

        JButton btn = new JButton(text) {

            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                int arc = 40;

                // Shadow layer
                g2.setColor(color.darker());
                g2.fillRoundRect(0, 2, getWidth(), getHeight()-2, arc, arc);

                // Main button
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight()-4, arc, arc);

                // Text
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

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });

        return btn;
    }
    
    public static void main(String[] args) {
        new OfflineMode().setVisible(true);
    }

}
