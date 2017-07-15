
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Query {
    public void SQL(String sql)
    {
        String url = "jdbc:postgresql://localhost:5432/holdemManager3";
        String user = "postgres";
        String password = "";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("connected to database...");
            Statement stmt = conn.createStatement();

            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}


