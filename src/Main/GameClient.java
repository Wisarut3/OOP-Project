package Main;

import Project_ui.GameUI;

import java.net.*;
import java.io.*;
import java.util.concurrent.SynchronousQueue;

/**
 * GameClient — เชื่อมต่อ GameServer และส่งข้อมูลไปมา ทุกการแสดงผลและรับ input
 * ทำผ่าน GameUI (Swing)
 */
public class GameClient {

    private static final int PORT = 9999;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Queue รับ move จาก GameUI แล้วส่งให้ server
    private final SynchronousQueue<GameMessage> moveQueue = new SynchronousQueue<>();

    private GameUI ui; // หน้าเกม (set จากภายนอกก่อน connect)
    private int playerIndex = -1;

    // Callback สำหรับ roomcreate UI
    private java.util.function.Consumer<String> onRoomCode;
    private java.util.function.Consumer<String[]> onRoomStatus;
    private Runnable onJoinOk;
    private java.util.function.Consumer<String> onJoinFail;
    private Runnable onGameStart;

    // ── setters ──────────────────────────────────────────────────────────
    public void setOnRoomCode(java.util.function.Consumer<String> c) {
        onRoomCode = c;
    }

    public void setOnRoomStatus(java.util.function.Consumer<String[]> c) {
        onRoomStatus = c;
    }

    public void setOnJoinOk(Runnable r) {
        onJoinOk = r;
    }

    public void setOnJoinFail(java.util.function.Consumer<String> c) {
        onJoinFail = c;
    }

    public void setOnGameStart(Runnable r) {
        onGameStart = r;
    }

    public void setGameUI(GameUI ui) {
        this.ui = ui;
    }

    // ── Connect และส่ง CREATE ─────────────────────────────────────────────
    public void connectAndCreate(String ip, String username, int roomSize) {
        if (!connect(ip)) {
            return;
        }
        send(GameMessage.createRoom(roomSize, username));
        new Thread(this::listen).start();
    }

    // ── Connect และส่ง JOIN ───────────────────────────────────────────────
    public void connectAndJoin(String ip, String username, String roomCode) {
        if (!connect(ip)) {
            return;
        }
        send(GameMessage.joinRoom(roomCode, username));
        new Thread(this::listen).start();
    }

    // ── ส่ง Move (เรียกจาก GameUI) ───────────────────────────────────────
    public void sendMove(int cardIdx, String buy, String use) {
        send(GameMessage.sendMove(cardIdx, buy, use));
    }

    // ── Internal ─────────────────────────────────────────────────────────
    private boolean connect(String ip) {
        try {
            socket = new Socket(ip, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("[Client] Connected to " + ip + ":" + PORT);
            return true;
        } catch (IOException e) {
            if (onJoinFail != null) {
                onJoinFail.accept("Can't connected: " + e.getMessage());
            }
            return false;
        }
    }

    private void listen() {
        try {
            while (true) {
                handle((GameMessage) in.readObject());
            }
        } catch (EOFException | SocketException e) {
            System.out.println("[Client] Disconnected.");
        } catch (Exception e) {
            System.err.println("[Client] Error: " + e.getMessage());
        }
    }

    private void handle(GameMessage msg) {
        switch (msg.getType()) {

            // ── Lobby phase ──────────────────────────────────────────────
            case ROOM_CODE -> {
                System.out.println("[Client] Room code: " + msg.getRoomCode());
                if (onRoomCode != null) {
                    onRoomCode.accept(msg.getRoomCode());
                }
            }

            case ROOM_STATUS -> {
                System.out.println("[Client] Room " + msg.getCurrentPlayers()
                        + "/" + msg.getMaxPlayers());
                if (onRoomStatus != null) {
                    onRoomStatus.accept(msg.getRoomUsernames());
                }
            }

            case ROOM_JOIN_OK -> {
                playerIndex = msg.getPlayerIndex();
                System.out.println("[Client] Joined room " + msg.getRoomCode());
                if (onJoinOk != null) {
                    onJoinOk.run();
                }
            }

            case ROOM_JOIN_FAIL -> {
                System.out.println("[Client] Join failed: " + msg.getReason());
                if (onJoinFail != null) {
                    onJoinFail.accept(msg.getReason());
                }
            }

            // ── Game phase ───────────────────────────────────────────────
            case GAME_START -> {
                playerIndex = msg.getPlayerIndex();
                System.out.println("[Client] Game start! I am Player " + (playerIndex + 1));
                if (onGameStart != null) {
                    onGameStart.run();
                }
            }

            case GAME_STATE -> {
                if (ui != null) {
                    ui.onGameState(msg.getTargets(), msg.getMoney(), msg.getScore(),
                            msg.getHandValues(), msg.getEffectInventory());
                }
            }

            case REQUEST_MOVE -> {
                if (ui != null) {
                    ui.onRequestMove(this);
                }
            }

            case ROUND_RESULT -> {
                if (ui != null) {
                    ui.onRoundResult(msg.getCumulativeTotal(),
                            msg.getAllScores(), msg.getRoundLog());
                }
            }

            case GAME_OVER -> {
                if (ui != null) {
                    ui.onGameOver(msg.getPlayerNames(), msg.getFinalScores());
                }
            }

            default -> {
            }
        }
    }

    public void send(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("[Client] Send error: " + e.getMessage());
        }
    }

    public int getPlayerIndex() {
        return playerIndex;
    }
}
