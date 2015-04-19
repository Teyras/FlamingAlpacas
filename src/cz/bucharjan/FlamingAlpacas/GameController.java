package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Sprites.Ally;
import cz.bucharjan.FlamingAlpacas.Sprites.Monster;
import cz.bucharjan.FlamingAlpacas.Sprites.Sprite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private int nextSpriteId = 0;

    public GameController (Board board, List<Monster> monsters, List<Ally> players) {
        this.board = board;
        this.monsters = monsters;
        this.players = players;
    }

    public void run () {
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

        executor.scheduleAtFixedRate(() -> {
            spawnMonsters(board.getHeight() - 2);
        }, 0, 10, TimeUnit.SECONDS);

        Map<Sprite, Integer> remaining = new HashMap<>();
        int period = 100;

        executor.scheduleAtFixedRate(() -> {
            for (Monster monster : monsters) {
                remaining.putIfAbsent(monster, monster.getTimePerSquare());
            }

            for (Map.Entry<Sprite, Integer> entry : remaining.entrySet()) {
                Integer newTime = entry.getValue() - period;

                Sprite sprite = entry.getKey();
                while (newTime <= 0) {
                    newTime += sprite.getTimePerSquare();
                    sprite.setPosition(sprite.getPosition().transform(sprite.getDirection()));
                }

                remaining.put(sprite, newTime);
            }
        }, 0, period, TimeUnit.MILLISECONDS);
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
        for (int i = 0; i < count; i++) {
            Monster monster = new Monster(getSpriteId());
            monsters.add(monster);
            placeSprite(monster, monsters, board.getWidth() - 1);
            monster.setDirection(Direction.Left);
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
        Monster[] monstersArray = new Monster[monsters.size()];
        int i = 0;

        for (Monster monster : monsters) {
            monstersArray[i++] = new Monster(monster);
        }

        return monstersArray;
    }

    protected int getSpriteId () {
        return nextSpriteId++;
    }
}
