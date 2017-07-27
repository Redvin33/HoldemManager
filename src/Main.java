import java.io.File;
public class Main {

    public static void main(String[] args) {
        File file = new File(args[0]);
        Thread[] myThreads = new Thread[4];
        int i = 0;
        if (file.isDirectory()) {
            System.out.println("dlfskjgflkdj");
            for (File file1 : file.listFiles()) {
                myThreads[i] = new Thread(new Game(file1.getName()));
                myThreads[i].start();
            }
        }
        //game.stop();
        //game2.stop();
    }
}
