import org.apache.commons.io.input.Tailer;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Jukka on 25.6.2017.
 */
public class Game implements Runnable {

    //todo Better regex x4
    //Matches 'PokerStars Zoom Hand #171235037798:  Hold'em No Limit ($0.01/$0.02) - 2017/06/02 5:35:03 ET'
    public static Pattern handPattern = Pattern.compile("(.+)#(\\d+):\\s+(['A-Za-z\\s]+)\\(([$|€|£])(\\d+\\.\\d+)\\/[$|€|£](\\d+\\.\\d+).?\\w+?\\) \\- (\\d+\\/\\d+\\/\\d+.\\d+:\\d+:\\d+) (\\w+).{0,60}");
    //Matches 'Table 'McNaught' 9-max Seat #1 is the button
    public static Pattern tablePattern = Pattern.compile("Table.['](.+)['].(\\d+)(.+)");
    //Matches 'Seat 1: hirsch262 ($2.10 in chips)"
    public static Pattern seatPattern = Pattern.compile("Seat.(\\d+):.(.+)\\(([$|€|£])(\\S+).in.chips\\).?");
    //Matches '*** RIVER *** [Kd 7s Ac 6c] [6d]' and '*** SHOW DOWN ***'
    public static Pattern turnPattern = Pattern.compile("[*]{3}.(.+).[*]{3}.?(?:\\[(.*?)\\])*.?(?:\\[(.*?)\\])*");
    //Matches action
    public static Pattern actionPattern = Pattern.compile("(.+):.(folds|calls|bets|raises|checks).?([$|€|£])?(\\d+(?:\\.\\d+)?)?(?:.to.)?([$|€|£])?(\\d+(?:\\.\\d+)?)?.?");
    //Matches holecards
    public static Pattern holecardsPattern = Pattern.compile("(Seat.\\d+:|.+:|.*).?(.*)(shows|mucked|Dealt.to).(.*)\\[(.*)\\]");
    //Matches holecards for mucked button/SB/BB player
    public static Pattern muckedcardsPattern = Pattern.compile("Seat.\\d+:.(.+)\\((button|small blind|big blind)\\).mucked.\\[(.*)\\]");
    //LinkedQueue due to undetermined size, stores rows from logFile.
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private Boolean running = true;
    private Tailer logTailer;

    private Connection con;

    public long lastTime = 0;
    public long timeout = 5000;

    public Game(String logFile) {
        con = Container.createConnection();
        this.logTailer = new Tailer(new File(logFile), new LogListener(queue, this), 1000, false);
    }

    public void stop() {
        this.running = false;
        this.logTailer.stop();
        Helper.debug("Game thread shutdown!");
    }

