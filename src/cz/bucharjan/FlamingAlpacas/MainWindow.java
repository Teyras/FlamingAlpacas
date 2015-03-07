package cz.bucharjan.FlamingAlpacas;

import javax.swing.*;
import java.awt.*;
import java.awt.BorderLayout;

/**
 * Created by teyras on 11.2.15.
 */
public class MainWindow {
    ServerInterface serverIface;
    GamePanel panel;
    boolean connected = false;

    public MainWindow (ServerInterface serverInterface) {
        serverIface = serverInterface;

        serverIface.addUpdateListener((StatusUpdate update) -> {
            if (!connected) {
                connected = true;
                setupUI(update.getBoardWidth(), update.getBoardHeight());
            }

            panel.paintMonsters(update.getMonsters());
            panel.repaint();
        });

        serverIface.connect();
    }

    protected void setupUI (int width, int height) {
        panel = new GamePanel(width, height);

        JFrame.setDefaultLookAndFeelDecorated(true);

        final JFrame frame = new JFrame("Flaming Alpacas");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        pane.add(panel);

        frame.pack();
        frame.setVisible(true);
    }
}
