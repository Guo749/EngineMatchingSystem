package demo;

/**
 * App entry point
 */
public class App {
    public static void main(String[] args) {
        Server server = new Server(12345);
        server.run();
    }
}
