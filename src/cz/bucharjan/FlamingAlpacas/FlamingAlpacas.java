package cz.bucharjan.FlamingAlpacas;

import javax.swing.*;

/**
 * Created by teyras on 30.12.14.
 */
public class FlamingAlpacas {
    public static void main (String[] args) {
        SwingUtilities.invokeLater(() -> {
            InitWindow init = new InitWindow();

            init.addSubmitListener(FlamingAlpacas::startGame);
        });
    }

    private static void startGame (Config config) {
        ServerInterface serverInterface;

        if (config.server) {
            final GameServer server = new GameServer(config.port);
            new Thread(server::serve).start();

            serverInterface = new LocalServerInterface(server);
        } else {
            serverInterface = new RemoteServerInterface(config.address, config.port);
        }

        MainWindow window = new MainWindow(serverInterface);
    }
}