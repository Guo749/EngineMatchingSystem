package demo;

import java.io.IOException;

/**
 * App entry point
 */
public class App {
    public static void main(String[] args) throws IOException {
        Server server = new Server(12345);
        server.run();
    }
}
