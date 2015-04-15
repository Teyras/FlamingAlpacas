package cz.bucharjan.FlamingAlpacas.Sprites;

/**
 * Created by teyras on 18.3.15.
 */
public class Monster extends Sprite {
    public Monster (int id) {
        super(id);
    }

    public Monster (Monster monster) {
        super(monster);
    }

    public int getTimePerSquare () {
        return 1500;
    }
}
