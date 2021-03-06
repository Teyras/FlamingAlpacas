package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Sprites.Player;
import cz.bucharjan.FlamingAlpacas.Sprites.Monster;
import cz.bucharjan.FlamingAlpacas.Sprites.Projectile;
import cz.bucharjan.FlamingAlpacas.Sprites.Sprite;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by teyras on 19.4.15.
 */
public class GameController {
    private Board board;
    private final List<Monster> monsters = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();

    private int nextSpriteId = 0;
    private boolean finished = false;
    private Map<Player, Integer> score = new HashMap<>();

    public GameController () {
        int width = 50;
        int height = 30;
        boolean[][] walls = new boolean[width][height];

        Random rand = new Random();

        for (int i = 1; i < width - 1; i += 2) {
            boolean passable = false;

            for (int j = 0; j < height; j++) {
                if (rand.nextDouble() < 0.65) {
                    walls[i][j] = true;
                } else {
                    passable = true;
                }
            }

            if (!passable) {
                walls[i][rand.nextInt(height)] = true;
            }
        }

        this.board = new Board(width, height, walls);
    }

    public Board getBoard () {
        return board;
    }

    public boolean isFinished () {
        return finished;
    }

    public void run () {
        finished = false;
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        Random rand = new Random();

        executor.scheduleAtFixedRate(() -> {
            spawnMonsters(board.getHeight() - 2);
        }, 0, 10, TimeUnit.SECONDS);

        final int period = 100;
        final Map<Sprite, Integer> monstersRemaining = new HashMap<>();

        executor.scheduleAtFixedRate(() -> {
            synchronized (monsters) {
                for (Monster monster : monsters) {
                    monstersRemaining.putIfAbsent(monster, monster.getTimePerSquare());
                }

                for (Map.Entry<Sprite, Integer> entry : monstersRemaining.entrySet()) {
                    Integer newTime = entry.getValue() - period;
                    Sprite sprite = entry.getKey();

                    while (newTime <= 0) {
                        newTime += sprite.getTimePerSquare();
                        sprite.setPosition(sprite.getPosition().transform(sprite.getDirection()));

                        if (sprite.getPosition().getX() == -1) {
                            executor.shutdown();
                            finished = true;
                        } else if (sprite.getPosition().getX() == 0 || board.isFree(sprite.getPosition().transform(Direction.Left))) {
                            sprite.setDirection(Direction.Left);
                        } else if (!board.isFree(sprite.getPosition().transform(sprite.getDirection()))) {
                            boolean freeUp = board.isFree(sprite.getPosition().transform(Direction.Up));
                            boolean freeDown = board.isFree(sprite.getPosition().transform(Direction.Down));

                            if (freeDown && freeUp) {
                                if (rand.nextInt(2) == 0) {
                                    sprite.setDirection(Direction.Down);
                                } else {
                                    sprite.setDirection(Direction.Up);
                                }
                            } else if (freeDown) {
                                sprite.setDirection(Direction.Down);
                            } else if (freeUp) {
                                sprite.setDirection(Direction.Up);
                            } else {
                                sprite.setDirection(Direction.None);
                            }
                        }
                    }

                    monstersRemaining.put(sprite, newTime);
                }
            }
        }, 0, period, TimeUnit.MILLISECONDS);

        final int projectilePeriod = 50;
        final Map<Projectile, Integer> projectilesRemaining = new HashMap<>();

        executor.scheduleAtFixedRate(() -> {
            synchronized (monsters) {
                for (Projectile projectile : projectiles) {
                    projectilesRemaining.putIfAbsent(projectile, projectile.getTimePerSquare());
                }

                List<Projectile> stoppedProjectiles = new ArrayList<>();
                List<Monster> deadMonsters = new ArrayList<>();

                for (Map.Entry<Projectile, Integer> entry : projectilesRemaining.entrySet()) {
                    Integer newTime = entry.getValue() - projectilePeriod;
                    Projectile projectile = entry.getKey();

                    while (newTime <= 0) {
                        newTime += projectile.getTimePerSquare();
                        projectile.setPosition(projectile.getPosition().transform(projectile.getDirection()));

                        for (Monster monster : monsters) {
                            if (monster.getPosition().equals(projectile.getPosition())) {
                                deadMonsters.add(monster);
                                Player owner = projectile.getOwner();
                                score.put(owner, score.getOrDefault(owner, 0) + 1);
                                stoppedProjectiles.add(projectile);
                                break;
                            }
                        }

                        if (!board.isFree(projectile.getPosition().transform(projectile.getDirection()))) {
                            stoppedProjectiles.add(projectile);
                            break;
                        }
                    }

                    projectilesRemaining.put(projectile, newTime);
                }

                for (Projectile projectile : stoppedProjectiles) {
                    projectiles.remove(projectile);
                    projectilesRemaining.remove(projectile);
                }

                for (Monster monster : deadMonsters) {
                    monsters.remove(monster);
                    monstersRemaining.remove(monster);
                }
            }
        }, 0, projectilePeriod, TimeUnit.MILLISECONDS);
    }

