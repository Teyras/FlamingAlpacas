package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.MoveMessage;
import cz.bucharjan.FlamingAlpacas.Messages.ShootMessage;
import cz.bucharjan.FlamingAlpacas.Messages.SteerMessage;
import cz.bucharjan.FlamingAlpacas.Sprites.Player;
import cz.bucharjan.FlamingAlpacas.Sprites.PlayerAvatar;

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
    private ServerInterface serverIface;
    private GamePanel panel;
    private boolean connected = false;
    private PlayerAvatar player = null;
    private final JFrame frame = new JFrame("Flaming Alpacas");

    private JLabel scoreText;

    public MainWindow (String nickname, ServerInterface serverInterface) {
        serverIface = serverInterface;

        serverIface.addUpdateListener((StatusUpdate update) -> {
            if (!connected) {
                for (Player player : update.getPlayers()) {
                    if (player.getId() == update.getPlayerId()) {
                        this.player = new PlayerAvatar(player);
                    }
                }

                if (player == null) {
                    return;
                }

                connected = true;
                setupUI(update.getBoard());
            }

            StringBuilder builder = new StringBuilder();

            for (ScoreEntry entry : update.getScore()) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }

                builder.append(String.format("%s: %d", entry.nickname, entry.score));
            }

            scoreText.setText(builder.toString());

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

        serverIface.connect(nickname);

        JFrame.setDefaultLookAndFeelDecorated(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    protected void setupUI (Board board) {
        panel = new GamePanel(board, player);

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        pane.add(panel, BorderLayout.CENTER);

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

        JPanel statusbar = new JPanel();
        int fontHeight = statusbar.getFontMetrics(statusbar.getFont()).getHeight();
        statusbar.setPreferredSize(new Dimension(frame.getWidth(), fontHeight));
        statusbar.setLayout(new BoxLayout(statusbar, BoxLayout.X_AXIS));
        frame.add(statusbar, BorderLayout.SOUTH);

        scoreText = new JLabel();
        statusbar.add(scoreText);

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
