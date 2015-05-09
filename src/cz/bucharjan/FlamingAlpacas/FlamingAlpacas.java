package cz.bucharjan.FlamingAlpacas;

import javax.swing.*;
import java.net.SocketException;

/**
 * Created by teyras on 30.12.14.
 */
public class FlamingAlpacas {
    private static MainWindow window = null;

    public static void main (String[] args) {
        SwingUtilities.invokeLater(() -> {
            InitWindow init = new InitWindow();

            init.addSubmitListener(FlamingAlpacas::startGame);
        });
    }

    private static void startGame (Config config) {
        ServerInterface serverInterface = null;

        if (config.server) {
            final GameServer server = new GameServer(config.port);
            new Thread(() -> {
                try {
                    server.serve();
                } catch (SocketException e) {
                    System.err.println("Error creating server socket");
                    if (window != null) {
                        window.close();
                    }
                }
            }).start();

            serverInterface = new LocalServerInterface(server);
        } else {
            try {
                serverInterface = new RemoteServerInterface(config.address, config.port);
            } catch (SocketException e) {
                System.err.println("Error creating client socket");
            }
        }

        window = new MainWindow(config.nickname, serverInterface);
    }
}