    private void placeSprite (Sprite sprite, List<? extends Sprite> others, int col) {
        int[] histogram = new int[board.getHeight()];

        for (Sprite other : others) {
            Coords position = other.getPosition();

            if (position != null) {
                histogram[position.getY()] += 1;
            }
        }

        int min = others.size();
        int index = 0;

        for (int i = 0; i < histogram.length; i++) {
            if (histogram[i] < min && board.isFree(col, i)) {
                min = histogram[i];
                index = i;
            }
        }

        sprite.setPosition(new Coords(col, index));
    }

    public void spawnMonsters (int count) {
        synchronized (monsters) {
            for (int i = 0; i < count; i++) {
                Monster monster = new Monster(getSpriteId());
                monsters.add(monster);
                placeSprite(monster, monsters, board.getWidth() - 1);
                monster.setDirection(Direction.Left);
            }
        }
    }

    public Player spawnPlayer (String nickname) {
        Player sprite = new Player(getSpriteId(), nickname);
        players.add(sprite);
        score.put(sprite, 0);
        placeSprite(sprite, players, 0);
        return sprite;
    }

    public Player[] getPlayersCopy () {
        Player[] playersArray = new Player[players.size()];
        int i = 0;

        for (Player player : players) {
            playersArray[i++] = new Player(player);
        }

        return playersArray;
    }

    public Monster[] getMonstersCopy () {
        synchronized (monsters) {
            Monster[] monstersArray = new Monster[monsters.size()];
            int i = 0;

            for (Monster monster : monsters) {
                monstersArray[i++] = new Monster(monster);
            }

            return monstersArray;
        }
    }

    public void startShot (Player player, Coords origin) {
        if (!board.isFree(origin.transform(Direction.Right))) {
            return;
        }

        Projectile projectile = new Projectile(getSpriteId(), player);
        projectile.setDirection(Direction.Right);
        projectile.setPosition(origin);
        projectiles.add(projectile);
    }

    protected int getSpriteId () {
        return nextSpriteId++;
    }

    public Projectile[] getProjectilesCopy () {
        Projectile[] projectilesArray = new Projectile[projectiles.size()];
        int i = 0;

        for (Projectile projectile : projectiles) {
            projectilesArray[i++] = new Projectile(projectile);
        }

        return projectilesArray;
    }

    public ScoreEntry[] getScore () {
        ScoreEntry[] result = new ScoreEntry[score.size()];
        int i = 0;

        for (Map.Entry<Player, Integer> entry : score.entrySet()) {
            result[i] = new ScoreEntry();
            result[i].id = entry.getKey().getId();
            result[i].nickname = entry.getKey().getNickname();
            result[i].score = entry.getValue();
            i++;
        }

        return result;
    }
}

class ScoreEntry implements Serializable {
    public int id;
    public String nickname;
    public int score;
}