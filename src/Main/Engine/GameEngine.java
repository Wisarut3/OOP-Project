
import java.util.*;

public class GameEngine implements Runnable {

    private final ArrayList<Player> playerList;
    private int cumulativeTotal;
    private boolean zero;
    private GameServer server; // null = offline mode

    // offline mode
    public GameEngine() {
        playerList = new ArrayList<>();
        playerList.add(new Bot());
        playerList.add(new Bot());
        playerList.add(new Human());
        init();
    }

    // network mode
    public GameEngine(List<Player> players, GameServer server) {
        playerList = new ArrayList<>(players);
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
        for (int i = 0; i < playerList.size(); i++) {
            System.out.println("Target of Player " + (i + 1)
                    + " = " + playerList.get(i).getTarget()[0]
                    + " and " + playerList.get(i).getTarget()[1]);
        }
    }

    public void setZero() {
        cumulativeTotal = 0;
        zero = true;
    }

    public void addTotal(int score) {
        if (!zero) {
            cumulativeTotal += score;
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
        System.out.println("Player " + (playerList.indexOf(sorted.get(0)) + 1) + " got 2 points");
        sorted.get(1).addScore(1);
        System.out.println("Player " + (playerList.indexOf(sorted.get(1)) + 1) + " got 1 point");
        System.out.println();
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
        for (Player p : playerList) {
            Card c = p.selectMove();
            calculateEffect(p, c, c.getType());
            p.addMoney(5);
        }
        zero = false;
        log.append("Total on table = ").append(cumulativeTotal).append("\n");
        System.out.println(log);
        calculateScore();

        if (server != null) {
            int[] allScores = playerList.stream().mapToInt(Player::getScore).toArray();
            server.broadcastState(
                    GameMessage.roundResult(cumulativeTotal, allScores, log.toString()));
        }
    }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            calculateRound(i);
        }

        System.out.println("\nTotal score:");
        String[] names = new String[playerList.size()];
        int[] finalScores = new int[playerList.size()];
        for (int i = 0; i < playerList.size(); i++) {
            names[i] = "Player " + (i + 1);
            finalScores[i] = playerList.get(i).getScore();
            System.out.println(names[i] + " got " + finalScores[i] + " points");
        }
        if (server != null) {
            server.broadcastState(GameMessage.gameOver(names, finalScores));
        }
    }

    public static void main(String[] args) {
        new Thread(new GameEngine()).start();
    }
}
