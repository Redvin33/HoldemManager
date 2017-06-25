import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Jukka on 25.6.2017.
 */
public class Hand {

    private String handName;
    private double id;
    private String gameMode;
    private Currency currency;
    private double minStake;
    private double maxStake;
    private Date date;
    private ArrayList<Turn> turns;

    public Hand(String handName, double id, String gameMode, String currency, double minStake, double maxStake, String date, String timezone) throws ParseException{
        this.handName = handName;
        this.id = id;
        this.gameMode = gameMode;
        this.minStake = minStake;
        this.maxStake = maxStake;
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
        this.date = format.parse(date);
    }

    @Override
    public String toString() {
        return handName + " #" + id + ", " + gameMode + ": ("+currency+minStake+"/"+maxStake+") " + date;
    }
}
