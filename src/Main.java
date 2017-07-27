public class Main {

    public static void main(String[] args) {

        Game game = new Game(args[0]);
        Thread g1 = new Thread(game);
        g1.start();
        Game game2 = new Game(args[1]);
        Thread g2 = new Thread(game2);
        g2.start();
        //game.stop();
        //game2.stop();
    }
}
