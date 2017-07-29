/**
 * Created by Lauri on 23.6.2017.
 */

import java.sql.Connection;
import java.sql.*;
import java.sql.SQLException;

public class Action {
    private String player;
    private String action;



    private double amount;

    public Action(String player_, String action_, double amount_) {
        player = player_;
        action = action_;
        amount = amount_;
    }

    public String getPlayer() {
        return player;
    }

    public String getAction() {
        return action;
    }

    public double getAmount() { return amount; }

    public void print() { System.out.println(player +" " + action);
        return;
    }

    public void Save(Connection conn, long turn_id, String phase) {
        ResultSet rs = Query.result("Select id from turns where site_id='"+turn_id+"' and phase ='"+phase+"';", conn);
        int i = 0;
        try {
            rs.next();

            i = rs.getInt(1);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("ACTION:   " +"INSERT into turn_player_action(player_name, action, turn_id, amount) VALUES('"+ player +"', '" + action +"', " + i +", " +amount +");");
        Query.SQL("INSERT into turn_player_action(player_name, action, turn_id, amount) VALUES('"+ player +"', '" + action +"', " + i +", " +amount +");"  , conn);

    }


}
