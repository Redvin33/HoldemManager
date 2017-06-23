/**
 * Created by Lauri on 23.6.2017.
 */

import java.util.ArrayList;

public class Turn {
    String tyyppi;
    ArrayList<Action> actions;
    long handid;

    public Turn(String tyyppi_, long handid_) {
        this.tyyppi = tyyppi_;
        this.actions = new ArrayList<>();
        this.handid = handid_;
        System.out.println("Created " + tyyppi + " with ID " + handid+ "!!!!!");
    }


    public String getTyyppi() {
        return tyyppi;
    }

    public long getHandid() {
        return handid;
    }
    public void AddAction(String player, String action_) {
        Action action = new Action(player, action_);
        actions.add(action);
        System.out.println("Added action: " + player +" " + action_ + " to " + tyyppi +" " + handid );
        return;
    }


}
