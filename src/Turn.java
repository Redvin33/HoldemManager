/**
 * Created by Lauri on 23.6.2017.
 */

import java.sql.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.sql.Connection;

public class Turn {

    public enum Phase{
        HOLECARDS,FLOP,TURN,RIVER,SHOWDOWN,SUMMARY
    }

    Phase phase;
    ArrayList<Action> actions;

    public void setTyyppi(Phase tyyppi) {
        this.phase = tyyppi;
    }

    public void setHandid(long handid) {
        this.handid = handid;
    }

    public String[] getTablecards() {
        return communityCards;
    }

    public void setTablecards(String[] communityCards) {
        this.communityCards = communityCards;
    }

    String[] communityCards;
    long handid;

    public Turn(Phase phase, long handid, String communityCards) {
        this.phase = phase;
        this.actions = new ArrayList<>();
        this.handid = handid;
        this.communityCards = (communityCards != null) ? communityCards.split(" ") : null;
        Helper.debug("Turn created: "+phase+", ID: "+handid + ", cards: "+ Arrays.toString(this.communityCards));
    }


    public Phase getTyyppi() {
        return phase;
    }

    public long getHandid() {
        return handid;
    }
    /*
    public void AddAction(String player, String action_, double amount_) {
        Action action = new Action(player, action_, amount_);
        actions.add(action);
        System.out.println("Added action: " + player +" " + action_ + " to " + phase +" " + handid );
        return;
    }
    */
    public void printActions() {
        System.out.println(handid+ " " + phase);
        for (Action action : actions) {
            action.print();
        }
        return;
    }

    public void printCards() {
        Helper.debug(Arrays.toString(communityCards));
        /*
        String printed = "";
        for(Card card: communityCards) {
            printed = printed.concat("[" + card.getCard() +"] ");
        }
        System.out.println(phase+ " " + handid +": " +printed);
        return;
        */
    }
    /*
    public void Save(Connection conn) {
        String[] community = new String[tablecards.size()];
        int i = 0;
        for (Card crd : tablecards) {
            community[i] = '"' +crd.getCard() +'"';
            i++;
        }
        if(Query.SQL("INSERT into turns(site_id, phase, communitycards) VALUES('"+ handid +"', '"+tyyppi+ "', '{"+ String.join(", ", community) +"}');", conn ))
        {


            System.out.println("Saved " + tyyppi + " " + handid + " to database.");
            for (Action action : actions) {
                action.Save(conn, handid, tyyppi);
            }
        }
    }
    */
}
