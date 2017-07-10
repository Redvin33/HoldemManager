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
    ArrayList<Action> actions;
    ArrayList<Card> tablecards;
    long handid;

    public Turn(String tyyppi_, long handid_, ArrayList<Card> cards) {
        this.tyyppi = tyyppi_;
        this.actions = new ArrayList<>();
        this.handid = handid_;
        this.tablecards = cards;
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

    public void printCards() {
        String printed = "";
        for(Card card: tablecards) {
            printed = printed.concat("[" + card.getCard() +"] ");
        }
        System.out.println(tyyppi+ " " + handid +": " +printed);
        return;
    }

}
