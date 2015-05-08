package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.MoveMessage;
import cz.bucharjan.FlamingAlpacas.Messages.ShootMessage;
import cz.bucharjan.FlamingAlpacas.Messages.SteerMessage;
import cz.bucharjan.FlamingAlpacas.Sprites.Ally;
import cz.bucharjan.FlamingAlpacas.Sprites.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by teyras on 11.2.15.
 */
public class MainWindow {
    ServerInterface serverIface;
    GamePanel panel;
    boolean connected = false;
    Player player = null;
    final JFrame frame = new JFrame("Flaming Alpacas");

    public MainWindow (ServerInterface serverInterface) {
        serverIface = serverInterface;

        serverIface.addUpdateListener((StatusUpdate update) -> {
            if (!connected) {
                for (Ally player : update.getPlayers()) {
                    if (player.getId() == update.getPlayerId()) {
                        this.player = new Player(player);
                    }
                }

                if (player == null) {
                    return;
                }

                connected = true;
                setupUI(update.getBoard());
            }

            switch (update.getState()) {
                case PLAYING:
                    panel.updateSprites(update.getMonsters(), update.getPlayers(), update.getProjectiles());
                    break;
                case FINISHED:
                    panel.setFinished();
                    break;
            }

            panel.repaint();
        });

        serverIface.connect();

        JFrame.setDefaultLookAndFeelDecorated(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    protected void setupUI (Board board) {
        panel = new GamePanel(board, player);

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        pane.add(panel);

        panel.addPlayerMoveListener((Direction direction) -> {
            serverIface.sendMessage(new MoveMessage(direction));
        });

        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        int repaintPeriod = 25;

        executor.scheduleAtFixedRate(() -> {
            panel.moveSprites(repaintPeriod);
            panel.repaint();
        }, 0, repaintPeriod, TimeUnit.MILLISECONDS);

        executor.scheduleAtFixedRate(() -> {
            System.out.printf("%d frames\n", panel.resetFrameCount());
        }, 0, 1, TimeUnit.SECONDS);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((KeyEvent e) -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_SPACE) {
                panel.addProjectile(player.getPosition());
                serverIface.sendMessage(new ShootMessage(player.getPosition()));
                return false;
            }

            Direction direction = keyToDirection(e);

            if (direction == Direction.None) {
                return false;
            }

            switch (e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    player.steer(direction);
                    serverIface.sendMessage(new SteerMessage(player.getDirection()));
                    break;
                case KeyEvent.KEY_RELEASED:
                    player.unsteer(direction);
                    serverIface.sendMessage(new SteerMessage(player.getDirection()));
                    break;
            }

            return false;
        });

        frame.pack();
        frame.setVisible(true);
    }

    private Direction keyToDirection (KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                return Direction.Up;
            case KeyEvent.VK_A:
                return Direction.Left;
            case KeyEvent.VK_S:
                return Direction.Down;
            case KeyEvent.VK_D:
                return Direction.Right;
            default:
                return Direction.None;
        }
    }
}
