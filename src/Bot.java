
public class Bot extends Player {

    public Bot() {
        for (int i = 1; i <= 5; i++) {
            cardList.add(new Card(i));
        }
    }

    @Override
    public boolean buyItem(String efc) {
        if (money >= 5) {
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
        return cardList.remove(0);
    }
}
