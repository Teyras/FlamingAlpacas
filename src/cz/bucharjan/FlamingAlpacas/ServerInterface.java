package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.Message;

/**
 * Created by teyras on 11.2.15.
 */
interface ServerInterface {
    public void connect (String nickname);
    public void sendMessage (Message msg);
    public void addUpdateListener (UpdateAction action);
}
