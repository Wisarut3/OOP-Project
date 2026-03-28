package Main;

import java.util.Random;

public class Bot extends Player {

    private int effectUsed = 0;
    private final int MAX_EFFECT = 2;

    public Bot() {
        for (int i = 1; i <= 5; i++) {
            cardList.add(new Card(i));
        }
    }

    @Override
    public boolean buyItem(String efc) {
        if (money >= 5) {
            money -= 5;
            if (efc.equals("Negative")) {
                efcList.add(new Negative());
            } else if (efc.equals("Zero")) {
                efcList.add(new Zero());
            }
            return true;
        }
        return false;
    }

    @Override
    public Card selectMove() {
        Random rand = new Random();
        int index = rand.nextInt(cardList.size());
        String effect;
        if (effectUsed < MAX_EFFECT && money >= 5) {
            String[] effects = {"Negative", "Zero"};
            effect = effects[rand.nextInt(2)];
            buyItem(effect);
        } else {
            effect = "None";
        }
        Card using = cardList.remove(index);
        if (effect.equals("Negative")) {
            using.setType(efcList.remove(0));
            effectUsed++;
        } else if (effect.equals("Zero")) {
            using.setType(efcList.remove(0));
            effectUsed++;
        }
        return using;
    }
}
