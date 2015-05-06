package cz.bucharjan.FlamingAlpacas;

import java.awt.Image;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;

import cz.bucharjan.FlamingAlpacas.Sprites.*;

public class GamePanel extends javax.swing.JPanel {
    final int fieldSize = 20;

    private Image background;

    Board board;

    private Player player;
    private Image image;
    private int frameCount = 0;

    private Map<Integer, Monster> monsters = new HashMap<>();
    private Map<Integer, Ally> allies = new HashMap<>();
    private Set<Projectile> projectiles = new HashSet<>();

    private Map<Sprite, MovementData> movement = new HashMap<>();
    private Map<Sprite, Image> spriteImages = new HashMap<>();

    private java.util.List<PlayerMoveAction> playerMoveListeners = new ArrayList<>();

    public GamePanel (Board board, Player player) {
        this.board = board;
        this.player = player;

        movement.put(player, new MovementData(player));

        setPreferredSize(new Dimension(board.getWidth() * fieldSize, board.getHeight() * fieldSize));
        paintPlayer();
    }

    public void addPlayerMoveListener (PlayerMoveAction action) {
        playerMoveListeners.add(action);
    }

    private void paintBackground () {
        background = createImage(board.getWidth() * fieldSize, board.getHeight() * fieldSize);

        if (background == null) {
            return;
        }

        Graphics g = background.getGraphics();

        g.setColor(Color.lightGray);
        g.fillRect(0, 0, board.getWidth() * fieldSize, board.getHeight() * fieldSize);

        g.setColor(Color.gray);

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                if (board.isWall(i, j)) {
                    g.fillRect(i * fieldSize, j * fieldSize, fieldSize, fieldSize);
                }
            }
        }

        g.setColor(Color.gray);

        for (int i = 0; i <= board.getWidth(); i++) {
            g.drawLine(i * fieldSize, 0, i * fieldSize, board.getHeight() * fieldSize);
        }

        for (int i = 0; i <= board.getHeight(); i++) {
            g.drawLine(0, i * fieldSize, board.getWidth() * fieldSize, i * fieldSize);
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
    protected synchronized void paintComponent (Graphics g) {
        super.paintComponent(g);

        if (background == null) {
            paintBackground();
        }

        Image foreground = new BufferedImage(board.getWidth() * fieldSize, board.getHeight() * fieldSize, BufferedImage.TYPE_INT_ARGB);
        Graphics foregroundGraphics = foreground.getGraphics();

        for (Monster monster : monsters.values()) {
            paintSprite(foregroundGraphics, monster);
        }

        for (Ally ally : allies.values()) {
            paintSprite(foregroundGraphics, ally);
        }

        for (Projectile projectile : projectiles) {
            paintSprite(foregroundGraphics, projectile);
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
            if (addSpriteProgress(player, time)) {
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
            addSpriteProgress(monster, time);
        }

        for (Ally ally : allies.values()) {
            addSpriteProgress(ally, time);
        }

        List<Projectile> stoppedProjectiles = new ArrayList<>();

        for (Projectile projectile : projectiles) {
            MovementData data = movement.get(projectile);
            addSpriteProgress(projectile, time);
            if (data.canStop() && !board.isFree(data.getTarget())) {
                stoppedProjectiles.add(projectile);
            }
        }

        for (Projectile projectile : stoppedProjectiles) {
            projectiles.remove(projectile);
        }
    }

    private boolean addSpriteProgress (Sprite sprite, int time) {
        MovementData data = movement.get(sprite);
        Coords target = data.getTarget();

        if (!board.isFree(target)) {
            return false;
        }

        if (data.isMoving() && data.addProgress(time)) {
            sprite.setPosition(target);
            return true;
        }

        return false;
    }

    public synchronized void updateSprites (Monster[] monsters, Ally[] allies, Projectile[] projectiles) {
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

        java.util.List<Monster> deadMonsters = new ArrayList<>();
        for (Monster monster : this.monsters.values()) {
            if (!Arrays.asList(monsters).contains(monster)) {
                deadMonsters.add(monster);
            }
        }

        for (Monster monster : deadMonsters) {
            this.monsters.remove(monster.getId());
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

        for (Projectile newProjectile : projectiles) {
            if (newProjectile.getOwnerId() == player.getId()) {
                continue; // Player's projectiles are handled locally
            }

            if (!this.projectiles.contains(newProjectile)) {
                this.projectiles.add(newProjectile);
                paintProjectile(newProjectile);
                movement.put(newProjectile, new MovementData(newProjectile));
            }
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

    private void paintProjectile (Projectile projectile) {
        image = new BufferedImage(fieldSize, fieldSize, BufferedImage.TYPE_INT_ARGB);

        Graphics g = image.getGraphics();
        g.setColor(Color.yellow);
        g.fillOval(fieldSize / 2, fieldSize / 4, fieldSize / 2, fieldSize / 2);

        spriteImages.put(projectile, image);
    }

    public int resetFrameCount () {
        int value = frameCount;
        frameCount = 0;
        return value;
    }

    public void addProjectile (Coords position) {
        Projectile projectile = new Projectile();
        projectile.setDirection(Direction.Right);
        projectile.setPosition(position);

        paintProjectile(projectile);
        projectiles.add(projectile);
        movement.put(projectile, new MovementData(projectile));
    }
}

class MovementData {
    private Direction direction;

    private int progress;

    private Sprite sprite;

    public MovementData (Sprite sprite) {
        this.sprite = sprite;
        this.direction = sprite.getDirection();
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

    public Coords getTarget () {
        return sprite.getPosition().transform(direction);
    }
}

interface PlayerMoveAction {
    void run (Direction direction);
}