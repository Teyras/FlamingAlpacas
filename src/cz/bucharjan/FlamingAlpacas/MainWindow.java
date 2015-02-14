package cz.bucharjan.FlamingAlpacas;

import javax.swing.*;
import java.awt.*;

/**
 * Created by teyras on 11.2.15.
 */
public class MainWindow {
    ServerInterface serverIface;

    public MainWindow (ServerInterface serverInterface) {
        serverIface = serverInterface;
        serverIface.addUpdateListener((StatusUpdate update) -> {
            System.out.printf("Status update %d received%n", update.getNumber());
        });
        serverIface.connect();

        JFrame.setDefaultLookAndFeelDecorated(true);

        final JFrame frame = new JFrame("Flaming Alpacas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();

        frame.pack();
        frame.setVisible(true);
    }
}
