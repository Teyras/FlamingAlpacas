package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.MoveMessage;
import cz.bucharjan.FlamingAlpacas.Messages.SteerMessage;
import cz.bucharjan.FlamingAlpacas.Sprites.Ally;
import cz.bucharjan.FlamingAlpacas.Sprites.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

/**
 * Created by teyras on 11.2.15.
 */
public class MainWindow {
    ServerInterface serverIface;
    GamePanel panel;
    boolean connected = false;
    Player player = null;

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
                setupUI(update.getBoardWidth(), update.getBoardHeight());
            }

            panel.updateSprites(update.getMonsters(), update.getPlayers());
            panel.repaint();
        });

        serverIface.connect();
    }

    protected void setupUI (int width, int height) {
        panel = new GamePanel(width, height, player);

        JFrame.setDefaultLookAndFeelDecorated(true);

        final JFrame frame = new JFrame("Flaming Alpacas");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        pane.add(panel);

        panel.addPlayerMoveListener((Direction direction) -> {
            serverIface.sendMessage(new MoveMessage(direction));
        });

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(25);
                    panel.moveSprites(25);
                    panel.repaint();
                } catch (InterruptedException e) {

                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    System.out.printf("%d frames\n", panel.resetFrameCount());
                } catch (InterruptedException e) {

                }
            }
        }).start();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((KeyEvent e) -> {
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
