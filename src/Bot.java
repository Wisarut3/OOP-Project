public class Bot extends Player {
    public Bot(){
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
        return cardList.remove(0);
    }
}
