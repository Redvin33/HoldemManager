import java.sql.*;

import java.util.Map;

public  class Query {

    public Connection con;

    public Query(){
        con = Container.createConnection();
    }

    public void commit() throws SQLException {
        con.commit();
    }

    public void rollback() throws SQLException {
        con.rollback();
    }

    public boolean playerExists(String name, Game game) throws SQLException {
        PreparedStatement playerQueryStatement = con.prepareStatement("SELECT name FROM players WHERE NAME = ?");
        playerQueryStatement.setString(1, name);
        ResultSet results = playerQueryStatement.executeQuery();
        return results.next();
    }

    public boolean tableExists(String table, Game game) throws SQLException {
        PreparedStatement playerQueryStatement = con.prepareStatement("SELECT name FROM tables WHERE NAME = ?");
        playerQueryStatement.setString(1, table);
        ResultSet results = playerQueryStatement.executeQuery();
        return results.next();
    }

    public void save(Hand hand) throws SQLException {
        if (hand != null) {
            try {
                PreparedStatement handStatement = con.prepareStatement("INSERT INTO hands(table_name, currency, gamemode_name, siteid, name, date) VALUES(?,?,?,?,?,?)");
                handStatement.setString(1, hand.getTable().getTableName());
                handStatement.setString(2, hand.getCurrency().toString());
                handStatement.setString(3, hand.getGameMode().getGamemode());
                handStatement.setLong(4, hand.getId());
                handStatement.setString(5, hand.getHandName());
                handStatement.setDate(6, new Date(hand.getDate().getTime()));
                handStatement.executeUpdate();

                con.commit();
                Helper.debug("Hand saved");
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void save(Turn turn) throws SQLException {
        if (turn != null) {
            try {
                PreparedStatement turnStatement = con.prepareStatement("INSERT INTO turns(site_id, phase, communitycards) VALUES (?,?,?)");
                turnStatement.setLong(1, turn.getHandid());
                turnStatement.setString(2, turn.getTyyppi().toString());
                turnStatement.setArray(3, (turn.getTablecards() != null) ? con.createArrayOf("TEXT", turn.getTablecards()) : null);
                turnStatement.executeUpdate();
                Helper.debug("Turn saved");
                con.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();

            }
        }
    }


    public void save(Player player, Game game) throws SQLException {
        if (player != null && !playerExists(player.getName(), game)) {
            PreparedStatement playerStatement = null;
            try {
                playerStatement = con.prepareStatement("INSERT INTO players(name) VALUES(?)");
                playerStatement.setString(1, player.getName());
                playerStatement.executeUpdate();
                con.commit();
                Helper.debug("Player saved");
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void save(Table table, Game game) throws SQLException {
        if (table != null && !tableExists(table.getTableName(), game)) {
            try {
                PreparedStatement tableStatement = con.prepareStatement("INSERT INTO tables(name) VALUES (?)");
                tableStatement.setString(1, table.getTableName());
                tableStatement.executeUpdate();
                con.commit();
                Helper.debug("Table saved");
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void save(GameMode mode) throws SQLException {

        if (mode != null) {
            try {
                PreparedStatement modeStatement = con.prepareStatement("INSERT INTO gamemodes(gamemode, minstake, maxstake) VALUES (?,?,?)");
                modeStatement.setString(1, mode.getGamemode());
                modeStatement.setDouble(2, mode.getMinstake());
                modeStatement.setDouble(3, mode.getMaxstake());
                modeStatement.executeUpdate();
                con.commit();
                Helper.debug("Game mode saved");
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void save(Action action) throws SQLException {
        if (action != null) {
            try {
                PreparedStatement actionStatement = con.prepareStatement("INSERT INTO turn_player_action(player_name,action,site_id_,phase_,amount) VALUES (?,?,?,?,?)");
                actionStatement.setString(1, action.getPlayer().getName());
                actionStatement.setString(2, action.getActivity().toString());
                actionStatement.setLong(3, action.getTurn().getHandid());
                actionStatement.setString(4, action.getTurn().getTyyppi().toString());
                actionStatement.setDouble(5, action.getAmount());
                actionStatement.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
    }

    public void saveHolecards(Hand hand) throws SQLException {
        if (hand != null && hand.getHoldecards() != null) {
            try {
                PreparedStatement holecardsStatement = con.prepareStatement("INSERT INTO hand_player(hand_id,playername,cards) VALUES (?,?,?)");

                for (Map.Entry<Player, String[]> entry : hand.getHoldecards().entrySet()) {
                    if (entry.getKey().getName().isEmpty()) {
                        System.out.println(hand.getId() +" !!!!!!!!!!" );

                    }
                    holecardsStatement.setLong(1, hand.getId());
                    holecardsStatement.setString(2, entry.getKey().getName());
                    holecardsStatement.setArray(3, con.createArrayOf("TEXT", entry.getValue()));
                    holecardsStatement.executeUpdate();
                    Helper.debug("Holecards saved");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                con.rollback();
            }
        }
    }

    public boolean handExists(long handId) throws SQLException {
        PreparedStatement playerQueryStatement = con.prepareStatement("SELECT siteid FROM hands WHERE siteid = ?");
        playerQueryStatement.setLong(1, handId);
        ResultSet results = playerQueryStatement.executeQuery();

        return results.next();
    }

    public double VPIP(String player) throws SQLException {
        try {

            Statement stmt = con.createStatement();
            ResultSet hands = stmt.executeQuery("Select count(*) from turn_player_action, turns where turns.site_id = turn_player_action.site_id_ and phase='HOLECARDS' and player_name ='"+player+"';");
            Statement stmt1 = con.createStatement();
            ResultSet vpip = stmt1.executeQuery("Select count(*) from turn_player_action, turns where turns.site_id = turn_player_action.site_id_ and phase='HOLECARDS' and player_name ='"+player+"' and (action='RAISES' or action='CALLS');");

            hands.next();
            vpip.next();

            int hands_ = hands.getInt(1);
            int vpip_ = vpip.getInt(1);

            return((double)vpip_/(double)hands_)*100;

        } catch(SQLException e) {
            System.out.println(e.getMessage());
            con.rollback();
            return 0.0;
        }
    }

    public double betFlop(String player) throws SQLException {
        try {
            Statement check_stmt = con.createStatement();
            ResultSet checks = check_stmt.executeQuery("Select count(*) from turn_player_action, turns where turns.site_id = turn_player_action.site_id_ and phase='FLOP' and player_name ='"+player+"' and action ='CHECKS' ;");
            Statement bets_stmt = con.createStatement();
            ResultSet bets = bets_stmt.executeQuery("Select count(*) from turn_player_action, turns where turns.site_id = turn_player_action.site_id_ and phase='FLOP' and player_name='"+player+"' and action = 'BETS';");

            checks.next();
            bets.next();


            int bets_ = bets.getInt(1);
            int checks_ = checks.getInt(1);

            return ((double)bets_/((double)bets_+(double)checks_))*100;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            con.rollback();
            return 0.0;
        }
    }

}

