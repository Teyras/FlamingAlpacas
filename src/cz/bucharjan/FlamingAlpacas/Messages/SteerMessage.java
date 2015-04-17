package cz.bucharjan.FlamingAlpacas.Messages;

import cz.bucharjan.FlamingAlpacas.Direction;

/**
 * Created by bucharj3 on 17.4.15.
 */
public class SteerMessage extends Message {
    private Direction direction;

    public SteerMessage (Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection () {
        return direction;
    }
}
