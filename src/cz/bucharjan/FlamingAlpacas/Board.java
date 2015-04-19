package cz.bucharjan.FlamingAlpacas;

/**
 * Created by teyras on 19.4.15.
 */
public class Board {
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

    public boolean isWall (int x, int y) {
        return walls[x][y];
    }

    public boolean isWall (Coords coords) {
        return isWall(coords.getX(), coords.getY());
    }
}
