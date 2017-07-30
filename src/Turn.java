/**
 * Created by Lauri on 23.6.2017.
 */

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Vector;
import java.sql.Connection;

public class Turn {

    public enum Phase{
        HOLECARDS,FLOP,TURN,RIVER,SHOWDOWN,SUMMARY
    }

    String tyyppi;
    ArrayList<Action> actions;

    public void setTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    public void setHandid(long handid) {
        this.handid = handid;
    }

    public ArrayList<Card> getTablecards() {
        return tablecards;
    }

    public void setTablecards(ArrayList<Card> tablecards) {
        this.tablecards = tablecards;
    }

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

    public void AddAction(String player, String action_, double amount_) {
        Action action = new Action(player, action_, amount_);
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
