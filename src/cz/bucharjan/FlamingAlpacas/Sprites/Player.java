package cz.bucharjan.FlamingAlpacas.Sprites;

import cz.bucharjan.FlamingAlpacas.Coords;
import cz.bucharjan.FlamingAlpacas.Direction;

import java.util.Stack;

/**
 * Created by teyras on 18.3.15.
 */


public class Player extends Sprite {
    private Stack<Direction> steering = new Stack<>();

    public Player(Sprite sprite) {
        super(sprite);
    }

    public void steer (Direction direction) {
        if (!steering.contains(direction)) {
            steering.push(direction);
        }
    }

    public void unsteer (Direction direction) {
        steering.remove(direction);
    }

    public Direction getDirection () {
        if (steering.empty()) {
            return Direction.None;
        }

        return steering.peek();
    }
}
