import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Jukka on 25.6.2017.
 */
public class Hand{

    private String handName;
    private long id;
    private GameMode gameMode;
    private Currency currency;
    private Date date;
    private ArrayList<Turn> turns;
    private HashMap<Player,String[]> holecards;
    private Table table;

    public Currency getCurrency() {
        return currency;
    }

    public String getHandName() {
        return handName;
    }

    public long getId() {
        return id;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public Date getDate() {
        return date;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public HashMap<Player, String[]> getHoldecards(){
        return this.holecards;
    }

    public void addHolecards(Player player, String cards){
        holecards.put(player,cards.split(" "));
    }

    public Hand(String handName, long id, GameMode gameMode, String currency, String date, String timezone) throws ParseException{
        this.handName = handName;
        this.id = id;
        this.gameMode = gameMode;
        this.turns = new ArrayList<>();
        this.table = table;
        this.holecards = new HashMap<>();
        switch (currency){
            case "$":
                this.currency = Currency.getInstance("USD");
                break;
            case "€":
                this.currency = Currency.getInstance("EUR");
                break;
            case "£":
                this.currency = Currency.getInstance("GBP");
                break;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //Sometimes timezones have multiple meanings, list them here.
        switch (timezone){
            case "ET":
                format.setTimeZone(TimeZone.getTimeZone("Eastern time"));
                break;
            default:
                format.setTimeZone(TimeZone.getTimeZone(timezone));
                break;
        }
        this.date = format.parse(date);


    }

    public void printActions() {

        for(Turn turn : turns) {
            turn.printActions();
        }

    }
    /*
    public void printStartingHands() {

        System.out.println("Hand "+ id + " starting hands");
        for(Player player : players.keySet()) {
            System.out.println(player.getName() +" ["+players.get(player).get(0).getCard() +"] [" + players.get(player).get(1).getCard()+"]");
        }
    }
    */
    /*
    public void Save(Connection conn) {
        System.out.println(id +" RRRRRRRRRRRRRRRRR");
        System.out.println("SQL: " + "INSERT into hands(table_name, gamemode_name, siteid, name, date) VALUES('"+table.getTableName() +"', '"+gameMode.replace("'", "") +"', '" + Long.toString(id) + "', '" + handName+ "', '" + date +"');");
        if(Query.SQL("INSERT into hands(table_name, gamemode_name, siteid, name, date) VALUES('"+table.getTableName() +"', '"+gameMode.replace("'", "") +"', '" + Long.toString(id) + "', '" + handName+ "', '" + date +"');" , conn)) {
            for (Turn turn : turns) {
                turn.Save(conn);
            }

            for (Player player : players.keySet()) {
                String[] holeCards = new String[2];
                holeCards[0] = players.get(player).get(0).getCard();
                holeCards[1] = players.get(player).get(1).getCard();

                int seatnumber = table.getPlayerSeatNumber(player.name);
                Query.SQL("INSERT INTO hand_player(seat_nro, hand_id, playername, cards) VALUES(" + seatnumber + ", '" + id + "', '" + player.name + "', '{" + String.join(", ", holeCards) + "}');", conn);
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            try {
                conn.commit();

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("mitä helvettiä");
        }

    }
    */

    @Override
    public String toString() {
        return handName + " #" + id + ", " + gameMode + date;
    }
}
