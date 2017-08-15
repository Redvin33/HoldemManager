public class GameMode {

    private String gamemode;

    public String getGamemode() {
        return gamemode;
    }

    public double getMinstake() {
        return minstake;
    }

    public double getMaxstake() {
        return maxstake;
    }

    private double minstake;
    private double maxstake;

    public GameMode(String gamemode, double minstake, double maxstake) {
        this.gamemode = gamemode;
        this.minstake = minstake;
        this.maxstake = maxstake;
    }
}
