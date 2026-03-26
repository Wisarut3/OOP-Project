package Main;

public class Negative implements EffectCard {

    @Override
    public void applyEffect(GameEngine engine, Player p, Card c) {
        engine.addTotal(c.getValue() * -1);
    }
}
