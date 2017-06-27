import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.input.Tailer;
import java.util.ArrayList;

/**
 * Created by Jukka on 25.6.2017.
 */
public class Game implements Runnable{

    //todo Better regex x4
    //Matches 'PokerStars Zoom Hand #171235037798:  Hold'em No Limit ($0.01/$0.02) - 2017/06/02 5:35:03 ET'
    public static Pattern handPattern = Pattern.compile("(.+)#(\\d+):\\s+(['A-Za-z\\s]+)\\(([$|€|£])(\\d+\\.\\d+)\\/[$|€|£](\\d+\\.\\d+)\\) \\- (\\d+\\/\\d+\\/\\d+) (\\d+:\\d+:\\d+) (\\w+)");
    //Matches 'Seat 1: hirsch262 ($2.10 in chips)"
    public static Pattern seatPattern = Pattern.compile("Seat.(\\d+):.(.+)\\(([$|€|£])(\\S+).in.chips\\)");
    //Matches '*** RIVER *** [Kd 7s Ac 6c] [6d]' and '*** SHOW DOWN ***'
    public static Pattern turnPattern = Pattern.compile("[*]{3}.(.+).[*]{3}.?(?:\\[(.*?)\\])*.?(?:\\[(.*?)\\])*");
    //Matches action
    public static Pattern actionPattern = Pattern.compile("(.+):.(folds|calls|bets|raises|checks).{0,60}");

    //LinkedQueue due to undetermined size, stores rows from logFile.
    private BlockingQueue<String> queue = new LinkedBlockingQueue();
    private Boolean running = true;
    private Tailer logTailer;

    public Game(String logFile){
        this.logTailer = new Tailer( new File(logFile), new LogListener(queue),1000 ,false);
    }

    public void stop(){
        this.running = false;
        this.logTailer.stop();
    }

    @Override
    public void run() {
        new Thread(logTailer).start();
        Map<String, Player> players = new HashMap<>();
        String buttonname = "";
        String phasestring = "";
        ArrayList<Turn> current = new ArrayList<>();
        ArrayList<Turn> turns = new ArrayList<>();
        ArrayList<Hand> hands = new ArrayList<>();

        //Variables for creating hand
        String handName ="";
        long handid = 0;
        String gameMode ="";
        String currency ="";
        double minStake = 0;
        double maxStake = 0;
        String date = "";
        String timezone = "";


        while (running){
            try {
                String line = queue.take();

                Matcher handMatcher = handPattern.matcher(line);
                Matcher seatMatcher = seatPattern.matcher(line);
                Matcher turnMatcher =  turnPattern.matcher(line);
                Matcher actionMatcher = actionPattern.matcher(line);
                System.out.println("LINE: " + line);

                if(handMatcher.matches()){

                    if (turns.size() >= 3) {
                        try {
                            ArrayList<Turn> parameter = new ArrayList<>();
                            for (Turn turn : current ) {
                                parameter.add(turn);
                            }
                            Hand hand = new Hand(handName, handid, gameMode, currency, minStake, maxStake, date, timezone, parameter);
                            current.clear();
                            System.out.println("Created hand " + hand);
                            hand.printActions();
                            hands.add(hand);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    handName = handMatcher.group(1);
                    handid = Long.parseLong(handMatcher.group(2));
                    gameMode = handMatcher.group(3);
                    currency = handMatcher.group(4);
                    minStake = Double.parseDouble(handMatcher.group(5));
                    maxStake = Double.parseDouble(handMatcher.group(6));
                    date = handMatcher.group(7) +" " + handMatcher.group(8);
                    timezone = handMatcher.group(9);
                    phasestring = "";


                }

                else if(seatMatcher.matches()){


                    String name = Helper.trim(seatMatcher.group(2));
                    if (seatMatcher.group(1).equals("1")) {
                        buttonname = name;
                        System.out.println(buttonname);
                    }

                    if (!players.keySet().contains(name)) {
                        Player player = new Player(name);
                        players.put(name, player);
                    }
                }

                else if(actionMatcher.matches()) {
                    if (phasestring.equals("HOLECARDS")) {
                        String name = Helper.trim(actionMatcher.group(1));
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
                        current.get(current.size()-1).AddAction(name, foldraise);
                    }

                }


                else if(turnMatcher.matches()){
                    Turn.Phase phase = Turn.Phase.valueOf(Helper.trim(turnMatcher.group(1)));

                    switch (phase){

                        case HOLECARDS:
                            phasestring = "HOLECARDS";
                            break;
                        case FLOP:
                            phasestring = "FLOP";
                            Turn flop = new Turn("FLOP", handid);
                            current.add(flop);
                            turns.add(flop);
                            break;
                        case TURN:
                            phasestring = "TURN";
                            Turn turn = new Turn("TURN", handid);
                            current.add(turn);
                            turns.add(turn);
                            break;
                        case RIVER:
                            phasestring = "RIVER";
                            Turn river = new Turn("RIVER", handid);
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
