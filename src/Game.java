import org.apache.commons.io.input.Tailer;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    //OLD (Seat.\d+:|.+:|.*).?(.*)(shows|showed|mucked|Dealt.to).(.*)\[(.*)\].{0,120}
    public static Pattern holecardsPattern = Pattern.compile("(?:Dealt to\\s(.*)?\\s\\[(.*)?\\])|(?:Seat\\s\\d+:\\s)([^\\s]*)\\s.*(?:showed|folded|mucked).*\\[(.*)\\].*");
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
                con.commit();
                Helper.debug("Hand saved");
            } catch (SQLException e) {
                System.out.println("DEBUG6: pöö");
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
                con.commit();
                Helper.debug("Player saved");
            } catch (SQLException e) {
                e.printStackTrace();
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
                e.printStackTrace();
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
                e.printStackTrace();
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

    public void saveHolecards(Hand hand) throws SQLException {
        if (hand != null && hand.getHoldecards() != null){
            try {
                PreparedStatement holecardsStatement = con.prepareStatement("INSERT INTO hand_player(hand_id,playername,cards) VALUES (?,?,?)");
                for (Map.Entry<Player,String[]> entry : hand.getHoldecards().entrySet()){
                    holecardsStatement.setLong(1,hand.getId());
                    holecardsStatement.setString(2,entry.getKey().getName());
                    holecardsStatement.setArray(3,con.createArrayOf("TEXT",entry.getValue()));
                    holecardsStatement.executeUpdate();
                    Helper.debug("Holecards saved");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
    }

    @Override
    public void run() {
        new Thread(logTailer).start();

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
                        if(phase == Turn.Phase.SUMMARY){
                            saveHolecards(hand);
                            con.commit();
                        }
                        gameMode = new GameMode(handMatcher.group(3)
                                , Double.parseDouble(handMatcher.group(5))
                                , Double.parseDouble(handMatcher.group(6)));
                        hand = new Hand(handMatcher.group(1)
                                , Long.parseLong(handMatcher.group(2))
                                , gameMode
                                , handMatcher.group(4)
                                , handMatcher.group(7)
                                , handMatcher.group(8));
                        System.out.println("DEBUG2: "+hand.getId());
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
                                ,table.getPlayer(actionMatcher.group(1),true)
                                ,activity
                                ,amount);
                        save(action);
                    }
                    else if (holecardMatcher.matches()){
                        if (phase == Turn.Phase.HOLECARDS){
                            Player player = table.getPlayer(holecardMatcher.group(1),true);
                            hand.addHolecards(player,holecardMatcher.group(2));
                        }
                        else if(phase == Turn.Phase.SUMMARY){
                            Player player = table.getPlayer(holecardMatcher.group(3),true);
                            if (!hand.getHoldecards().containsKey(player)){
                                hand.addHolecards(player,holecardMatcher.group(4));
                            }
                        }
                    }
                    if (phase == Turn.Phase.SUMMARY && queue.isEmpty()){
                        saveHolecards(hand);
                        con.commit();
                    }

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
            }
        }
    }
}
