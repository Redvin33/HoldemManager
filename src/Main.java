import org.apache.commons.io.input.Tailer;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    //Linkitetty jono, koska jonon kokoa ei tiedetä etukäteen.
    private static BlockingQueue queue = new LinkedBlockingQueue<String>();

    public static void main(String[] args) {
	// write your code here
        LogListener listener = new LogListener(queue);
        File file= new File(args[0]);
        Tailer tailer = new Tailer(file,listener,1000 ,false);
        Thread thread = new Thread(tailer);
        thread.start();
        handle();
    }

    private static void handle(){
        while (true){
            try {
                System.out.println(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
