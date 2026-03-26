package Main;

import java.net.*;
import java.io.*;
import java.util.*;

public class GameClient {

    private static final int DEFAULT_PORT = 9999;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Player localPlayer;
    private int playerIndex = -1;

    private final Scanner scanner = new Scanner(System.in);

    // ---- Connect ----
    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected to server " + ip + ":" + port);
            listen();
        } catch (IOException e) {
            System.err.println("[Client] Cannot connect: " + e.getMessage());
        }
    }

    // ---- Main message loop ----
    private void listen() {
        try {
            while (true) {
                GameMessage msg = (GameMessage) in.readObject();
                handleMessage(msg);
            }
        } catch (EOFException | SocketException e) {
            System.out.println("[Client] Disconnected from server.");
        } catch (Exception e) {
            System.err.println("[Client] Error: " + e.getMessage());
        }
    }

    private void handleMessage(GameMessage msg) throws IOException {
        switch (msg.getType()) {

            case WELCOME:
                playerIndex = msg.getPlayerIndex();
                System.out.println("\n=== Welcome! You are Player " + (playerIndex + 1) + " ===\n");
                break;

            case GAME_STATE:
                displayState(msg);
                break;

            case REQUEST_MOVE:
                handleMoveRequest();
                break;

            case ROUND_RESULT:
                displayRoundResult(msg);
                break;

            case GAME_OVER:
                displayGameOver(msg);

                System.exit(0);
                break;

            default:
                break;
        }
    }

    // ---- Display helpers ----
    private int[] lastHandValues = new int[0];
    private String[] lastEffectInv = new String[0];
    private int lastMoney = 0;

    private void displayState(GameMessage msg) {
        lastHandValues = msg.getHandValues();
        lastEffectInv = msg.getEffectInventory();
        lastMoney = msg.getMoney();

        System.out.println("-----------------------------");
        System.out.println("Your Targets : " + msg.getTargets()[0] + " and " + msg.getTargets()[1]);
        System.out.println("Coins        : " + msg.getMoney());
        System.out.println("Score        : " + msg.getScore());
        System.out.print("Hand         : ");
        for (int i = 0; i < lastHandValues.length; i++) {
            System.out.print("[" + (i + 1) + "]" + lastHandValues[i] + "  ");
        }
        System.out.println();
        System.out.print("Effects      : ");
        if (lastEffectInv.length == 0) {
            System.out.print("(none)");
        } else {
            Arrays.stream(lastEffectInv).forEach(e -> System.out.print(e + "  "));
        }
        System.out.println("\n-----------------------------");
    }

    private void displayRoundResult(GameMessage msg) {
        System.out.println("\n>>> Round Result <<<");
        System.out.println(msg.getRoundLog());
        int[] scores = msg.getAllScores();
        System.out.println("Current Scores:");
        for (int i = 0; i < scores.length; i++) {
            System.out.println("  Player " + (i + 1) + " : " + scores[i] + " pts");
        }
        System.out.println();
    }

    private void displayGameOver(GameMessage msg) {
        System.out.println("\n========= GAME OVER =========");
        String[] names = msg.getPlayerNames();
        int[] scores = msg.getFinalScores();
        int maxScore = Arrays.stream(scores).max().getAsInt();
        for (int i = 0; i < names.length; i++) {
            String tag = (scores[i] == maxScore) ? " <-- WINNER" : "";
            System.out.println("  " + names[i] + " : " + scores[i] + " pts" + tag);
        }
        System.out.println("=============================");
    }

    // ---- Move input ----
    private void handleMoveRequest() throws IOException {
        System.out.println("\n*** Your Turn! ***");

        // ── ซื้อ effect ──
        String buyEffect = "None";
        System.out.println("Do you want to buy an effect? (y/n)");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("You have " + lastMoney + " coins.");
            if (lastMoney < 5) {
                System.out.println("Not enough coins (need 5).");
            } else {
                System.out.println("Which effect? (n = Negative / z = Zero)");
                String b = scanner.nextLine().trim().toLowerCase();
                if (b.equals("n")) {
                    buyEffect = "Negative";
                } else if (b.equals("z")) {
                    buyEffect = "Zero";
                } else {
                    System.out.println("Invalid choice, skipping purchase.");
                }
            }
        }

        System.out.print("Choose card (1 - " + lastHandValues.length + "): ");
        int cardIdx = 0;
        try {
            cardIdx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (cardIdx < 0 || cardIdx >= lastHandValues.length) {
                cardIdx = 0;
            }
        } catch (NumberFormatException e) {
            cardIdx = 0;
        }
        System.out.println("Card " + lastHandValues[cardIdx] + " selected.");

        // ── ใช้ effect ──
        String useEffect = "None";
        // รวม inventory + effect
        List<String> available = new ArrayList<>(Arrays.asList(lastEffectInv));
        if (!buyEffect.equals("None")) {
            available.add(buyEffect);
        }

        if (!available.isEmpty()) {
            System.out.println("Your effects: " + available);
            System.out.print("Use effect? (None / Negative / Zero): ");
            String u = scanner.nextLine().trim();
            if (available.contains(u)) {
                useEffect = u;
            } else {
                System.out.println("Effect not found, not using.");
            }
        }

        // ── ส่งกลับ server ──
        sendSelection(GameMessage.sendMove(cardIdx, buyEffect, useEffect));
        System.out.println("Move sent!\n");
    }

    // ---- Send ----
    public void sendSelection(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("[Client] Send error: " + e.getMessage());
        }
    }

    // ---- Main ----
    public static void main(String[] args) {
        String ip = (args.length > 0) ? args[0] : "localhost";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        new GameClient().connect(ip, port);
    }
}
