package Main;

import java.io.Serializable;

public class GameMessage implements Serializable {

    private static final long serialVersionUID = 2L;

    public enum Type {
        // Server → Client
        WELCOME,
        ROOM_CODE, // ส่ง room code ให้ host แสดงบนหน้าจอ
        ROOM_STATUS, // บอกสถานะห้อง (x/y players + รายชื่อ)
        ROOM_JOIN_OK, // client เข้าร่วมสำเร็จ
        ROOM_JOIN_FAIL, // เข้าห้องไม่ได้ + reason
        GAME_START, // เกมเริ่ม
        GAME_STATE,
        REQUEST_MOVE,
        ROUND_RESULT,
        GAME_OVER,
        // Client → Server
        CREATE_ROOM, // ขอสร้างห้อง + จำนวน human
        JOIN_ROOM, // ส่ง room code + username เพื่อเข้า
        SEND_MOVE
    }

    private final Type type;

    // WELCOME / GAME_START
    private int playerIndex;

    // CREATE_ROOM
    private int roomSize;

    // JOIN_ROOM / ROOM_CODE / ROOM_JOIN_OK / ROOM_JOIN_FAIL
    private String roomCode;
    private String username;
    private String reason;

    // ROOM_STATUS
    private int currentPlayers;
    private int maxPlayers;
    private String[] roomUsernames;

    // GAME_STATE
    private int round;
    private int[] targets;
    private int money;
    private int score;
    private int[] handValues;
    private String[] effectInventory;

    // ROUND_RESULT
    private int cumulativeTotal;
    private int[] allScores;
    private String roundLog;

    // SEND_MOVE
    private int cardIndex;
    private String buyEffect;
    private String useEffect;

    // GAME_OVER
    private String[] playerNames;
    private int[] finalScores;

    private GameMessage(Type t) {
        this.type = t;
    }

    // ── Factories ───────────────────────────────────────────────────────
    public static GameMessage welcome(int idx) {
        GameMessage m = new GameMessage(Type.WELCOME);
        m.playerIndex = idx;
        return m;
    }

    public static GameMessage roomCode(String code, int size) {
        GameMessage m = new GameMessage(Type.ROOM_CODE);
        m.roomCode = code;
        m.maxPlayers = size;
        return m;
    }

    public static GameMessage roomStatus(int cur, int max, String[] names) {
        GameMessage m = new GameMessage(Type.ROOM_STATUS);
        m.currentPlayers = cur;
        m.maxPlayers = max;
        m.roomUsernames = names;
        return m;
    }

    public static GameMessage roomJoinOk(String code, int idx) {
        GameMessage m = new GameMessage(Type.ROOM_JOIN_OK);
        m.roomCode = code;
        m.playerIndex = idx;
        return m;
    }

    public static GameMessage roomJoinFail(String reason) {
        GameMessage m = new GameMessage(Type.ROOM_JOIN_FAIL);
        m.reason = reason;
        return m;
    }

    public static GameMessage gameStart(int idx) {
        GameMessage m = new GameMessage(Type.GAME_START);
        m.playerIndex = idx;
        return m;
    }

    public static GameMessage gameState(int round, int[] targets, int money,
            int score, int[] hand, String[] inv) {
        GameMessage m = new GameMessage(Type.GAME_STATE);
        m.round = round;
        m.targets = targets;
        m.money = money;
        m.score = score;
        m.handValues = hand;
        m.effectInventory = inv;
        return m;
    }

    public static GameMessage requestMove(int round) {
        GameMessage m = new GameMessage(Type.REQUEST_MOVE);
        m.round = round;
        return m;
    }

    public static GameMessage roundResult(int total, int[] scores, String log) {
        GameMessage m = new GameMessage(Type.ROUND_RESULT);
        m.cumulativeTotal = total;
        m.allScores = scores;
        m.roundLog = log;
        return m;
    }

    public static GameMessage gameOver(String[] names, int[] scores) {
        GameMessage m = new GameMessage(Type.GAME_OVER);
        m.playerNames = names;
        m.finalScores = scores;
        return m;
    }

    public static GameMessage createRoom(int size, String username) {
        GameMessage m = new GameMessage(Type.CREATE_ROOM);
        m.roomSize = size;
        m.username = username;
        return m;
    }

    public static GameMessage joinRoom(String code, String username) {
        GameMessage m = new GameMessage(Type.JOIN_ROOM);
        m.roomCode = code;
        m.username = username;
        return m;
    }

    public static GameMessage sendMove(int cardIdx, String buy, String use) {
        GameMessage m = new GameMessage(Type.SEND_MOVE);
        m.cardIndex = cardIdx;
        m.buyEffect = buy;
        m.useEffect = use;
        return m;
    }

    // ── Getters ──────────────────────────────────────────────────────────
    public Type getType() {
        return type;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public int getRoomSize() {
        return roomSize;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getUsername() {
        return username;
    }

    public String getReason() {
        return reason;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String[] getRoomUsernames() {
        return roomUsernames;
    }

    public int getRound() {
        return round;
    }

    public int[] getTargets() {
        return targets;
    }

    public int getMoney() {
        return money;
    }

    public int getScore() {
        return score;
    }

    public int[] getHandValues() {
        return handValues;
    }

    public String[] getEffectInventory() {
        return effectInventory;
    }

    public int getCumulativeTotal() {
        return cumulativeTotal;
    }

    public int[] getAllScores() {
        return allScores;
    }

    public String getRoundLog() {
        return roundLog;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    public String getBuyEffect() {
        return buyEffect;
    }

    public String getUseEffect() {
        return useEffect;
    }

    public String[] getPlayerNames() {
        return playerNames;
    }

    public int[] getFinalScores() {
        return finalScores;
    }
}
