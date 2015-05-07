package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Sprites.Ally;
import cz.bucharjan.FlamingAlpacas.Sprites.Monster;
import cz.bucharjan.FlamingAlpacas.Sprites.Projectile;
import cz.bucharjan.FlamingAlpacas.Sprites.Sprite;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by teyras on 19.4.15.
 */
public class GameController {
    private Board board;
    private final List<Monster> monsters;
    private final List<Ally> players;
    private final List<Projectile> projectiles = new ArrayList<>();

    private int nextSpriteId = 0;

    public GameController (Board board, List<Monster> monsters, List<Ally> players) {
        this.board = board;
        this.monsters = monsters;
        this.players = players;
    }

    public void run () {
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

                        if (board.isFree(sprite.getPosition().transform(Direction.Left))) {
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

    public Ally spawnPlayer () {
        Ally sprite = new Ally(getSpriteId());
        players.add(sprite);
        placeSprite(sprite, players, 0);
        return sprite;
    }

    public Ally[] getPlayersCopy () {
        Ally[] playersArray = new Ally[players.size()];
        int i = 0;

        for (Ally player : players) {
            playersArray[i++] = new Ally(player);
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

    public void startShot (Ally player, Coords origin) {
        if (!board.isFree(origin.transform(Direction.Right))) {
            return;
        }

        Projectile projectile = new Projectile(getSpriteId(), player.getId());
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
}
