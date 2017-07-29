
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public  class Query {
    public static boolean SQL(String sql, Connection conn)
    {
        System.out.println("SQL:  " +sql);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            try {
                conn.rollback();
            } catch(SQLException er) {
                System.out.println(er.getMessage());
            }
            return false;
        }

    }
    public static ResultSet result(String sql, Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            return rs;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

}


