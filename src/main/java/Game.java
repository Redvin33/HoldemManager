import org.apache.commons.io.input.Tailer;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
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

    //LinkedQueue due to undetermined size, stores rows from logFile.
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private Boolean running = true;
    private Tailer logTailer;
    private Query query;

    public long lastTime = 0;
    public long timeout = 5000;

    public Game(String logFile) {

        query = new Query();
        //con = Container.createConnection();
        this.logTailer = new Tailer(new File(logFile), new LogListener(queue, this), 1000, false);
        this.query = new Query();
    }

    public void stop() {
        this.running = false;
        this.logTailer.stop();
        Helper.debug("Game thread shutdown!");
    }

    @Override
    public void run() {
        new Thread(logTailer).start();

        Hand hand = null;
        GameMode gameMode = null;
        Table table = null;
        Turn turn = null;
        Turn.Phase phase = null;
        boolean skipHand = false;
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

                    Helper.debug("LINE: " + line);


                    if (handMatcher.matches()) {
                        if (phase == Turn.Phase.SUMMARY) {
                            query.saveHolecards(hand);
                            query.commit();
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
                        skipHand = query.handExists(hand.getId());
                    } else if (!skipHand && tableMatcher.matches()) {
                        table = new Table(tableMatcher.group(1)
                                , Integer.parseInt(tableMatcher.group(2)));
                        hand.setTable(table);
                        query.save(gameMode);
                        query.save(table, this);
                        query.save(hand);
                    } else if (!skipHand && seatMatcher.matches()) {
                        Player player = new Player(seatMatcher.group(2));
                        Seat seat = new Seat(player, Integer.parseInt(seatMatcher.group(1)));
                        table.addSeat(seat);
                        query.save(player, this);
                    } else if (!skipHand && turnMatcher.matches()) {
                        phase = Turn.Phase.valueOf(Helper.trim(turnMatcher.group(1)));
                        String cards = null;
                        if (phase == Turn.Phase.FLOP || phase == Turn.Phase.TURN || phase == Turn.Phase.RIVER) {
                            cards = (phase == Turn.Phase.FLOP) ? turnMatcher.group(2) : String.format("%s %s", turnMatcher.group(2), turnMatcher.group(3));
                        }
                        turn = new Turn(phase, hand.getId(), cards);
                        query.save(turn);
                    } else if (!skipHand && actionMatcher.matches()) {
                        Action.Activity activity = Action.Activity.valueOf(actionMatcher.group(2).toUpperCase());
                        double amount;
                        switch (activity) {
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
                                , table.getPlayer(actionMatcher.group(1), true)
                                , activity
                                , amount);
                        query.save(action);
                    } else if (!skipHand && holecardMatcher.matches()) {
                        if (phase == Turn.Phase.HOLECARDS) {
                            Player player = table.getPlayer(holecardMatcher.group(1), true);
                            hand.addHolecards(player, holecardMatcher.group(2));
                        } else if (phase == Turn.Phase.SUMMARY) {
                            Player player = table.getPlayer(holecardMatcher.group(3), true);
                            if (!hand.getHoldecards().containsKey(player)) {
                                hand.addHolecards(player, holecardMatcher.group(4));
                            }

                        }
                    }
                    if (phase == Turn.Phase.SUMMARY && queue.isEmpty()) {
                        query.saveHolecards(hand);
                        query.commit();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();

            } catch (SQLException e) {
                try {
                    query.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }
}