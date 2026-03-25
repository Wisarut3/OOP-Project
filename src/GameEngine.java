import java.util.*;
public class GameEngine implements Runnable{
    private final ArrayList<Player> playerList;
    private int cumulativeTotal;
    private boolean zero;
    
    public GameEngine(){
        playerList = new ArrayList<>();
        playerList.add(new Human());
        playerList.add(new Human());
        playerList.add(new Human());
        
        zero = false;
        Random rand = new Random();
        for(Player player: playerList){
            player.setTarget(0, rand.nextInt(0, 11));
            player.setTarget(1, rand.nextInt(10, 21));   
        }
        for(int i = 0; i < playerList.size(); i++){
            System.out.print("Target of Player " + (i + 1) + " = " + playerList.get(i).getTarget()[0] + " and " + playerList.get(i).getTarget()[1] + "\n");
        }
    }
    
    public void setZero(){
        cumulativeTotal = 0;
        zero = true;
    }
    
    public void addTotal(int score){
        if(zero == false){
            cumulativeTotal += score;
        }
    }
    
    public void calculateEffect(Player p, Card c, EffectCard efc){
        if(efc == null){
            addTotal(c.getValue());
            return;
        }
        efc.applyEffect(this, p, c);
    }
    
    public void addScore(ArrayList<Player> p){
        p.get(0).addScore(2);
        System.out.println("Player " + (playerList.indexOf(p.get(0)) + 1) + " got 2 points");
        p.get(1).addScore(1);
        System.out.println("Player " + (playerList.indexOf(p.get(1)) + 1)+ " got 1 points");
        System.out.println("");
    }
    
    public void calculateScore(){
        for(Player p: playerList){
            //Find mininum difference from two targets and set to player's diif.
            p.diff = Math.min(Math.abs(cumulativeTotal - p.getTarget()[0]), Math.abs(cumulativeTotal - p.getTarget()[1]));
        }
        ArrayList<Player> scoringList = new ArrayList<>(playerList);
        //Sort player by least diff
        scoringList.sort(Comparator.comparing(Player::getDiff));
        addScore(scoringList);
    }
    
    public void calculateRound(){
        for(Player p: playerList){
            Card calculating = p.selectMove();
            calculateEffect(p, calculating, calculating.getType());
            p.addMoney(5);
        }
        zero = false;
        System.out.println("Total points on the table = " + cumulativeTotal);
        calculateScore();
    }
    
    @Override
    public void run(){
        for(int i = 0; i < 5; i++){
            calculateRound();
        }
        System.out.println("");
        System.out.println("Total score:");
        for(int i = 0; i < playerList.size(); i++){
            System.out.println("Player " + (i + 1) + " got " + playerList.get(i).getScore() + " points");
        }
    }
    
    public static void main(String[] args) {
        Thread t = new Thread(new GameEngine());
        t.start();
    }
}
