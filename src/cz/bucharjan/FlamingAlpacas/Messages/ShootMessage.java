package cz.bucharjan.FlamingAlpacas.Messages;

import cz.bucharjan.FlamingAlpacas.Coords;

/**
 * Created by teyras on 4.5.15.
 */
public class ShootMessage extends Message {
    private Coords origin;

    public ShootMessage (Coords origin) {
        this.origin = origin;
    }

    public Coords getOrigin () {
        return origin;
    }
}
