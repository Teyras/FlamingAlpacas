package cz.bucharjan.FlamingAlpacas.Sprites;

/**
 * Created by teyras on 5.5.15.
 */
public class Projectile extends Sprite {
    private int timePerSquare = 50;
    private Player owner;

    public Projectile () {
        super();
    }

    public Projectile (int id, Player owner) {
        super(id);
        this.owner = owner;
    }

    public Projectile (Projectile projectile) {
        super(projectile);
        this.timePerSquare = projectile.getTimePerSquare();
        this.owner = projectile.getOwner();
    }

    public int getTimePerSquare () {
        return timePerSquare;
    }

    public void setTimePerSquare (int timePerSquare) {
        this.timePerSquare = timePerSquare;
    }

    public Player getOwner () {
        return owner;
    }
}
