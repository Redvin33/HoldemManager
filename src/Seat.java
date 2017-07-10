/**
 * Created by Lauri on 27.6.2017.
 */
public class Seat {
    Player player;
    int seatNumber;
    String role;
    public Seat(Player player, int seatNumber) {
        this.player = player;
        this.seatNumber = seatNumber;
        if(seatNumber == 1) {role = "button";}
        else if (seatNumber == 2) {role = "SB";}
        else if (seatNumber == 3) {role = "BB";}
        else { role ="normal";}
    }

    public void print() {
        System.out.println("Seat "+ seatNumber +": "+ player.getName() + "("+ role +")");
    }
}