    public void save(Hand hand) throws SQLException {
        if (hand != null) {
            try {
                PreparedStatement handStatement = con.prepareStatement("INSERT INTO hands(table_name, currency, gamemode_name, siteid, name, date) VALUES(?,?,?,?,?,?)");
                handStatement.setString(1, hand.getTable().getTableName());
                handStatement.setString(2, hand.getCurrency().toString());
                handStatement.setString(3, hand.getGameMode().getGamemode());
                handStatement.setLong(4, hand.getId());
                handStatement.setString(5, hand.getHandName());
                handStatement.setDate(6, new java.sql.Date(hand.getDate().getTime()));
                handStatement.executeUpdate();
                Helper.debug("Hand saved");
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void save(Turn turn) throws SQLException {
        if (turn != null) {
            try {
                PreparedStatement turnStatement = con.prepareStatement("INSERT INTO turns(site_id, phase, communitycards) VALUES (?,?,?)");
                turnStatement.setLong(1, turn.getHandid());
                turnStatement.setString(2, turn.getTyyppi().toString());
                turnStatement.setArray(3,(turn.getTablecards() != null) ? con.createArrayOf("TEXT", turn.getTablecards()) : null);
                turnStatement.executeUpdate();
                Helper.debug("Turn saved");
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void save(Player player) throws SQLException {
        if (player != null) {
            PreparedStatement playerStatement = null;
            try {
                playerStatement = con.prepareStatement("INSERT INTO players(name) VALUES(?)");
                playerStatement.setString(1, player.getName());
                playerStatement.executeUpdate();
                Helper.debug("Player saved");
            } catch (SQLException e) {
                //e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void save(Table table) throws SQLException {
        if (table != null) {
            try {
                PreparedStatement tableStatement = con.prepareStatement("INSERT INTO tables(name) VALUES (?)");
                tableStatement.setString(1, table.getTableName());
                tableStatement.executeUpdate();
                Helper.debug("Table saved");
            } catch (SQLException e) {
                //e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void save(GameMode mode) throws SQLException {

        if (mode != null) {
            try {
                PreparedStatement modeStatement = con.prepareStatement("INSERT INTO gamemodes(gamemode, minstake, maxstake) VALUES (?,?,?)");
                modeStatement.setString(1, mode.getGamemode());
                modeStatement.setDouble(2, mode.getMinstake());
                modeStatement.setDouble(3, mode.getMaxstake());
                modeStatement.executeUpdate();
                Helper.debug("Game mode saved");
            } catch (SQLException e) {
                //e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void save(Action action) throws SQLException {
        if (action != null){
            try {

                PreparedStatement actionStatement = con.prepareStatement("INSERT INTO turn_player_action(player_name,action,site_id,phase,amount) VALUES (?,?,?,?,?)");
                actionStatement.setString(1,action.getPlayer().getName());
                actionStatement.setString(2,action.getActivity().toString());
                actionStatement.setLong(3,action.getTurn().getHandid());
                actionStatement.setString(4,action.getTurn().getTyyppi().toString());
                actionStatement.setDouble(5,action.getAmount());
                actionStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
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
        String handName = "";
        long handid = 0;
        //String gameMode = "";
        String currency = "";
        double minStake = 0;
        double maxStake = 0;
        Date date = new Date();
        String timezone = "";
        //Table table = new Table("Default", 0);
        HashMap<Player, ArrayList<Card>> curr_players = new HashMap<>();
        ArrayList<Turn> current = new ArrayList<>();

        Hand hand = null;
        GameMode gameMode = null;
        Table table = null;
        Turn turn = null;
        Turn.Phase phase = null;
        while (running) {
            try {
                if (queue.isEmpty() && lastTime != 0 && (System.currentTimeMillis() - lastTime) > timeout) {
                    stop();
                } else if (queue.size() > 0) {

                    String line = queue.take();

                    Matcher handMatcher = handPattern.matcher(line);
                    Matcher tableMatcher = tablePattern.matcher(line);
                    Matcher seatMatcher = seatPattern.matcher(line);
                    Matcher turnMatcher = turnPattern.matcher(line);
                    Matcher actionMatcher = actionPattern.matcher(line);
                    Matcher holecardMatcher = holecardsPattern.matcher(line);
                    Matcher muckedMatcher = muckedcardsPattern.matcher(line);

                    Helper.debug("LINE: " + line);

                    if (handMatcher.matches()) {
                        gameMode = new GameMode(handMatcher.group(3)
                                , Double.parseDouble(handMatcher.group(5))
                                , Double.parseDouble(handMatcher.group(6)));
                        hand = new Hand(handMatcher.group(1)
                                , Long.parseLong(handMatcher.group(2))
                                , gameMode
                                , handMatcher.group(4)
                                , handMatcher.group(7)
                                , handMatcher.group(8));
                        save(gameMode);
                    } else if (tableMatcher.matches()) {
                        table = new Table(tableMatcher.group(1)
                                , Integer.parseInt(tableMatcher.group(2)));
                        hand.setTable(table);
                        save(table);
                        save(hand);
                    } else if (seatMatcher.matches()) {
                        Player player = new Player(seatMatcher.group(2));
                        Seat seat = new Seat(player, Integer.parseInt(seatMatcher.group(1)));
                        table.addSeat(seat);
                        save(player);
                    } else if (turnMatcher.matches()) {
                        phase = Turn.Phase.valueOf(Helper.trim(turnMatcher.group(1)));
                        String cards = null;
                        if (phase == Turn.Phase.FLOP || phase == Turn.Phase.TURN || phase == Turn.Phase.RIVER){
                            cards = (phase == Turn.Phase.FLOP) ? turnMatcher.group(2) : String.format("%s %s",turnMatcher.group(2),turnMatcher.group(3));
                        }
                        turn = new Turn(phase, hand.getId(), cards);
                        save(turn);
                    } else if (actionMatcher.matches()){
                        Action.Activity activity = Action.Activity.valueOf(actionMatcher.group(2).toUpperCase());
                        double amount;
                        System.out.println("DEBUG2: "+activity);
                        switch (activity){
                            case CALLS:
                                amount = Double.parseDouble(actionMatcher.group(4));
                                break;
                            case BETS:
                                amount = Double.parseDouble(actionMatcher.group(4));
                                break;
                            case RAISES:
                                amount = Double.parseDouble(actionMatcher.group(6)) - Double.parseDouble(actionMatcher.group(4));
                                break;
                            default:
                                amount = 0.0;
                                break;
                        }
                        Action action = new Action(turn
                                ,table.getPlayer(actionMatcher.group(1))
                                ,activity
                                ,amount);
                        save(action);
                    }
                    /*
                    //System.out.println(Analytics.buttonbet("Redvin33", conn)+ "%");
                    if (handMatcher.matches()) {
                        System.out.println("MATCH FOUND!: " + handMatcher.group(3));
                        if (turns.size() >= 1) {
                            System.out.println("päästiin");
                            try {
                                ArrayList<Turn> turns_param = new ArrayList<>();
                                for (Turn turn : current) {
                                    turns_param.add(turn);
                                }
                                HashMap<Player, ArrayList<Card>> players_param = new HashMap<>();
                                for (Player player : curr_players.keySet()) {
                                    players_param.put(player, curr_players.get(player));
                                }
                                for (Player player : players_param.keySet()) {
                                    System.out.println(player);
                                }

                                //Hand hand = new Hand(handName, handid, gameMode, currency, minStake, maxStake, date, timezone, turns_param, table, players_param);

                                current.clear();
                                curr_players.clear();
                                System.out.println("Created hand " + hand);
                                hand.printActions();
                                hands.add(hand);
                                save(hand);
                                //PreparedStatement hand = con.prepareStatement();
                                //hand.Save(conn);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        handName = handMatcher.group(1);
                        handid = Long.parseLong(handMatcher.group(2));
                        //gameMode = handMatcher.group(3);
                        //currency = handMatcher.group(4);
                        //minStake = Double.parseDouble(handMatcher.group(5));
                        //maxStake = Double.parseDouble(handMatcher.group(6));
                        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        try {
                            date = format.parse(handMatcher.group(7) + " " + handMatcher.group(8));
                        } catch (ParseException e) {
                            System.out.println("Date couldnt be parsed");
                        }
                        timezone = handMatcher.group(9);
                        phasestring = "";
                        if (turns.size() == 0) {
                            System.out.println("PEKRLE");
                            //GameMode mode = new GameMode(handMatcher.group(3), handMatcher.group(4), Double.parseDouble(handMatcher.group(5)), Double.parseDouble(handMatcher.group(6)));
                            //save(mode);

                            /*
                            Query.SQL("INSERT INTO gamemodes(gamemode, currency, minstake, maxstake) VALUES('" + gameMode.replace("'", "") + "', '" + currency + "', " + minStake + ", " + maxStake + ");", conn);
                            try {
                                System.out.println("Commit:" + conn.getAutoCommit());
                                conn.commit();
                            } catch (SQLException e) {
                                System.out.println(e.getMessage());
                            }

                        }

                    } else if (tableMatcher.matches()) {
                        int playerAmount = Integer.parseInt(tableMatcher.group(2));
                        table = new Table(tableMatcher.group(1), playerAmount);
                        save(table);
                        //table.Save(conn);

                    } else if (seatMatcher.matches()) {
                        System.out.println("SEATED:");
                        int seatNumber = Integer.parseInt(seatMatcher.group(1));
                        String name = seatMatcher.group(2).trim();

                        if (seatMatcher.group(1).equals("1")) {
                            buttonname = name;
                            System.out.println(buttonname);
                        }

                        if (!players.keySet().contains(name)) {
                            Player player = new Player(name);
                            players.put(name, player);
                            save(player);

                            Query.SQL("INSERT into players(name) VALUES ('" + name + "');", conn);
                            try {
                                conn.commit();
                            } catch (SQLException e) {
                                System.out.println(e.getMessage());
                            }

                        }
                        table.addSeat(players.get(name), seatNumber);
                        ArrayList<Card> holecards = new ArrayList<>();
                        Card card1 = new Card("?");
                        Card card2 = new Card("?");
                        holecards.add(card1);
                        holecards.add(card2);
                        curr_players.put(players.get(name), holecards);

                    } else if (actionMatcher.matches()) {

                        String name = actionMatcher.group(1);
                        String foldraise = actionMatcher.group(2);
                        if (actionMatcher.group(2).equals("raises")) {
                            double i = Double.parseDouble(actionMatcher.group(7));
                            current.get(current.size() - 1).AddAction(name, foldraise, i);
                        } else if (actionMatcher.group(3) != null) {
                            double i = Double.parseDouble(actionMatcher.group(4));
                            current.get(current.size() - 1).AddAction(name, foldraise, i);
                        } else {
                            current.get(current.size() - 1).AddAction(name, foldraise, 0);
                        }

                    } else if (turnMatcher.matches()) {
                        ArrayList<Card> cards = new ArrayList<>();
                        Turn.Phase phase2 = Turn.Phase.valueOf(Helper.trim(turnMatcher.group(1)));
                        String crds = "";
                        if (turnMatcher.group(2) != null) {
                            crds = turnMatcher.group(2);
                        }
                        if (turnMatcher.group(3) != null) {
                            crds = crds.concat(" " + turnMatcher.group(3));
                        }
                        System.out.println("KORTIT: " + crds);
                        Turn turn = null;
                        switch (phase) {

                            case HOLECARDS:
                                phasestring = "HOLECARDS";
                                //turn = new Turn("PREFLOP", handid, cards);
                                current.add(turn);
                                turns.add(turn);
                                turn.printCards();
                                break;

                            case FLOP:
                                phasestring = "FLOP";
                                String[] card_src = crds.split(" ");
                                int i = 0;
                                for (String s : card_src) {
                                    Card card = new Card(s);
                                    cards.add(card);
                                    card_src[i] = '"' + s + '"';
                                    i++;
                                }

                                //turn = new Turn("FLOP", handid, cards);
                                current.add(turn);
                                turns.add(turn);
                                turn.printCards();

                                break;
                            case TURN:
                                phasestring = "TURN";
                                card_src = crds.split(" ");

                                int j = 0;
                                for (String s : card_src) {
                                    Card card = new Card(s);
                                    cards.add(card);
                                    card_src[j] = '"' + s + '"';
                                    j++;

                                }

                                //turn = new Turn("TURN", handid, cards);
                                current.add(turn);
                                turns.add(turn);
                                turn.printCards();

                                break;
                            case RIVER:
                                phasestring = "RIVER";
                                card_src = crds.split(" ");
                                int k = 0;
                                for (String s : card_src) {
                                    Card card = new Card(s);
                                    cards.add(card);
                                    card_src[k] = '"' + s + '"';
                                    k++;
                                }

                                //turn = new Turn("RIVER", handid, cards);
                                current.add(turn);
                                turns.add(turn);
                                turn.printCards();

                                break;
                            case SHOWDOWN:
                                System.out.println("SHOWDOWN");
                                break;
                            case SUMMARY:
                                System.out.println("SUMMARY");
                                break;
                        }
                        save(turn);

                    } else if (muckedMatcher.matches()) {
                        for (int i = 0; i <= muckedMatcher.groupCount(); i++) {
                            System.out.println(muckedMatcher.group(i));
                        }
                        String name = muckedMatcher.group(1).trim();
                        String cards = muckedMatcher.group(3).trim();
                        String[] card_src = cards.split(" ");
                        ArrayList<Card> holecards = new ArrayList<>();
                        for (String s : card_src) {
                            Card card = new Card(s);
                            holecards.add(card);
                        }
                        System.out.println("NAME: " + name);

                        curr_players.put(players.get(name), holecards);
                        System.out.println(curr_players.get(players.get(name.trim())).size());

                    } else if (holecardMatcher.matches()) {
                        String name;

                        for (int i = 0; i <= holecardMatcher.groupCount(); i++) {
                            System.out.println(holecardMatcher.group(i));
                        }
                        if (holecardMatcher.group(3).equals("Dealt to")) {
                            name = holecardMatcher.group(4);
                        } else if (holecardMatcher.group(3).equals("mucked")) {
                            name = holecardMatcher.group(2);
                        } else {
                            name = holecardMatcher.group(1);
                        }
                        String cards = holecardMatcher.group(5);
                        String[] card_src = cards.split(" ");
                        ArrayList<Card> holecards = new ArrayList<>();
                        for (String s : card_src) {
                            Card card = new Card(s);
                            System.out.println(s);
                            holecards.add(card);
                        }
                        System.out.println("NAME: " + name.trim());

                        curr_players.put(players.get(name.trim()), holecards);
                        System.out.println(curr_players.get(players.get(name.trim())).size());
                    }
                    */
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            } finally {
                try {
                    con.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
