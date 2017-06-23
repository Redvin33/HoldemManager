import org.apache.commons.io.input.Tailer;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    //Matches 'PokerStars Zoom Hand #171235037798:  Hold'em No Limit ($0.01/$0.02) - 2017/06/02 5:35:03 ET'
    static Pattern handPattern = Pattern.compile("(.+)#(\\d+):\\s+(['A-Za-z\\s]+)\\(([$|€|£])(\\d+\\.\\d+)\\/[$|€|£](\\d+\\.\\d+)\\) \\- (\\d+\\/\\d+\\/\\d+) (\\d+:\\d+:\\d+) (\\w+)");
    //Matches 'Seat 1: hirsch262 ($2.10 in chips)"
    static Pattern seatPattern = Pattern.compile("Seat.(\\d+):.(.+)\\(([$|€|£])(.+).");
    //Matches '*** RIVER *** [Kd 7s Ac 6c] [6d]' and '*** SHOW DOWN ***'
    static Pattern turnPattern = Pattern.compile("[*]{3}.(.+).[*]{3}.?(?:\\[(.*?)\\])*.?(?:\\[(.*?)\\])*");
    //Matches action
    static Pattern actionPattern = Pattern.compile("(\\w+):.(folds|calls|bets|raises).{0,20}");

    public enum Phase{
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
        long handid = 0;
        String phasestring = "";
        ArrayList<Turn> current = new ArrayList<>();
        ArrayList<Turn> turns = new ArrayList<>();


        while (true){
            try {
                String line = queue.take();
                System.out.println(line);
                Matcher handMatcher = handPattern.matcher(line);
                Matcher seatMatcher = seatPattern.matcher(line);
                Matcher turnMatcher =  turnPattern.matcher(line);
                Matcher actionMatcher = actionPattern.matcher(line);

                if(handMatcher.matches()){
                    System.out.println(handMatcher.group(1) + handMatcher.group(2) + handMatcher.group(3) + handMatcher.group(4)+handMatcher.group(5)+"/"+handMatcher.group(6) + " " + handMatcher.group(7) + handMatcher.group(8) + handMatcher.group(9));
                    handid = Long.parseLong(trim(handMatcher.group(2)));
                    phasestring = "";

                }

                if(seatMatcher.matches()){


                    String name = trim(seatMatcher.group(2));
                    if (seatMatcher.group(1).equals("1")) {
                        buttonname = name;
                        System.out.println(buttonname);
                    }

                    if (!players.keySet().contains(name)) {
                        Player player = new Player(name);
                        players.put(name, player);
                    }
                }

                if(actionMatcher.matches()) {
                    if (phasestring.equals("HOLECARDS")) {
                        String name = trim(actionMatcher.group(1));
                        String foldraise = actionMatcher.group(2);

                        if (name.equals(buttonname)) {
                            System.out.println(name + "  " + foldraise);
                            players.get(name).button(foldraise);
                        } else {
                            players.get(name).hand_append(foldraise);
                        }

                    } else {
                        String name = actionMatcher.group(1);
                        String foldraise = actionMatcher.group(2);
                        current.get(0).AddAction(name, foldraise);
                    }

                }


                if(turnMatcher.matches()){
                    Phase phase = Phase.valueOf(trim(turnMatcher.group(1)));

                    switch (phase){

                        case HOLECARDS:
                            phasestring = "HOLECARDS";
                            break;
                        case FLOP:
                            phasestring = "FLOP";
                            Turn flop = new Turn("FLOP", handid);
                            current.clear();
                            current.add(flop);
                            turns.add(flop);
                            break;
                        case TURN:
                            phasestring = "TURN";
                            Turn turn = new Turn("TURN", handid);
                            current.clear();
                            current.add(turn);
                            turns.add(turn);
                            break;
                        case RIVER:
                            phasestring = "RIVER";
                            Turn river = new Turn("RIVER", handid);
                            current.clear();
                            current.add(river);
                            turns.add(river);
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
