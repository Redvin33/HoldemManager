/**
 * Created by Lauri on 27.6.2017.
 */
import java.util.ArrayList;

public class Table {

    private String tableName;
    private ArrayList<Seat> seats;
    private int playerAmount;
    public Table(String tablename, int playerAmount) {
        this.tableName = tablename;
        this.seats = new ArrayList<>();
        this.playerAmount = playerAmount;
        System.out.println("Created table " + tablename + " with max-player amount " + playerAmount);
    }

    public void addSeat(Player player, int number) {
        Seat seat = new Seat(player, number);
        seats.add(seat);
        if(seats.size() > playerAmount) {
            System.out.println("ERROR: program added too man seats to table");
        }
    }

    public void printTable() {
        System.out.println("Created table " + tableName + " with max-player amount " + playerAmount);
        for (Seat seat : seats) {
            seat.print();
        }
    }


}
