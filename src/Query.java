
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public  class Query {
    public static void SQL(String sql, Connection conn)
    {

        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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


