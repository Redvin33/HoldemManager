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
        while (true){
            try {
                String line = queue.take();
                if (line.contains("Seat") && line.contains(":")) {
                    String name = line.split(" ")[2];
                    if (!players.keySet().contains(name)) {
                        Player player = new Player(name);
                        players.put(name, player);
                    }
                }
                if (line.contains("SUMMARY")) {
                    while (line.length() != 0) {
                        line = queue.take();

                        if (line.contains("Seat")) {
                            String[] lista = line.split(" ");
                            String name = lista[2];
                            String foldraise = lista[3];
                            if (foldraise.equals("(button)")) {
                                foldraise = lista[4];
                            } else if (foldraise.equals("(small") || foldraise.equals("(big")) {
                                foldraise = lista[5];
                            }

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
