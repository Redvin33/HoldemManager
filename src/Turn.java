/**
 * Created by Lauri on 23.6.2017.
 */

import java.util.ArrayList;
import java.util.Vector;

public class Turn {

    public enum Phase{
        HOLECARDS,FLOP,TURN,RIVER,SHOWDOWN,SUMMARY
    }

    String tyyppi;
    Vector<Action> actions;
    long handid;

    public Turn(String tyyppi_, long handid_) {
        this.tyyppi = tyyppi_;
        this.actions = new Vector<>();
        this.handid = handid_;
        System.out.println("Created " + tyyppi + " with ID " + handid);
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

    public void printActions() {
        System.out.println(handid+ " " + tyyppi);
        for (Action action : actions) {
            action.print();
        }
        return;
    }

}
