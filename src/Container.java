import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Container {

    private static Properties properties = new Properties();

    private static void initializeProperties() {
        try {
            InputStream in = new FileInputStream("config.properties");
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        if (properties.isEmpty()) {
            initializeProperties();
        }
        return properties.getProperty(key);
    }

    public static Connection createConnection() {
        try {
            Connection connection;
            String url = String.format("jdbc:%s://%s:%s/%s"
                    , getProperty("database")
                    , getProperty("dbhost")
                    , getProperty("dbport")
                    , getProperty("dbname"));
            connection = DriverManager.getConnection(url, getProperty("dbuser"), getProperty("dbpassword"));
            connection.setAutoCommit(false);
            Helper.debug("Connected");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Container() {
    }
}
