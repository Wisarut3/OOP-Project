package Main;

import java.util.*;

public class Human extends Player {

    Scanner choose = new Scanner(System.in);

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

        //Card Choosing Phase
        System.out.print("Choose your card. (1 - " + cardList.size() + "): ");
        int index = choose.nextInt() - 1;
        Card using = cardList.remove(index);
        choose.nextLine();

        //Effect Buying Phase
        System.out.println("Do you want to buy effects? (y/n): ");
        if (choose.nextLine().toLowerCase().equals("y")) {
            System.out.println("You have " + money + " coins.");
            System.out.println("Which effect do you want to buy? (n/z)");
            String buying = choose.nextLine().toLowerCase();
            if (buying.equals("n")) {
                if (buyItem("Negative") == false) {
                    System.out.println("Not enough coins.");
                }
            } else if (buying.equals("z")) {
                if (buyItem("Zero") == false) {
                    System.out.println("Not enough coins.");
                }
            }
        }

        //Effect Choosing Phase
        if (!efcList.isEmpty()) {
            System.out.println("Your effect list:");
            for (EffectCard c : efcList) {
                System.out.print(c.getClass().getSimpleName() + "\n");
            }
            System.out.print("Choose your effect. (None, Negative, Zero): ");
            String effect = choose.nextLine().trim().toLowerCase();
            if (effect.equals("negative")) {
                using.setType(useEffect("Negative"));
            } else if (effect.equals("zero")) {
                using.setType(useEffect("Zero"));
            }
        }
        System.out.println((using.getValue()) + " Chosen, removed from the hand");
        System.out.println("");
        return using;
    }
}
