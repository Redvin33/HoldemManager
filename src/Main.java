import org.apache.commons.io.input.Tailer;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Main {

    //Linkitetty jono, koska jonon kokoa ei tiedetä etukäteen.
    private static BlockingQueue<String> queue = new LinkedBlockingQueue();

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


            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }
}
