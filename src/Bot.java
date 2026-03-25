import java.util.Random;
public class Bot extends Player {
    private int effectUsed = 0;
    private final int MAX_EFFECT = 2;

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
        Random rand = new Random();
        int index = rand.nextInt(cardList.size());
        String effect;
        if(effectUsed < MAX_EFFECT){
            String[] effects = {"none", "negative", "zero"};
            effect = effects[rand.nextInt(3)];
        } else {
            effect = "none";
        }
        Card using ;
        if(effect.equals("negative")){
            using = cardList.remove(index);
            using.setType(new Negative());
            effectUsed++;
        }
        else if(effect.equals("zero")){
            using = cardList.remove(index);
            using.setType(new Zero());
            effectUsed++;
        }
        else{
            using = cardList.remove(index);
        }
        return using;
   }
} 
