package Main;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class GameServer2 {

    private static final int DEFAULT_PORT = 9999;
    private static final int DEFAULT_NUM_HUMAN = 3;

    // ======================================================
    //  Static room registry  (roomCode -> port)
    //  ใช้สำหรับ Join ตรวจสอบว่ารหัสห้องถูกต้องไหม
    // ======================================================
    private static final Map<String, Integer> activeRooms = new HashMap<>();

    public static boolean roomExists(String code) {
        return activeRooms.containsKey(code);
    }

    public static int getPortForRoom(String code) {
        return activeRooms.getOrDefault(code, -1);
    }

    // ======================================================
    //  Instance fields
    // ======================================================
    private final int port;
    private final int numHumans;
    private final String roomCode;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();

    // Callback แจ้ง UI ว่ามีผู้เล่นใหม่เข้ามา  (playerNumber 1-based)
    private Consumer<Integer> onPlayerJoined;

    public GameServer2(int port, int numHumans, String roomCode) {
        this.port = port;
        this.numHumans = numHumans;
        this.roomCode = roomCode;
    }

    public void setOnPlayerJoined(Consumer<Integer> callback) {
        this.onPlayerJoined = callback;
    }

    // ======================================================
    //  Start
    // ======================================================
    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            activeRooms.put(roomCode, port);          // ลงทะเบียนห้อง

            System.out.println("=== Game Server started  port=" + port
                    + "  room=" + roomCode + " ===");

            for (int i = 0; i < numHumans; i++) {
                Socket s = serverSocket.accept();
                ClientHandler handler = new ClientHandler(s);
                clients.add(handler);
                new Thread(handler).start();
                handler.send(GameMessage.welcome(i));
                System.out.println("Player " + (i + 1) + " connected.");

                // แจ้ง Lobby UI
                if (onPlayerJoined != null) {
                    final int num = i + 1;
                    onPlayerJoined.accept(num);
                }
            }

            System.out.println("All players connected. Starting game!\n");
            activeRooms.remove(roomCode);             // ห้องเต็ม ลบออก
            runGame();

        } catch (IOException e) {
            System.err.println("[GameServer] Error: " + e.getMessage());
        } finally {
            activeRooms.remove(roomCode);
            closeAll();
        }
    }

    // ======================================================
    //  Run game
    // ======================================================
    private void runGame() {
        List<Player> players = new ArrayList<>();
        int numBots = Math.max(0, 3 - numHumans);
        for (int i = 0; i < numBots; i++) players.add(new Bot());
        for (int i = 0; i < clients.size(); i++)
            players.add(new NetworkHuman(clients.get(i), numBots + i));

        Thread t = new Thread(new GameEngine(players, this));
        t.start();
        try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public void broadcastState(GameMessage msg) {
        for (ClientHandler h : clients) if (h.isRunning()) h.send(msg);
    }

    private void closeAll() {
        for (ClientHandler h : clients) h.close();
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    // ======================================================
    //  Main (standalone)
    // ======================================================
    public static void main(String[] args) {
        int port      = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        int numHuman  = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_NUM_HUMAN;
        String code   = args.length > 2 ? args[2] : "0000";
        new GameServer2(port, numHuman, code).startServer();
    }
}
