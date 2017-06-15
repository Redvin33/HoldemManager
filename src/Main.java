import org.apache.commons.io.input.Tailer;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

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
        //Pls tehkää joku paremmat
        //Matches 'PokerStars Zoom Hand #171235037798:  Hold'em No Limit ($0.01/$0.02) - 2017/06/02 5:35:03 ET'
        Pattern handPattern = Pattern.compile("(.+)#(\\d+):\\s+(['A-Za-z\\s]+)\\(([$|€|£])(\\d+\\.\\d+)\\/[$|€|£](\\d+\\.\\d+)\\) \\- (\\d+\\/\\d+\\/\\d+) (\\d+:\\d+:\\d+) (\\w+)");
        //Matches 'Seat 1: hirsch262 ($2.10 in chips)"
        Pattern seatPattern = Pattern.compile("Seat.(\\d+):.(.+)\\(([$|€|£])(\\S+).in.chips\\).");
        //Matches '*** RIVER *** [Kd 7s Ac 6c] [6d]' and '*** SHOW DOWN ***'
        Pattern turnPattern = Pattern.compile("[*]{3}.(.+).[*]{3}.?(?:\\[(.*?)\\])*.?(?:\\[(.*?)\\])*");

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
                if (seatMatcher.matches()){
                    System.out.println("Seat " + seatMatcher.group(1) + ": " +trim(seatMatcher.group(2)) + " (" + seatMatcher.group(3) + seatMatcher.group(4) + ")");
                }
                if (turnMatcher.matches()){
                    Turn turn = Turn.valueOf(trim(turnMatcher.group(1)));
                    switch (turn){
                        case HOLECARDS:
                            System.out.println("PREFLOP");
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
                /*
                if (line.contains("Seat") && line.contains(":") && line.contains("chips")) {
                    String[] row = line.split("\\(");
                    String name = row[0].split(":")[1];
                    name = name.substring(1, name.length() -1);
                    if (row[0].split(":")[0].equals("Seat 1")) {
                        buttonname = name;
                    }


                    if (!players.keySet().contains(name)) {
                        Player player = new Player(name);
                        players.put(name, player);
                    }
                }
                //Starts to handle preflop actions and stops when detects line which contains ***FLOP***
                if (line.contains("*** HOLE CARDS ***")) {


                    while (!line.contains("*** FLOP ***") && !line.contains("*** SUMMARY ***")) {

                        line = queue.take();
                        if (!line.contains(":") || line.contains("doesn't")) {
                            continue;
                        }

                        String[] lista = line.split(":");
                        String name = lista[0];
                        String[] list2 = lista[1].split(" ");
                        String foldraise = list2[1];
                        if (name.equals(buttonname)) {
                            players.get(name).button(foldraise);
                        } else {
                            players.get(name).hand_append(foldraise);
                        }

                    }
                }
                */

            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }
}
