package cz.bucharjan.FlamingAlpacas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

/**
 * Created by teyras on 11.2.15.
 */
public class InitWindow {
    private List<SubmitAction> submitListeners = new ArrayList<SubmitAction>();
    private TextField address;
    private TextField port;
    private TextField nickname;
    private Checkbox server;

    public InitWindow () {
        JFrame.setDefaultLookAndFeelDecorated(true);

        final JFrame frame = new JFrame("Flaming Alpacas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        Panel form = new Panel(new GridLayout(0, 2));

        address = new TextField();
        port = new TextField();
        server = new Checkbox();
        nickname = new TextField();

        Button submit = new Button("OK");
        submit.addActionListener((event) -> {
            Config config = getConfig();

            for (SubmitAction action: submitListeners) {
                action.run(config);
            }

            frame.setVisible(false);
            frame.dispose();
        });

        form.add(new Label("Host the game"));
        form.add(server);

        form.add(new Label("Server port"));
        form.add(port);

        form.add(new Label("Server address"));
        form.add(address);

        form.add(new Label("Nickname"));
        form.add(nickname);

        pane.add(form, BorderLayout.CENTER);
        pane.add(submit, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    interface SubmitAction {
        public void run (Config config);
    }

    public void addSubmitListener (SubmitAction action) {
        submitListeners.add(action);
    }

    private Config getConfig () {
        Config config = new Config();
        config.server = server.getState();

        try {
            config.port = Integer.parseInt(port.getText());
        } catch (NumberFormatException e) {

        }

        try {
            config.address = InetAddress.getByName(address.getText());
        } catch (UnknownHostException e) {

        }

        config.nickname = nickname.getText();

        return config;
    }
}
