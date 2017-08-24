/**
 * Created by Lauri on 23.6.2017.
 */

public class Action {

    public enum Activity{
        CHECKS,CALLS,BETS,RAISES,FOLDS
    }

    public Player getPlayer() {
        return player;
    }

    public Turn getTurn() {
        return turn;
    }

    public Activity getActivity() {
        return activity;
    }

    private Player player;
    private Turn turn;
    private Activity activity;



    private double amount;

    public Action(Turn turn, Player player, Activity activity, double amount) {
        this.player = player;
        this.turn = turn;
        this.activity = activity;
        this.amount = amount;
    }

    public double getAmount() { return amount; }

    public void print() { System.out.println(player +" " + activity);
        return;
    }
    /*
    public void Save(Connection conn, long turn_id, String phase) {
        System.out.println(turn_id+"  " + phase+"TTTTTT");
        ResultSet rs = Query.result("Select id from turns where site_id='"+turn_id+"' and phase ='"+phase+"';", conn);
        int i = 0;
        try {
            rs.next();
            i = rs.getInt(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("ACTION:   " +"INSERT into turn_player_action(player_name, activity, turn_id, amount) VALUES('"+ player +"', '" + activity +"', " + i +", " +amount +");");
        Query.SQL("INSERT into turn_player_action(player_name, activity, turn_id, amount) VALUES('"+ player +"', '" + activity +"', " + i +", " +amount +");"  , conn);
    }
    */

}