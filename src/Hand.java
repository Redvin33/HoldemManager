import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;

/**
 * Created by Jukka on 25.6.2017.
 */
public class Hand {

    private String handName;
    private long id;
    private String gameMode;
    private Currency currency;
    private double minStake;
    private double maxStake;
    private Date date;
    private ArrayList<Turn> turns;
    private HashMap<Player, ArrayList<Card>> players;
    private Table table;
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

    @Override
    public String toString() {
        return handName + " #" + id + ", " + gameMode + ": ("+currency+" "+minStake+"/"+maxStake+") " + date;
    }
}
