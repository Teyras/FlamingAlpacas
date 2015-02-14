package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.ConnectMessage;
import cz.bucharjan.FlamingAlpacas.Messages.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by teyras on 11.2.15.
 */
public class LocalServerInterface implements ServerInterface, Client {
    private GameServer server;
    private List<UpdateAction> updateListeners = new ArrayList<>();
    private long lastUpdateNumber = 0;

    public LocalServerInterface (GameServer server) {
        this.server = server;
    }

    @Override
    public void connect () {
        sendMessage(new ConnectMessage());
    }

    @Override
    public void sendMessage (Message msg) {
        server.receiveMessage(msg, this);
    }

    @Override
    public void addUpdateListener (UpdateAction action) {
        updateListeners.add(action);
    }

    public void update (StatusUpdate update) {
        if (update.getNumber() <= lastUpdateNumber) {
            return;
        }

        for (UpdateAction action: updateListeners) {
            action.run(update);
        }

        lastUpdateNumber = update.getNumber();
    }
}