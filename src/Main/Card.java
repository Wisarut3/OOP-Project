package Main;

import java.io.*;

public class Card implements Serializable {

    private final int value;
    private EffectCard type;

    public Card(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }

    public EffectCard getType() {
        return type;
    }

    public void setType(EffectCard efc) {
        this.type = efc;
    }
}
