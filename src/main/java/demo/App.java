package demo;

import java.io.IOException;
import java.sql.SQLException;

/**
 * App entry point
 */
public class App {
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        Server server = new Server(12345);
        server.run();
    }
}
