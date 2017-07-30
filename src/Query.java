
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;


public class Query {
    private Connection con;
    public Query() {
        this.con =Container.createConnection();
    }

    public boolean save(Hand hand) {
        if (hand != null) {
            try {
                System.out.println("DEBUG: gamemode: "+hand.getGameMode());
                PreparedStatement handStatement = con.prepareStatement("INSERT INTO hands(table_name, gamemode_name, siteid, name, date) VALUES(?,?,?,?,?)");
                PreparedStatement startStatement = con.prepareStatement("INSERT INTO hand_player(seat_nro, hand_id, cards) VALUES(?, ?, ?)");
                handStatement.setString(1, hand.getTable().getTableName());
                handStatement.setString(2, hand.getGameMode());
                handStatement.setLong(3, hand.getId());
                handStatement.setString(4, hand.getHandName());
                handStatement.setDate(5, new java.sql.Date(hand.getDate().getTime()));
                handStatement.executeUpdate();

                for (Turn turn : hand.turns) {
                    if(!save(turn)) {
                       con.rollback();
                       return false;
                    }
                }
                for (Player player : hand.players.keySet()) {
                    ArrayList<String> holeCards = new ArrayList<>();
                    holeCards.add(hand.players.get(player).get(0).getCard());
                    holeCards.add(hand.players.get(player).get(1).getCard());

                    startStatement.setInt(1, hand.table.getPlayerSeatNumber(player.getName()));
                    startStatement.setString(2, Long.toString(hand.id));
                    startStatement.setArray(3, con.createArrayOf("TEXT", holeCards.toArray()));
                    startStatement.executeUpdate();
                }
                System.out.println("Hand " + hand.id +" saved successfully!");
                con.commit();
                return true;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                try {
                    con.rollback();
                } catch(SQLException er) {
                    System.out.println(er.getMessage());
                }
                return false;
            }
        }
        return false;
    }

    public boolean save(Turn turn) {
        if (turn != null) {
            try {
                PreparedStatement turnStatement = con.prepareStatement("INSERT INTO turns(site_id, phase, communitycards) VALUES (?,?,?)");
                turnStatement.setLong(1, turn.getHandid());
                turnStatement.setString(2, turn.getTyyppi());
                turnStatement.setArray(3, con.createArrayOf("TEXT", turn.getTablecards().toArray()));
                turnStatement.executeUpdate();
                Helper.debug("Turn saved");
                for (Action action : turn.getActions()) {
                    if(!save(action, turn.getHandid(), turn.getTyyppi())) {
                        con.rollback();
                        return false;
                    }
                }
                return true;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                try {
                    con.rollback();
                } catch(SQLException er) {
                    System.out.println(er.getMessage());
                }
                return false;
            }
        }
        return false;
    }

    public void save(Player player){
        if (player != null){
            PreparedStatement playerStatement = null;
            try {
                playerStatement = con.prepareStatement("INSERT INTO players(name) VALUES(?)");
                playerStatement.setString(1,player.getName());
                playerStatement.executeUpdate();
                Helper.debug("Player saved");
                con.commit();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                try {
                    con.rollback();
                } catch(SQLException er) {
                    System.out.println(er.getMessage());
                }
            }
        }
    }

    public boolean save(Action action,  long turn_id, String phase) {
        if (action != null) {
            PreparedStatement actionStatement = null;
            try {
                actionStatement = con.prepareStatement("INSERT into turn_player_action(player_name, action, turn_id, amount) VALUES(?, ?, ? ,?)");

                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("Select id from turns where site_id='"+turn_id+"' and phase ='"+phase+"';");
                int i = 0;
                try {
                    rs.next();

                    i = rs.getInt(1);

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                actionStatement.setString(1, action.getPlayer());
                actionStatement.setString(2, action.getAction());
                actionStatement.setInt(3, i);
                actionStatement.setDouble(4, action.getAmount());
                actionStatement.executeUpdate();
                return true;


            } catch (SQLException e) {
                System.out.println(e.getMessage());
                try {
                    con.rollback();
                } catch(SQLException er) {
                    System.out.println(er.getMessage());

                }
                return false;
            }
        }
        return false;
    }

    public void save(Table table){
        if (table != null){
            try {
                PreparedStatement tableStatement = con.prepareStatement("INSERT INTO tables(name) VALUES (?)");
                tableStatement.setString(1,table.getTableName());
                tableStatement.executeUpdate();
                Helper.debug("Table saved");
                con.commit();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                try {
                    con.rollback();
                } catch(SQLException er) {
                    System.out.println(er.getMessage());
                }
            }
        }
    }

    public void save(GameMode mode){

        if(mode != null){

            try {

                PreparedStatement modeStatement = con.prepareStatement("INSERT INTO gamemodes(gamemode, currency, minstake, maxstake) VALUES (?,?,?,?)");
                modeStatement.setString(1,mode.getGamemode());
                modeStatement.setString(2,mode.getCurrency());
                modeStatement.setDouble(3,mode.getMinstake());
                modeStatement.setDouble(4,mode.getMaxstake());
                modeStatement.executeUpdate();
                Helper.debug("Game mode saved");
                con.commit();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                try {
                    con.rollback();
                } catch(SQLException er) {
                    System.out.println(er.getMessage());
                }
            }
        }
    }
}


