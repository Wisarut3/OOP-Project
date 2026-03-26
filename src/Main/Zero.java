package Main;

public class Zero implements EffectCard {

    @Override
    public void applyEffect(GameEngine engine, Player p, Card c) {
        engine.setZero();
    }
}
