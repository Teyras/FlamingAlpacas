package cz.bucharjan.FlamingAlpacas.Sprites;

import cz.bucharjan.FlamingAlpacas.Coords;
import cz.bucharjan.FlamingAlpacas.Direction;

/**
 * Created by teyras on 18.3.15.
 */
public abstract class Sprite {
    private Coords position = new Coords(0, 0);

    private Direction direction = Direction.None;

    private int id;

    private int timePerSquare = 200;

    public Sprite () {

    }

    public Sprite (int id) {
        this.id = id;
    }

    public int getId () {
        return id;
    }

    public Coords getPosition () {
        return position;
    }

    public void setPosition (Coords position) {
        this.position = position;
    }

    public Direction getDirection () {
        return direction;
    }

    public void setDirection (Direction direction) {
        this.direction = direction;
    }

    public int getTimePerSquare () {
        return timePerSquare;
    }
}
