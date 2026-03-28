package Main;

import java.util.*;

public class NetworkHuman extends Player {

    private final ClientHandler handler;
    private final int playerIndex;

    public NetworkHuman(ClientHandler handler, int playerIndex) {
        this.handler = handler;
        this.playerIndex = playerIndex;
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
        int[] handValues = cardList.stream().mapToInt(Card::getValue).toArray();
        String[] inv = efcList.stream()
                .map(e -> e.getClass().getSimpleName())
                .toArray(String[]::new);

        handler.send(GameMessage.gameState(
                0, target, money, score, handValues, inv));
        handler.send(GameMessage.requestMove(0));

        GameMessage reply = handler.waitForMove();
        if (reply == null) {

            return cardList.remove(0);
        }

        int cardIdx = reply.getCardIndex();
        String buyEfc = reply.getBuyEffect();
        String useEfc = reply.getUseEffect();

        if (!buyEfc.equals("None")) {
            if (!buyItem(buyEfc)) {
                System.out.println("[Server] Player " + (playerIndex + 1)
                        + " tried to buy " + buyEfc + " but not enough coins.");
            }
        }

        if (cardIdx < 0 || cardIdx >= cardList.size()) {
            cardIdx = 0;
        }
        Card using = cardList.remove(cardIdx);

        if (!useEfc.equals("None")) {
            EffectCard found = null;
            for (EffectCard e : efcList) {
                if (e.getClass().getSimpleName().equals(useEfc)) {
                    found = e;
                    break;
                }
            }
            if (found != null) {
                efcList.remove(found);
                using.setType(found);
            }
        }

        System.out.println("[Server] Player " + (playerIndex + 1)
                + " played card " + using.getValue()
                + " with effect " + (using.getType() == null ? "None"
                : using.getType().getClass().getSimpleName()));
        return using;
    }
}
