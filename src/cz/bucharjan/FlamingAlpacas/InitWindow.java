package cz.bucharjan.FlamingAlpacas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
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

        Label serverLabel = new Label("Host the game");
        server = new Checkbox();
        server.setState(false);

        Label addressLabel = new Label("Server address");
        address = new TextField();
        address.setEnabled(false);
        address.setEditable(false);

        server.addItemListener((ItemEvent e) -> {
            address.setEnabled(server.getState());
            address.setEditable(server.getState());
        });

        Label portLabel = new Label("Server port");
        port = new TextField();

        Label nicknameLabel = new Label("Nickname");
        nickname = new TextField();

        Button submit = new Button("OK");
        submit.addActionListener((event) -> {
            try {
                Config config = getConfig();

                for (SubmitAction action : submitListeners) {
                    action.run(config);
                }

                frame.setVisible(false);
                frame.dispose();
            } catch (ValidationException e) {
                switch (e.getField()) {
                    case "port":
                        port.setBackground(Color.red);
                        break;
                    case "address":
                        address.setBackground(Color.red);
                        break;
                    case "nickname":
                        nickname.setBackground(Color.red);
                        break;
                }
            }
        });

        form.add(serverLabel);
        form.add(server);

        form.add(addressLabel);
        form.add(address);

        form.add(portLabel);
        form.add(port);

        form.add(nicknameLabel);
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

    private Config getConfig () throws ValidationException {
        Config config = new Config();
        config.server = server.getState();

        if (port.getText().equals("")) {
            throw new ValidationException("port");
        }

        try {
            config.port = Integer.parseInt(port.getText());
        } catch (NumberFormatException e) {
            throw new ValidationException("port");
        }

        if (server.getState()) {
            try {
                config.address = InetAddress.getByName(address.getText());
            } catch (UnknownHostException e) {
                throw new ValidationException("address");
            }
        }

        if (nickname.getText().equals("")) {
            throw new ValidationException("nickname");
        }

        config.nickname = nickname.getText();

        return config;
    }
}

class ValidationException extends Exception {
    private String field;

    public ValidationException (String field) {
        this.field = field;
    }

    public String getField () {
        return field;
    }
}
