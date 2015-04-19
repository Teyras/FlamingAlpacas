package cz.bucharjan.FlamingAlpacas;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.bucharjan.FlamingAlpacas.Sprites.*;

public class GamePanel extends javax.swing.JPanel {
    final int fieldSize = 20;

    private Image background;

    private int width;
    private int height;

    private Player player;
    private Image image;
    private int frameCount = 0;

    private Map<Integer, Monster> monsters = new HashMap<>();
    private Map<Integer, Ally> allies = new HashMap<>();
    private Map<Sprite, MovementData> movement = new HashMap<>();
    private Map<Sprite, Image> spriteImages = new HashMap<>();

    private java.util.List<PlayerMoveAction> playerMoveListeners = new ArrayList<>();

    public GamePanel (int width, int height, Player player) {
        this.width = width;
        this.height = height;
        this.player = player;

        movement.put(player, new MovementData(player));

        setPreferredSize(new Dimension(width * fieldSize, height * fieldSize));
        paintPlayer();
    }

    public void addPlayerMoveListener (PlayerMoveAction action) {
        playerMoveListeners.add(action);
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
        image = new BufferedImage(fieldSize, fieldSize, BufferedImage.TYPE_INT_ARGB);

        Graphics g = image.getGraphics();
        g.setColor(Color.blue);
        g.fillOval(1, 1, fieldSize - 2, fieldSize - 2);

        spriteImages.put(player, image);
    }

    @Override
    protected void paintComponent (Graphics g) {
        super.paintComponent(g);

        if (background == null) {
            paintBackground();
        }

        Image foreground = new BufferedImage(width * fieldSize, height * fieldSize, BufferedImage.TYPE_INT_ARGB);
        Graphics foregroundGraphics = foreground.getGraphics();

        for (Monster monster : monsters.values()) {
            paintSprite(foregroundGraphics, monster);
        }

        for (Ally ally : allies.values()) {
            paintSprite(foregroundGraphics, ally);
        }

        paintSprite(foregroundGraphics, player);

        g.drawImage(background, 0, 0, null);
        g.drawImage(foreground, 0, 0, null);

        frameCount++;
    }

    private void paintSprite (Graphics g, Sprite sprite) {
        MovementData data = movement.get(sprite);
        Image image = spriteImages.get(sprite);

        if (data == null || image == null) {
            return;
        }

        g.drawImage(
                image,
                sprite.getPosition().getX() * fieldSize + data.getXOffset(fieldSize),
                sprite.getPosition().getY() * fieldSize + data.getYOffset(fieldSize),
                null
        );
    }

    public synchronized void moveSprites (int time) {
        MovementData playerMovement = movement.get(player);

        if (playerMovement.isMoving()) {
            if (playerMovement.addProgress(time)) {
                player.setPosition(player.getPosition().transform(playerMovement.getDirection()));

                for (PlayerMoveAction action : playerMoveListeners) {
                    action.run(playerMovement.getDirection());
                }
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

        for (Ally ally : allies.values()) {
            MovementData allyMovement = movement.get(ally);
            if (allyMovement.addProgress(time)) {
                ally.setPosition(ally.getPosition().transform(allyMovement.getDirection()));
            }
        }
    }

    public synchronized void updateSprites (Monster[] monsters, Ally[] allies) {
        for (Monster newMonster : monsters) {
            Monster monster = this.monsters.get(newMonster.getId());
            if (monster == null) {
                this.monsters.put(newMonster.getId(), newMonster);
                this.movement.put(newMonster, new MovementData(newMonster));
                this.paintMonster(newMonster);
                continue;
            }

            if (!newMonster.getPosition().equals(monster.getPosition())) {
                this.movement.get(monster).clearProgress();
            }

            monster.setDirection(newMonster.getDirection());
            monster.setPosition(newMonster.getPosition());
            this.movement.get(monster).setDirection(newMonster.getDirection());
        }

        for (Ally newAlly : allies) {
            Ally ally = this.allies.get(newAlly.getId());
            if (ally == null) {
                if (newAlly.getId() == player.getId()) {
                    continue;
                } else {
                    this.allies.put(newAlly.getId(), newAlly);
                    this.movement.put(newAlly, new MovementData(newAlly));
                    this.paintAlly(newAlly);
                    continue;
                }
            }

            if (!newAlly.getPosition().equals(ally.getPosition())) {
                this.movement.get(ally).clearProgress();
            }

            ally.setDirection(newAlly.getDirection());
            ally.setPosition(newAlly.getPosition());
            this.movement.get(ally).setDirection(newAlly.getDirection());
        }
    }

    private void paintMonster (Monster monster) {
        image = new BufferedImage(fieldSize, fieldSize, BufferedImage.TYPE_INT_ARGB);

        Graphics g = image.getGraphics();
        g.setColor(Color.red);
        g.fillOval(1, 1, fieldSize - 2, fieldSize - 2);

        spriteImages.put(monster, image);
    }

    private void paintAlly (Ally ally) {
        image = new BufferedImage(fieldSize, fieldSize, BufferedImage.TYPE_INT_ARGB);

        Graphics g = image.getGraphics();
        g.setColor(Color.green);
        g.fillOval(1, 1, fieldSize - 2, fieldSize - 2);

        spriteImages.put(ally, image);
    }

    public int resetFrameCount () {
        int value = frameCount;
        frameCount = 0;
        return value;
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

interface PlayerMoveAction {
    void run (Direction direction);
}