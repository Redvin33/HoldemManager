/**
 * Created by Lauri on 23.6.2017.
 */
public class Action {
    private String player;
    private String action;
    private double amount;

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
