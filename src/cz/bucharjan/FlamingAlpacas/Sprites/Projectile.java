package cz.bucharjan.FlamingAlpacas.Sprites;

/**
 * Created by teyras on 5.5.15.
 */
public class Projectile extends Sprite {
    private int timePerSquare = 50;
    private int ownerId;

    public Projectile () {
        super();
    }

    public Projectile (int id, int ownerId) {
        super(id);
        this.ownerId = ownerId;
    }

    public Projectile (Projectile projectile) {
        super(projectile);
        this.timePerSquare = projectile.getTimePerSquare();
        this.ownerId = projectile.getOwnerId();
    }

    public int getTimePerSquare () {
        return timePerSquare;
    }

    public void setTimePerSquare (int timePerSquare) {
        this.timePerSquare = timePerSquare;
    }

    public int getOwnerId () {
        return ownerId;
    }
}
