package cz.bucharjan.FlamingAlpacas.Sprites;

import cz.bucharjan.FlamingAlpacas.Coords;
import cz.bucharjan.FlamingAlpacas.Direction;

import java.io.Serializable;

/**
 * Created by teyras on 18.3.15.
 */
public abstract class Sprite implements Serializable {
    private Coords position = new Coords(0, 0);

    private Direction direction = Direction.None;

    private int id;

    public Sprite () {

    }

    public Sprite (Sprite sprite) {
        this.id = sprite.getId();
        this.position = sprite.getPosition();
        this.direction = sprite.getDirection();
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

    public void transformPosition (Direction direction) {
        this.position = this.position.transform(direction);
    }

    public Direction getDirection () {
        return direction;
    }

    public void setDirection (Direction direction) {
        this.direction = direction;
    }

    public int getTimePerSquare () {
        return 200;
    }
}
