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


/**
 * Created by Jukka on 25.6.2017.
 */
public class Game implements Runnable{

    //todo Better regex x4
    //Matches 'PokerStars Zoom Hand #171235037798:  Hold'em No Limit ($0.01/$0.02) - 2017/06/02 5:35:03 ET'
    public static Pattern handPattern = Pattern.compile("(.+)#(\\d+):\\s+(['A-Za-z\\s]+)\\(([$|€|£])(\\d+\\.\\d+)\\/[$|€|£](\\d+\\.\\d+)\\) \\- (\\d+\\/\\d+\\/\\d+) (\\d+:\\d+:\\d+) (\\w+)");
    //Matches 'Table 'McNaught' 9-max Seat #1 is the button
    public static Pattern tablePattern = Pattern.compile("Table.['](.+)['].(\\d+)(.+)");
    //Matches 'Seat 1: hirsch262 ($2.10 in chips)"
    public static Pattern seatPattern = Pattern.compile("Seat.(\\d+):.(.+)\\(([$|€|£])(\\S+).in.chips\\)");
    //Matches '*** RIVER *** [Kd 7s Ac 6c] [6d]' and '*** SHOW DOWN ***'
    public static Pattern turnPattern = Pattern.compile("[*]{3}.(.+).[*]{3}.?(?:\\[(.*?)\\])*.?(?:\\[(.*?)\\])*");
    //Matches action
    public static Pattern actionPattern = Pattern.compile("(.+):.(folds|calls|bets|raises|checks).{0,60}");
    //Matches holecards
    public static Pattern holecardsPattern = Pattern.compile("(Seat.\\d+:|.+:|.*).?(.*)(shows|mucked|Dealt.to).(.*)\\[(.*)\\]");

    public static Pattern muckedcardsPattern = Pattern.compile("Seat.\\d+:.(.+)\\((button|small blind|big blind)\\).mucked.\\[(.*)\\]");
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
        Table table = new Table("Default", 0);
        HashMap<Player, ArrayList<Card>> curr_players = new HashMap<Player, ArrayList<Card>>();
        ArrayList<Turn> current = new ArrayList<>();



