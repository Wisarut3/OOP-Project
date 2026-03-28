package Main;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * GameServer — รันบนเครื่อง host รับ connection ต่อเนื่อง, จัดการ Room Code,
 * เริ่มเกมเมื่อห้องเต็ม
 *
 * รัน: java Main.GameServer (หรือ รันจาก main() ด้านล่าง)
 */
public class GameServer {

    private static final int PORT = 9999;

    private final RoomManager roomManager = new RoomManager();

    public void start() {
        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("=== GameServer on port " + PORT + " ===");
            while (true) {
                Socket s = ss.accept();
                ClientHandler handler = new ClientHandler(s);
                new Thread(handler).start();
                new Thread(() -> handleClient(handler)).start();
            }
        } catch (IOException e) {
            System.err.println("[GameServer] Fatal: " + e.getMessage());
        }
    }

    // ── ดูแล client แต่ละคนตั้งแต่ต้น ──────────────────────────────────
    private void handleClient(ClientHandler handler) {
        // รอรับ CREATE_ROOM หรือ JOIN_ROOM
        GameMessage msg = handler.waitForLobby();
        if (msg == null) {
            return;
        }

        if (msg.getType() == GameMessage.Type.CREATE_ROOM) {
            handleCreate(handler, msg);
        } else if (msg.getType() == GameMessage.Type.JOIN_ROOM) {
            handleJoin(handler, msg);
        }
    }

    // ── สร้างห้องใหม่ ────────────────────────────────────────────────────
    private void handleCreate(ClientHandler handler, GameMessage msg) {
        String username = msg.getUsername();
        int size = Math.max(1, Math.min(3, msg.getRoomSize()));
        handler.setUsername(username);

        RoomManager.Room room = roomManager.createRoom(username, size, handler);

        // ส่ง room code กลับให้ host แสดงบนหน้าจอ
        handler.send(GameMessage.roomCode(room.getCode(), size));
        broadcastStatus(room);

        // block รอให้ห้องเต็ม
        while (!room.isFull()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }

        startGame(room);
        roomManager.removeRoom(room.getCode());
    }

    // ── เข้าร่วมห้องด้วย code ────────────────────────────────────────────
    private void handleJoin(ClientHandler handler, GameMessage msg) {
        String code = msg.getRoomCode().toUpperCase();
        String username = msg.getUsername();
        handler.setUsername(username);

        RoomManager.Room room = roomManager.getRoom(code);

        if (room == null || room.isStarted()) {
            handler.send(GameMessage.roomJoinFail("Room not found"));
            return;
        }
        if (room.isFull()) {
            handler.send(GameMessage.roomJoinFail("Room is full"));
            return;
        }

        room.join(handler, username);
        handler.send(GameMessage.roomJoinOk(code, room.getCurrentCount() - 1));
        broadcastStatus(room);

        System.out.println("[Room " + code + "] " + username + " joined ("
                + room.getCurrentCount() + "/" + room.getMaxHumans() + ")");
        // client นี้แค่รอ GAME_START — host thread จะ start เกม
    }

    // ── เริ่มเกมเมื่อห้องเต็ม ────────────────────────────────────────────
    private void startGame(RoomManager.Room room) {
        room.markStarted();
        List<ClientHandler> clients = room.getClients();
        List<Player> players = new ArrayList<>();

        int numBots = Math.max(0, 3 - clients.size());
        for (int i = 0; i < numBots; i++) {
            players.add(new Bot());
        }
        for (int i = 0; i < clients.size(); i++) {
            int idx = numBots + i;
            clients.get(i).send(GameMessage.gameStart(idx));
            players.add(new NetworkHuman(clients.get(i), idx));
        }

        System.out.println("[Room " + room.getCode() + "] Game starting! "
                + clients.size() + " humans + " + numBots + " bots");

        // broadcast ผ่าน RoomBroadcaster
        RoomBroadcaster bc = new RoomBroadcaster(room);
        Thread t = new Thread(new GameEngine(players, bc));
        t.start();
        try {
            t.join();
        } catch (InterruptedException ignored) {
        }
    }

    private void broadcastStatus(RoomManager.Room room) {
        String[] names = room.getUsernames().toArray(new String[0]);
        room.broadcast(GameMessage.roomStatus(
                room.getCurrentCount(), room.getMaxHumans(), names));
    }

    // broadcastState สำหรับ GameEngine เรียก
    public void broadcastState(GameMessage msg) {
        /* override ใน RoomBroadcaster */ }

    // ── Inner adapter ─────────────────────────────────────────────────────
    public static class RoomBroadcaster extends GameServer {

        private final RoomManager.Room room;

        RoomBroadcaster(RoomManager.Room r) {
            this.room = r;
        }

        @Override
        public void broadcastState(GameMessage msg) {
            room.broadcast(msg);
        }

        @Override
        public void start() {
        }
    }

    public static void main(String[] args) {
        new GameServer().start();
    }
}
