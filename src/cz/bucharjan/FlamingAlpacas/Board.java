package cz.bucharjan.FlamingAlpacas;

import java.io.Serializable;

/**
 * Created by teyras on 19.4.15.
 */
public class Board implements Serializable {
    private int width;
    private int height;
    boolean[][] walls;

    public Board (int width, int height, boolean[][] walls) {
        this.width = width;
        this.height = height;
        this.walls = walls;
    }

    public int getWidth () {
        return width;
    }

    public int getHeight () {
        return height;
    }

    protected boolean isValid (int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isWall (int x, int y) {
        return isValid(x, y) && walls[x][y];
    }

    public boolean isWall (Coords coords) {
        return isWall(coords.getX(), coords.getY());
    }

    public boolean isFree (int x, int y) {
        return isValid(x, y) && !isWall(x, y);
    }

    public boolean isFree (Coords coords) {
        return isFree(coords.getX(), coords.getY());
    }
}
