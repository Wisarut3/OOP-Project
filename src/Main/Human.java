package Main;

import java.util.*;

public class Human extends Player {
    public Card using;
    public volatile boolean waitingInput = false;
    public volatile boolean roundDone;

    public Human() {
        for (int i = 1; i <= 5; i++) {
            cardList.add(new Card(i));
        }
    }

    @Override
    public boolean buyItem(String efc) {
        if (money >= 5) {
            money -= 5;
            if (efc.equals("Negative")) {
                System.out.println("Bought negative.");
                efcList.add(new Negative());
            } else if (efc.equals("Zero")) {
                System.out.println("Bought zero.");
                efcList.add(new Zero());
            }
            return true;
        }
        return false;
    }

    public EffectCard useEffect(String efc) {
        for (EffectCard c : efcList) {
            if (efc.equals(c.getClass().getSimpleName())) {
                System.out.println("Using " + efc + ".");
                return efcList.remove(efcList.indexOf(c));
            }
        }
        System.out.println("No " + efc + "in the inventory.");
        return null;
    }

    @Override
    public Card selectMove() {
        waitingInput = true;
        while(waitingInput){
            try{
                Thread.sleep(100);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        return using;
    }
    
    public void setUse(Card c){
        using = c;
        waitingInput = false;
    }
    
    public int getMoney(){
        return money;
    }
    
    public ArrayList<EffectCard> getEfcList(){
        return efcList;
    }
    
    public ArrayList<Card> getCardList(){
        return cardList;
    }
}
