import java.util.*;
import java.io.*;
public abstract class Player implements Serializable{
//    private int ID;
//    private double winrate;
    private String username;
    protected int money, score;
    protected ArrayList<Card> cardList = new ArrayList<>();
    protected int[] target = new int[2];
    protected int diff;
    
    public void addScore(int i){
        score += i;
    }

    public int[] getTarget(){
        return target;
    }
    
    public int getDiff(){
        return diff;
    }
    
    public int getScore(){
        return score;
    }
    
    public void setTarget(int index, int target){
        this.target[index] = target;
    }

    public abstract boolean buyItem(EffectCard c);
    public abstract Card selectMove();
    
}
