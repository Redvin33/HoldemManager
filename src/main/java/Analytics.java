/**
 * Created by Lauri on 25.7.2017.
 */

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Analytics {

    /*
    public static double buttonbet(String player, Connection conn) {
        ResultSet buttonhands = Query.result("Select count(hand_id) from hand_player where seat_nro=1 and playername='"+ player+"';", conn);
        int buttons = 0;
        int buttonbets1 = 0;
        try {
            buttonhands.next();
            buttons = buttonhands.getInt(1);
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        ResultSet buttonbets = Query.result("Select count(*) from turn_player_action, turns, hand_player, hands where turn_player_action.turn_id = turns.id  and turn_player_action.player_name = hand_player.playername and hands.siteid = hand_player.hand_id and hands.siteid = turns.site_id and playername = '"+player+"' and action = 'raises' and phase ='PREFLOP' and  seat_nro =1" , conn);
        try {
            buttonbets.next();
            buttonbets1 = buttonbets.getInt(1);
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        if (buttons == 0) {
            buttons = 1;
        }
        return ((double)buttonbets1/(double)buttons)*100;
    }
    */
    public static void test() {
        String url = "jdbc:postgresql://localhost:5432/holdemManager3";
        String user = "postgres";
        String password = "";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //System.out.println(buttonbet("Redvin33", conn));



    }



}