package cz.bucharjan.FlamingAlpacas;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.bucharjan.FlamingAlpacas.Sprites.*;

public class GamePanel extends javax.swing.JPanel {
    final int fieldSize = 20;

    private Image background;
    private Image monsters;

    private int width;
    private int height;

    private Player player;
    private Image playerImage;

    private Map<Sprite, MovementData> movement = new HashMap<>();

    public GamePanel (int width, int height, Player player) {
        this.width = width;
        this.height = height;
        this.player = player;

        movement.put(player, new MovementData());

        setPreferredSize(new Dimension(width * fieldSize, height * fieldSize));
        paintPlayer();
    }

    private void paintBackground () {
        background = createImage(width * fieldSize, height * fieldSize);

        if (background == null) {
            return;
        }

        Graphics g = background.getGraphics();

        g.setColor(Color.lightGray);
        g.fillRect(0, 0, width * fieldSize, height * fieldSize);

        g.setColor(Color.gray);

        for (int i = 0; i <= width; i++) {
            g.drawLine(i * fieldSize, 0, i * fieldSize, height * fieldSize);
        }

        for (int i = 0; i <= height; i++) {
            g.drawLine(0, i * fieldSize, width * fieldSize, i * fieldSize);
        }
    }

    private void paintPlayer () {
        playerImage = new BufferedImage(fieldSize, fieldSize, BufferedImage.TYPE_INT_ARGB);

        Graphics g = playerImage.getGraphics();
        g.setColor(Color.blue);
        g.fillOval(1, 1, fieldSize - 2, fieldSize - 2);
    }

    @Override
    protected void paintComponent (Graphics g) {
        super.paintComponent(g);

        if (background == null) {
            paintBackground();
        }

        g.drawImage(background, 0, 0, null);

        if (monsters != null) {
            g.drawImage(monsters, 0, 0, null);
        }

        int x = player.getPosition().getX();
        int y = player.getPosition().getY();

        MovementData playerMovement = movement.get(player);

        g.drawImage(
                playerImage,
                x * fieldSize + playerMovement.getXOffset(fieldSize),
                y * fieldSize + playerMovement.getYOffset(fieldSize),
                null
        );
    }

    public void paintMonsters (Coords[] monsters) {
        this.monsters = new BufferedImage(width * fieldSize, height * fieldSize, BufferedImage.TYPE_INT_ARGB);

        Graphics g = this.monsters.getGraphics();
        g.setColor(Color.red);

        for (Coords monster : monsters) {
            g.fillOval(
                    monster.getX() * fieldSize + 1,
                    monster.getY() * fieldSize + 1,
                    fieldSize - 2,
                    fieldSize - 2
            );
        }
    }

    public void moveSprites () {
        MovementData playerMovement = movement.get(player);

        if (playerMovement.isMoving()) {
            if (playerMovement.addProgress(10)) {
                player.setPosition(player.getPosition().transform(playerMovement.getDirection()));
            }

            if (playerMovement.canStop()) {
                playerMovement.setDirection(player.getDirection());
            }
        } else {
            playerMovement.setDirection(player.getDirection());
        }
    }
}

class MovementData {
    private Direction direction;

    private int progress;

    private boolean stopped = true;

    public void setDirection (Direction direction) {
        this.direction = direction;
        stopped = direction == Direction.None;
    }

    public Direction getDirection () {
        return direction;
    }

    public boolean addProgress (int steps) {
        progress += steps;

        if (progress >= 100) {
            progress = 0;
            return true;
        }

        return false;
    }

    public boolean canStop () {
        return progress == 0;
    }

    public boolean isMoving () {
        return !stopped || progress > 0;
    }

    public int getXOffset (int fieldSize) {
        if (direction == Direction.Left) {
            return (-progress * fieldSize) / 100;
        }

        if (direction == Direction.Right) {
            return (progress * fieldSize) / 100;
        }

        return 0;
    }

    public int getYOffset (int fieldSize) {
        if (direction == Direction.Up) {
            return (-progress * fieldSize) / 100;
        }

        if (direction == Direction.Down) {
            return (progress * fieldSize) / 100;
        }

        return 0;
    }
}