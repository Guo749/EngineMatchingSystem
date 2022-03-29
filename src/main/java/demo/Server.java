package demo;

import org.hibernate.jdbc.Work;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Server{
    /* server socket */
    public ServerSocket serverSocket   = null;

    /* the stream we read from the client */
    public DataInputStream in          =  null;

    public static final int MAX_THREAD_ALLOWED = 20;
    public static final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREAD_ALLOWED);

    public Server(int port) throws IOException, ClassNotFoundException, SQLException {
        this.serverSocket = new ServerSocket(port);
        System.out.println("Server started");
        Database.init();
    }

    /**
     * Constantly receive connection from the client
     * and read entire request
     */
    public void run(){
        while(true) {

            Socket client = null;
            try {
                client = this.serverSocket.accept();
                System.out.println("Client accepted");

                threadPool.execute(new Worker(client, this.serverSocket));;
            } catch (Exception e){//whatever bad happens, we close and regard it as bad reqeust
                e.printStackTrace();
            }
        }
    }

}
