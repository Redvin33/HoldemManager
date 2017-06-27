/**
 * Created by Lauri on 23.6.2017.
 */
public class Action {
    String player;
    String action;
    public Action(String player_, String action_) {
        player = player_;
        action = action_;
    }

    public String getPlayer() {
        return player;
    }

    public String getAction() {
        return action;
    }

    public void print() { System.out.println(player +" " + action);
        return;
    }


}
