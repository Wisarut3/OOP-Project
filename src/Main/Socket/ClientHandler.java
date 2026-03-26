import java.net.*;
import java.io.*;
import java.util.concurrent.SynchronousQueue;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // SynchronousQueue
    private final SynchronousQueue<GameMessage> moveQueue = new SynchronousQueue<>();

    private volatile boolean running = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in  = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("[ClientHandler] Failed to open streams: " + e.getMessage());
            running = false;
        }
    }

    @Override
    public void run() {
        System.out.println("[ClientHandler] Client connected: " + socket.getInetAddress());
        while (running) {
            try {
                GameMessage msg = (GameMessage) in.readObject();
                if (msg.getType() == GameMessage.Type.SEND_MOVE) {
                    moveQueue.put(msg);
                }
            } catch (EOFException | SocketException e) {
                System.err.println("[ClientHandler] Client disconnected.");
                running = false;
            } catch (Exception e) {
                System.err.println("[ClientHandler] Error reading: " + e.getMessage());
                running = false;
            }
        }
        close();
    }

    // ---- API ที่ GameEngine / GameServer

    /** ส่ง GameMessage client */
    public synchronized void send(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset(); // reset object cache
        } catch (IOException e) {
            System.err.println("[ClientHandler] Send failed: " + e.getMessage());
            running = false;
        }
    }


    public GameMessage waitForMove() {
        try {
            return moveQueue.take(); // block
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public boolean isRunning() { return running; }

    public void close() {
        running = false;
        try { socket.close(); } catch (IOException ignored) {}
    }
}
