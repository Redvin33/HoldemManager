public class GameMode {

    private String gamemode;
    private String currency;

    public String getGamemode() {
        return gamemode;
    }

    public void setGamemode(String gamemode) {
        this.gamemode = gamemode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getMinstake() {
        return minstake;
    }

    public void setMinstake(double minstake) {
        this.minstake = minstake;
    }

    public double getMaxstake() {
        return maxstake;
    }

    public void setMaxstake(double maxstake) {
        this.maxstake = maxstake;
    }

    private double minstake;
    private double maxstake;

    public GameMode(String gamemode, String currency, double minstake, double maxstake) {
        this.gamemode = gamemode;
        this.currency = currency;
        this.minstake = minstake;
        this.maxstake = maxstake;
    }
}
