import java.util.*;
public class Human extends Player{
    Scanner choose = new Scanner(System.in);
    public Human(){
        for(int i = 1;i <= 5; i++){
            cardList.add(new Card(i));
        }
    }
    
    @Override
    public boolean buyItem(EffectCard c){
        return true;
    }
    
    @Override
    public Card selectMove(){
        System.out.print("Choose your card. (1 - " + cardList.size() +"): ");
        int index = choose.nextInt() - 1;
        choose.nextLine();
        System.out.print("Choose your effect. (None, Negative, Zero): ");
        String effect = choose.nextLine().trim().toLowerCase();
        Card using;
        if(effect.equals("negative")){
            using = cardList.remove(index);
            using.setType(new Negative());
            System.out.println("Using negative.");
        }
        else if(effect.equals("zero")){
            using = cardList.remove(index);
            using.setType(new Zero());
            System.out.println("Using zero.");
        }
        else{
            using = cardList.remove(index);
        }
        System.out.println((using.getValue()) + " Chosen, removed from the hand");
        System.out.println("");
        return using;
    }
}
