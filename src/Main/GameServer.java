package Main;

import java.net.*;
import java.io.*;
import java.util.*;

public class GameServer {

    private static final int DEFAULT_PORT = 9999;
    private static final int DEFAULT_NUM_HUMAN = 3;

    private final int port;
    private final int numHumans;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();

    public GameServer(int port, int numHumans) {
        this.port = port;
        this.numHumans = numHumans;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("=== Game Server started on port " + port + " ===");
            System.out.println("Waiting for " + numHumans + " human player(s)...");

            for (int i = 0; i < numHumans; i++) {
                Socket s = serverSocket.accept();
                ClientHandler handler = new ClientHandler(s);
                clients.add(handler);
                new Thread(handler).start();
                handler.send(GameMessage.welcome(i));
                System.out.println("Player " + (i + 1) + " connected.");
            }

            System.out.println("All players connected. Starting game!\n");
            runGame();

        } catch (IOException e) {
            System.err.println("[GameServer] Error: " + e.getMessage());
        } finally {
            closeAll();
        }
    }

    private void runGame() {
        List<Player> players = new ArrayList<>();
        int numBots = Math.max(0, 3 - numHumans);
        for (int i = 0; i < numBots; i++) {
            players.add(new Bot());
        }
        for (int i = 0; i < clients.size(); i++) {
            players.add(new NetworkHuman(clients.get(i), numBots + i));
        }

        Thread t = new Thread(new GameEngine(players, this));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void broadcastState(GameMessage msg) {
        for (ClientHandler h : clients) {
            if (h.isRunning()) {
                h.send(msg);
            }
        }
    }

    private void closeAll() {
        for (ClientHandler h : clients) {
            h.close();
        }
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        int numHuman = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_NUM_HUMAN;
        new GameServer(port, numHuman).startServer();
    }
}
