package cz.bucharjan.FlamingAlpacas.Messages;

/**
 * Created by teyras on 13.2.15.
 */
public class ConnectMessage extends Message {
    private String nickname;

    public ConnectMessage (String nickname) {
        this.nickname = nickname;
    }

    public String getNickname () {
        return nickname;
    }
}
