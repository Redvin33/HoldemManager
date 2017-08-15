import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.util.concurrent.BlockingQueue;

public class LogListener extends TailerListenerAdapter {

    //Viite jono-olioon
    private BlockingQueue<String> queue;
    private Game game;
    private boolean eof = true;

    public LogListener(BlockingQueue queue, Game game){
        this.queue = queue;
        this.game = game;
    }

    @Override
    public void handle(String line){
        try {
            queue.put(line);
            eof = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endOfFileReached(){
        if (!eof){
            game.lastTime = System.currentTimeMillis();
            eof = true;
        }

    }
}
