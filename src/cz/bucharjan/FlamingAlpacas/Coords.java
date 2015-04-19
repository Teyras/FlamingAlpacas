package cz.bucharjan.FlamingAlpacas;

import java.io.Serializable;

/**
 * Created by teyras on 18.3.15.
 */
public class Coords implements Serializable {
    private int x;
    private int y;

    public Coords (int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX () {
        return x;
    }

    public int getY () {
        return y;
    }

    public Coords transform (Direction direction) {
        if (direction == null) {
            return new Coords(x, y);
        }

        switch (direction) {
            case Up:
                return new Coords(x, y - 1);
            case Down:
                return new Coords(x, y + 1);
            case Left:
                return new Coords(x - 1, y);
            case Right:
                return new Coords(x + 1, y);
        }

        return new Coords(x, y);
    }

    public boolean equals (Object object) {
        return
                object != null &&
                object instanceof Coords &&
                x == ((Coords) object).getX() &&
                y == ((Coords) object).getY();
    }
}
