package cz.bucharjan.FlamingAlpacas.Sprites;

/**
 * Created by teyras on 18.3.15.
 */
public class Player extends Sprite {
    public Player () {
    }

    public Player (int id) {
        super(id);
    }

    public Player (Player ally) {
        super(ally);
    }
}
