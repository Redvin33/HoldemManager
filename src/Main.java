import org.apache.commons.io.input.Tailer;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

    //Pls tehkää joku paremmat
    //Matches 'PokerStars Zoom Hand #171235037798:  Hold'em No Limit ($0.01/$0.02) - 2017/06/02 5:35:03 ET'
    static Pattern handPattern = Pattern.compile("(.+)#(\\d+):\\s+(['A-Za-z\\s]+)\\(([$|€|£])(\\d+\\.\\d+)\\/[$|€|£](\\d+\\.\\d+)\\) \\- (\\d+\\/\\d+\\/\\d+) (\\d+:\\d+:\\d+) (\\w+)");
    //Matches 'Seat 1: hirsch262 ($2.10 in chips)"
    static Pattern seatPattern = Pattern.compile("Seat.(\\d+):.(.+)+\\s+\\(([$|€|£])(\\S+).in.chips\\).");
    //Matches '*** RIVER *** [Kd 7s Ac 6c] [6d]' and '*** SHOW DOWN ***'
    static Pattern turnPattern = Pattern.compile("[*]{3}.(.+).[*]{3}.?(?:\\[(.*?)\\])*.?(?:\\[(.*?)\\])*");
    //Matches action
    static Pattern actionPattern = Pattern.compile("(\\w+):.(folds|calls|bets|raises).{0,20}");
    //Matches calling


    public enum Turn{
        HOLECARDS,FLOP,TURN,RIVER,SHOWDOWN,SUMMARY
    }

    //Linkitetty jono, koska jonon kokoa ei tiedetä etukäteen.
    private static BlockingQueue<String> queue = new LinkedBlockingQueue();

    //Removes whitespaces
    public static String trim(String old){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < old.length(); i++){
            if (!Character.isWhitespace(old.charAt(i))){
                sb.append(old.charAt(i));
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        LogListener listener = new LogListener(queue);
        File file= new File (args[0]);

        Tailer tailer = new Tailer(file,listener,1000 ,false);
        Thread thread = new Thread(tailer);

        thread.start();
        handle();

    }



    private static void handle(){
        Map<String, Player> players = new HashMap<>();
        String buttonname = "";
        while (true){
            try {
                String line = queue.take();

                Matcher handMatcher = handPattern.matcher(line);
                Matcher seatMatcher = seatPattern.matcher(line);
                Matcher turnMatcher =  turnPattern.matcher(line);

                if(handMatcher.matches()){
                    System.out.println(handMatcher.group(1) + handMatcher.group(2) + " " + handMatcher.group(3) + handMatcher.group(4)+handMatcher.group(5)+"/"+handMatcher.group(6) + " " + handMatcher.group(7) + handMatcher.group(8) + handMatcher.group(9));
                }
                System.out.println(line);
                System.out.println("jaahas");
                if (seatMatcher.matches()){
                    System.out.println("joo");
                    System.out.println("Seat " + seatMatcher.group(1) + ": " +trim(seatMatcher.group(2)) + " (" + seatMatcher.group(3) + seatMatcher.group(4) + ")");
                    String name = seatMatcher.group(2);
                    if (seatMatcher.group(1).equals("1")) {
                        buttonname = name;
                        System.out.println(buttonname);
                    }


                    if (!players.keySet().contains(name)) {
                        Player player = new Player(name);
                        players.put(name, player);
                    }
                }

                if (turnMatcher.matches()){

                    Turn turn = Turn.valueOf(trim(turnMatcher.group(1)));
                    switch (turn){

                        case HOLECARDS:
                            line = queue.take();
                            turnMatcher = turnPattern.matcher(line);
                            while(!turnMatcher.matches()) {

                                Matcher actionMatcher = actionPattern.matcher(line);

                                if (actionMatcher.matches()) {
                                    String name = actionMatcher.group(1);
                                    String foldraise = actionMatcher.group(2);

                                    if (name.equals(buttonname)) {
                                        System.out.println(name+"  " +foldraise);
                                        players.get(name).button(foldraise);
                                    } else {
                                        players.get(name).hand_append(foldraise);
                                    }
                                }
                                line = queue.take();
                                turnMatcher = turnPattern.matcher(line);

                            }

                            break;

                        case FLOP:
                            System.out.println("FLOP: " + turnMatcher.group(2));
                            break;
                        case TURN:
                            System.out.println("TURN: " + turnMatcher.group(2) + ", "+ turnMatcher.group(3));
                            break;
                        case RIVER:
                            System.out.println("RIVER: " + turnMatcher.group(2) + ", "+ turnMatcher.group(3));
                            break;
                        case SHOWDOWN:
                            System.out.println("SHOWDOWN");
                            break;
                        case SUMMARY:
                            System.out.println("SUMMARY");
                            break;
                    }
                }


            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }
}
