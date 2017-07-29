import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Container {

    private static Connection connection = null;

    private static Properties properties = new Properties();

    public static Properties getProperties(){
        if (properties.isEmpty()){
            try {
                InputStream in = new FileInputStream("config.properties");
                properties.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                properties = getProperties();
                String url = String.format("jdbc:%s://%s:%s/%s"
                        ,properties.getProperty("database")
                        ,properties.getProperty("dbhost")
                        ,properties.getProperty("dbport")
                        ,properties.getProperty("dbname"));
                connection = DriverManager.getConnection(url,properties.getProperty("dbuser"),properties.getProperty("dbpassword"));
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    private Container() {
    }
}
