import java.util.*;
import java.io.*;
public abstract class Player implements Serializable{
//    private int ID;
//    private double winrate;
    protected String username;
    protected int money, score;
    protected ArrayList<Card> cardList = new ArrayList<>();
    protected ArrayList<EffectCard> efcList = new ArrayList<>();
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
    
    public String getUsername(){
        return username;
    }
    
    public void addMoney(int money){
        this.money += money;
    }

    public abstract boolean buyItem(String efc);
    public abstract Card selectMove();
    
}
