package Main;

import Project_ui.GameUI;

public class LocalHuman extends Player {

    private final GameUI ui;
    private volatile Card selectedCard;
    private volatile boolean waitingForInput;

    public LocalHuman(GameUI ui) {
        this.ui = ui;
        for (int i = 1; i <= 5; i++) {
            cardList.add(new Card(i));
        }
    }

    @Override
    public boolean buyItem(String efc) {
        if (money >= 5) {
            money -= 5;
            if (efc.equals("Negative")) efcList.add(new Negative());
            else if (efc.equals("Zero")) efcList.add(new Zero());
            return true;
        }
        return false;
    }

    @Override
    //Runs when GameEngine calls LocalPlayer.selectMove()
    public Card selectMove() {
        //Send player's card and effect to GUI
        int[] handValues = cardList.stream().mapToInt(Card::getValue).toArray();
        String[] inv = efcList.stream().map(e -> e.getClass().getSimpleName()).toArray(String[]::new);
        
        ui.onGameState(target, money, score, handValues, inv);
        ui.onRequestMove(null);

        //Wait until player choose the card
        waitingForInput = true;
        while (waitingForInput) {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
        
        return selectedCard;
    }

    //Runs after clicking play card button
    public void submitMove(int cardIdx, String buyEfc, String useEfc) {
        if (!buyEfc.equals("None")) buyItem(buyEfc);
        
        if (cardIdx >= 0 && cardIdx < cardList.size()) {
            selectedCard = cardList.remove(cardIdx);
        } else {
            selectedCard = cardList.remove(0); // Fallback ป้องกัน Error
        }
        
        if (!useEfc.equals("None")) {
            for (EffectCard e : efcList) {
                if (e.getClass().getSimpleName().equals(useEfc)) {
                    efcList.remove(e);
                    selectedCard.setType(e);
                    break;
                }
            }
        }
        waitingForInput = false; // ปลดล็อกให้ GameEngine วิ่งต่อ
    }
}