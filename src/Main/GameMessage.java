package Main;

import java.io.Serializable;

public class GameMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        // Server -> Client
        WELCOME, // ส่ง playerIndex ให้ client รู้ว่าตัวเองเป็น Player ที่เท่าไหร่
        GAME_STATE, // broadcast สถานะเกม (round, targets, money, scores, hand)
        REQUEST_MOVE, // ขอให้ Human player เลือก card + effect
        ROUND_RESULT, // ผลของ round (total, คะแนนที่ได้)
        GAME_OVER, // เกมจบ + final scores

        // Client -> Server
        SEND_MOVE           // Human ส่งการเลือกกลับมา
    }

    private final Type type;
    private int playerIndex;

    // GAME_STATE
    private int round;
    private int[] targets;      // targets ของ player คนนี้
    private int money;
    private int score;
    private int[] handValues;   // ค่าไพ่ที่มีอยู่ใน hand
    private String[] effectInventory; // effects ที่มีใน inventory

    // REQUEST_MOVE 
    // ใช้ round field ด้านบน
    // ROUND_RESULT
    private int cumulativeTotal;
    private int[] allScores;     // scores ของทุก player ในรอบนี้
    private String roundLog;     // log ข้อความผลรอบ

    // SEND_MOVE (Client -> Server)
    private int cardIndex;       // index ของไพ่ใน hand ที่เลือก
    private String buyEffect;    // "None" | "Negative" | "Zero"  (ซื้อก่อนเล่น)
    private String useEffect;    // "None" | "Negative" | "Zero"  (ใช้กับไพ่)

    // GAME_OVER
    private String[] playerNames;
    private int[] finalScores;

    private GameMessage(Type type) {
        this.type = type;
    }

    public static GameMessage welcome(int playerIndex) {
        GameMessage m = new GameMessage(Type.WELCOME);
        m.playerIndex = playerIndex;
        return m;
    }

    public static GameMessage gameState(int round, int[] targets, int money,
            int score, int[] handValues, String[] effectInventory) {
        GameMessage m = new GameMessage(Type.GAME_STATE);
        m.round = round;
        m.targets = targets;
        m.money = money;
        m.score = score;
        m.handValues = handValues;
        m.effectInventory = effectInventory;
        return m;
    }

    public static GameMessage requestMove(int round) {
        GameMessage m = new GameMessage(Type.REQUEST_MOVE);
        m.round = round;
        return m;
    }

    public static GameMessage roundResult(int cumulativeTotal, int[] allScores, String log) {
        GameMessage m = new GameMessage(Type.ROUND_RESULT);
        m.cumulativeTotal = cumulativeTotal;
        m.allScores = allScores;
        m.roundLog = log;
        return m;
    }

    public static GameMessage gameOver(String[] playerNames, int[] finalScores) {
        GameMessage m = new GameMessage(Type.GAME_OVER);
        m.playerNames = playerNames;
        m.finalScores = finalScores;
        return m;
    }

    public static GameMessage sendMove(int cardIndex, String buyEffect, String useEffect) {
        GameMessage m = new GameMessage(Type.SEND_MOVE);
        m.cardIndex = cardIndex;
        m.buyEffect = buyEffect;
        m.useEffect = useEffect;
        return m;
    }

    public Type getType() {
        return type;
    }

    public int getPlayerIndex() {
        return playerIndex;
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
