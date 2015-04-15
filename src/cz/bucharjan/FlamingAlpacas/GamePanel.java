package cz.bucharjan.FlamingAlpacas;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import cz.bucharjan.FlamingAlpacas.Sprites.*;

public class GamePanel extends javax.swing.JPanel {
    final int fieldSize = 20;

    private Image background;

    private int width;
    private int height;

    private Player player;
    private Image playerImage;

    private Map<Integer, Monster> monsters = new HashMap<>();
    private Map<Sprite, MovementData> movement = new HashMap<>();

    public GamePanel (int width, int height, Player player) {
        this.width = width;
        this.height = height;
        this.player = player;

        movement.put(player, new MovementData(player));

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

        paintMonsters(g);

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

    private void paintMonsters (Graphics g) {
        g.setColor(Color.red);

        for (Monster monster : monsters.values()) {
            MovementData data = movement.get(monster);

            g.fillOval(
                    monster.getPosition().getX() * fieldSize + 1 + data.getXOffset(fieldSize),
                    monster.getPosition().getY() * fieldSize + 1 + data.getYOffset(fieldSize),
                    fieldSize - 2,
                    fieldSize - 2
            );
        }
    }

    public synchronized void moveSprites (int time) {
        MovementData playerMovement = movement.get(player);

        if (playerMovement.isMoving()) {
            if (playerMovement.addProgress(time)) {
                player.setPosition(player.getPosition().transform(playerMovement.getDirection()));
            }

            if (playerMovement.canStop()) {
                playerMovement.setDirection(player.getDirection());
            }
        } else {
            playerMovement.setDirection(player.getDirection());
        }

        for (Monster monster : monsters.values()) {
            MovementData monsterMovement = movement.get(monster);
            if (monsterMovement.addProgress(time)) {
                monster.setPosition(monster.getPosition().transform(monsterMovement.getDirection()));
            }
        }
    }

    public synchronized void updateSprites (Monster[] monsters) {
        for (Monster newMonster : monsters) {
            Monster monster = this.monsters.get(newMonster.getId());
            if (monster == null) {
                this.monsters.put(newMonster.getId(), newMonster);
                this.movement.put(newMonster, new MovementData(newMonster));
                continue;
            }

            if (!newMonster.getPosition().equals(monster.getPosition())) {
                this.movement.get(monster).clearProgress();
            }

            monster.setDirection(newMonster.getDirection());
            monster.setPosition(newMonster.getPosition());
            this.movement.get(monster).setDirection(newMonster.getDirection());
        }
    }
}

class MovementData {
    private Direction direction;

    private int progress;

    private Sprite sprite;

    public MovementData (Sprite sprite) {
        this.sprite = sprite;
    }

    public void setDirection (Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection () {
        return direction;
    }

    public boolean addProgress (int steps) {
        progress += steps;

        if (progress >= sprite.getTimePerSquare()) {
            progress = 0;
            return true;
        }

        return false;
    }

    public boolean canStop () {
        return progress == 0;
    }

    public boolean isMoving () {
        return direction != Direction.None || progress > 0;
    }

    public int getXOffset (int fieldSize) {
        if (direction == Direction.Left) {
            return (-progress * fieldSize) / sprite.getTimePerSquare();
        }

        if (direction == Direction.Right) {
            return (progress * fieldSize) / sprite.getTimePerSquare();
        }

        return 0;
    }

    public int getYOffset (int fieldSize) {
        if (direction == Direction.Up) {
            return (-progress * fieldSize) / sprite.getTimePerSquare();
        }

        if (direction == Direction.Down) {
            return (progress * fieldSize) / sprite.getTimePerSquare();
        }

        return 0;
    }

    public void clearProgress () {
        this.progress = 0;
    }
}