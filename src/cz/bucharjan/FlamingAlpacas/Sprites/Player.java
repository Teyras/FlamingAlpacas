package cz.bucharjan.FlamingAlpacas.Sprites;

/**
 * Created by teyras on 18.3.15.
 */
public class Player extends Sprite {
    private String nickname;

    public Player () {
    }

    public Player (int id, String nickname) {
        super(id);
        this.nickname = nickname;
    }

    public Player (Player ally) {
        super(ally);
        nickname = ally.getNickname();
    }

    public String getNickname () {
        return nickname;
    }
}
