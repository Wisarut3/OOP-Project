import java.util.*;
public class Human extends Player{
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
        Scanner choose = new Scanner(System.in);
        System.out.print("Choose your card. (1 - " + cardList.size() +"): ");
        int index = choose.nextInt() - 1;
        System.out.println((cardList.get(index).getValue()) + " Chosen, removed from the hand");
        return cardList.remove(index);
    }
}
