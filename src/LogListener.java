import org.apache.commons.io.input.TailerListenerAdapter;

import java.util.concurrent.BlockingQueue;

public class LogListener extends TailerListenerAdapter {

    //Viite jono-olioon
    private BlockingQueue<String> queue;

    public LogListener(BlockingQueue queue){
        this.queue = queue;
    }

    @Override
    public void handle(String line){
        try {
            queue.put(line);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
