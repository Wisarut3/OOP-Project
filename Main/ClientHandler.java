package Main;

import java.net.*;
import java.io.*;
import java.util.concurrent.SynchronousQueue;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // แยก queue ตาม phase
    private final SynchronousQueue<GameMessage> lobbyQueue = new SynchronousQueue<>();
    private final SynchronousQueue<GameMessage> moveQueue = new SynchronousQueue<>();

    private volatile boolean running = true;
    private String username = "?";

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("[ClientHandler] Stream error: " + e.getMessage());
            running = false;
        }
    }

    @Override
    public void run() {
        System.out.println("[ClientHandler] Connected: " + socket.getInetAddress());
        while (running) {
            try {
                GameMessage msg = (GameMessage) in.readObject();
                switch (msg.getType()) {
                    case CREATE_ROOM, JOIN_ROOM ->
                        lobbyQueue.put(msg);
                    case SEND_MOVE ->
                        moveQueue.put(msg);
                    default -> {
                    }
                }
            } catch (EOFException | SocketException e) {
                System.out.println("[ClientHandler] " + username + " disconnected.");
                running = false;
            } catch (Exception e) {
                System.err.println("[ClientHandler] Error: " + e.getMessage());
                running = false;
            }
        }
        close();
    }

    public synchronized void send(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("[ClientHandler] Send failed");
            running = false;
        }
    }

    public GameMessage waitForLobby() {
        try {
            return lobbyQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public GameMessage waitForMove() {
        try {
            return moveQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public String getUsername() {
        return username;
    }

    public boolean isRunning() {
        return running;
    }

    public void close() {
        running = false;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