        while (running){
            try {
                String line = queue.take();

                Matcher handMatcher = handPattern.matcher(line);
                Matcher tableMatcher = tablePattern.matcher(line);
                Matcher seatMatcher = seatPattern.matcher(line);
                Matcher turnMatcher =  turnPattern.matcher(line);
                Matcher actionMatcher = actionPattern.matcher(line);
                Matcher holecardMatcher = holecardsPattern.matcher(line);
                Matcher muckedMatcher = muckedcardsPattern.matcher(line);
                System.out.println("LINE: " + line);

                if(handMatcher.matches()){

                    if (turns.size() >= 3) {
                        try {
                            ArrayList<Turn> turns_param = new ArrayList<>();
                            for (Turn turn : current ) {
                                turns_param.add(turn);
                            }
                            HashMap<Player, ArrayList<Card>> players_param = new HashMap<>();
                            for (Player player: curr_players.keySet()) {
                                players_param.put(player, curr_players.get(player));
                            }
                            for (Player player: players_param.keySet()) {
                                System.out.println(player);
                            }

                            Hand hand = new Hand(handName, handid, gameMode, currency, minStake, maxStake, date, timezone, turns_param, table, players_param);
                            hand.printStartingHands();
                            current.clear();
                            curr_players.clear();
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

                else if (tableMatcher.matches()) {
                    int playerAmount = Integer.parseInt(tableMatcher.group(2));
                    table = new Table(tableMatcher.group(1), playerAmount);
                }

                else if(seatMatcher.matches()){
                    int seatNumber = Integer.parseInt(seatMatcher.group(1));
                    String name = seatMatcher.group(2).trim();

                    if (seatMatcher.group(1).equals("1")) {
                        buttonname = name;
                        System.out.println(buttonname);
                    }

                    if (!players.keySet().contains(name)) {
                        Player player = new Player(name);
                        players.put(name, player);
                    }
                    table.addSeat(players.get(name), seatNumber);
                    ArrayList<Card> holecards = new ArrayList<>();
                    Card card1 = new Card("?");
                    Card card2 = new Card("?");
                    holecards.add(card1);
                    holecards.add(card2);
                    curr_players.put(players.get(name), holecards);

                }

                else if(actionMatcher.matches()) {
                    if (phasestring.equals("HOLECARDS")) {
                        String name = actionMatcher.group(1).trim();
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
                    ArrayList<Card> cards = new ArrayList<>();
                    Turn.Phase phase = Turn.Phase.valueOf(Helper.trim(turnMatcher.group(1)));
                    String crds = "";
                    if (turnMatcher.group(2) != null) {
                        crds = turnMatcher.group(2);
                    }
                    System.out.println("KORTIT: " +crds);


                    switch (phase){

                        case HOLECARDS:
                            phasestring = "HOLECARDS";
                            break;

                        case FLOP:
                            phasestring = "FLOP";
                            String[] card_src = crds.split(" ");

                           for(String s: card_src) {
                               Card card = new Card(s);
                               cards.add(card);
                           }


                            Turn flop = new Turn("FLOP", handid, cards);
                            current.add(flop);
                            turns.add(flop);
                            flop.printCards();
                            break;
                        case TURN:
                            phasestring = "TURN";
                            card_src = crds.split(" ");

                            for(String s: card_src) {
                                Card card = new Card(s);
                                cards.add(card);
                            }

                            Turn turn = new Turn("TURN", handid, cards);
                            current.add(turn);
                            turns.add(turn);
                            turn.printCards();
                            break;
                        case RIVER:
                            phasestring = "RIVER";
                            card_src = crds.split(" ");

                            for(String s: card_src) {
                                Card card = new Card(s);
                                cards.add(card);
                            }


                            Turn river = new Turn("RIVER", handid, cards);
                            current.add(river);
                            turns.add(river);
                            river.printCards();
                            break;
                        case SHOWDOWN:
                            System.out.println("SHOWDOWN");
                            break;
                        case SUMMARY:
                            System.out.println("SUMMARY");
                            break;
                    }
                 }


                 else if(muckedMatcher.matches()){
                    for (int i = 0; i<= muckedMatcher.groupCount(); i++) {
                        System.out.println(muckedMatcher.group(i));
                    }
                    String name = muckedMatcher.group(1).trim();
                    String cards = muckedMatcher.group(3).trim();
                    String[] card_src = cards.split(" ");
                    ArrayList<Card> holecards = new ArrayList<>();
                    for (String s: card_src) {
                        Card card = new Card(s);
                        holecards.add(card);
                    }
                    System.out.println("NAME: " +name);

                    curr_players.put(players.get(name), holecards);
                    System.out.println(curr_players.get(players.get(name.trim())).size());


                }

                else if (holecardMatcher.matches()) {
                    String name;
                    System.out.println(line +"!!!!!!!!!!!!!!!!!!!!!!");
                    for (int i = 0; i<= holecardMatcher.groupCount(); i++) {
                        System.out.println(holecardMatcher.group(i));
                    }
                    if(holecardMatcher.group(3).equals("Dealt to" )) {
                        name = holecardMatcher.group(4);
                    } else if (holecardMatcher.group(3).equals("mucked")){
                        name = holecardMatcher.group(2);
                    } else {
                        name = holecardMatcher.group(1);
                    }
                    String cards = holecardMatcher.group(5);
                    String[] card_src = cards.split(" ");
                    ArrayList<Card> holecards = new ArrayList<>();
                    for (String s: card_src) {
                        Card card = new Card(s);
                        System.out.println(s);
                        holecards.add(card);
                    }
                    System.out.println("NAME: " +name.trim());

                    curr_players.put(players.get(name.trim()), holecards);
                    System.out.println(curr_players.get(players.get(name.trim())).size());



                }

            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }
}
