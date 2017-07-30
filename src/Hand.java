import java.sql.PreparedStatement;
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

    public String handName;
    public long id;
    public String gameMode;
    public Currency currency;
    public double minStake;

    public String getHandName() {
        return handName;
    }

    public void setHandName(String handName) {
        this.handName = handName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public double maxStake;
    public Date date;
    public ArrayList<Turn> turns;
    public HashMap<Player, ArrayList<Card>> players;
    public Table table;
    public Hand(String handName, long id, String gameMode, String currency, double minStake, double maxStake, Date date, String timezone, ArrayList<Turn> turns, Table table, HashMap<Player, ArrayList<Card>> players) throws ParseException{
        this.handName = handName;
        this.id = id;
        this.gameMode = gameMode;
        this.minStake = minStake;
        this.maxStake = maxStake;
        this.turns = turns;
        this.table = table;
        this.players = players;
        //Symbols are converted into currency code.
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
        this.date = date;


    }

    public void printActions() {

        for(Turn turn : turns) {
            turn.printActions();
        }

    }

    public void printStartingHands() {

        System.out.println("Hand "+ id + " starting hands");
        for(Player player : players.keySet()) {
            System.out.println(player.getName() +" ["+players.get(player).get(0).getCard() +"] [" + players.get(player).get(1).getCard()+"]");
        }
    }

    public void Save(Connection conn) {
        try {
            PreparedStatement handStatement = conn.prepareStatement("INSERT INTO hands(table_name, gamemode_name, siteid, name, date) VALUES(?,?,?,?,?)");
            handStatement.setString(1, table.getTableName());
            handStatement.setString(2, gameMode);
            handStatement.setString(3, Long.toString(id));
            handStatement.setString(4, handName);
            handStatement.setDate(5, new java.sql.Date(date.getTime()));
            handStatement.executeUpdate();
            Helper.debug("Hand saved");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return handName + " #" + id + ", " + gameMode + ": ("+currency+" "+minStake+"/"+maxStake+") " + date;
    }
}
