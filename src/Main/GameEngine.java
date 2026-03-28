package Main;

import java.util.*;

public class GameEngine implements Runnable {

    private final ArrayList<Player> playerList;
    private int cumulativeTotal;
    private boolean zero;
    private GameServer server;

    // ── Network mode ──────────────────────────────────────────────────────
    public GameEngine(List<Player> players, GameServer server) {
        playerList = new ArrayList<>(players);
        this.server = server;
        init();
    }
    
    public GameEngine(Player player, GameServer server){
        playerList = new ArrayList<>();
        playerList.add(player);
        playerList.add(new Bot());
        playerList.add(new Bot());
        this.server = server;
        init();
    }

    private void init() {
        zero = false;
        Random rand = new Random();
        for (Player p : playerList) {
            p.setTarget(0, rand.nextInt(0, 11));
            p.setTarget(1, rand.nextInt(10, 21));
        }
    }

    public void setZero() {
        cumulativeTotal = 0;
        zero = true;
    }

    public void addTotal(int s) {
        if (!zero) {
            cumulativeTotal += s;
        }
    }

    public void calculateEffect(Player p, Card c, EffectCard efc) {
        if (efc == null) {
            addTotal(c.getValue());
            return;
        }
        efc.applyEffect(this, p, c);
    }

    public void addScore(ArrayList<Player> sorted) {
        sorted.get(0).addScore(2);
        sorted.get(1).addScore(1);
    }

    public void calculateScore() {
        for (Player p : playerList) {
            p.diff = Math.min(
                    Math.abs(cumulativeTotal - p.getTarget()[0]),
                    Math.abs(cumulativeTotal - p.getTarget()[1]));
        }
        ArrayList<Player> sorted = new ArrayList<>(playerList);
        sorted.sort(Comparator.comparing(Player::getDiff));
        addScore(sorted);
    }

    public void calculateRound(int roundNum) {
        cumulativeTotal = 0;
        StringBuilder log = new StringBuilder("--- Round " + roundNum + " ---\n");

        for (int i = 0; i < playerList.size(); i++) {
            Player p = playerList.get(i);
            Card c = p.selectMove();
            String eff = c.getType() == null ? "None"
                    : c.getType().getClass().getSimpleName();
            log.append("P").append(i + 1).append(": card=").append(c.getValue())
                    .append(" effect=").append(eff).append("\n");
            calculateEffect(p, c, c.getType());
            p.addMoney(5);
        }
        zero = false;
        log.append("Total = ").append(cumulativeTotal).append("\n");
        calculateScore();

        if (server != null) {
            int[] scores = playerList.stream().mapToInt(Player::getScore).toArray();
            server.broadcastState(
                    GameMessage.roundResult(cumulativeTotal, scores, log.toString()));
        }
    }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            calculateRound(i);
        }

        String[] names = new String[playerList.size()];
        int[] scores = new int[playerList.size()];
        for (int i = 0; i < playerList.size(); i++) {
            names[i] = "Player " + (i + 1);
            scores[i] = playerList.get(i).getScore();
        }
        if (server != null) {
            server.broadcastState(GameMessage.gameOver(names, scores));
        }
    }

//    public static void main(String[] args) {
//        new Thread(new GameEngine()).start();
//    }
}
