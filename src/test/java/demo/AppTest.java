package demo;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class AppTest {
    XmlParserTest xpt = new XmlParserTest();


    @Test
    @Description("Integration Test")
    public void integrationTest() throws IOException, InterruptedException {
//        new Thread(() -> {
//            Server server = null;
//            try {
//                System.out.println("reach here?");
//                server = new Server(12345);
//                server.run();
//                System.out.println("reach fucking here?");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();


        //make sure the server is booted
        Thread.sleep(10000);

        String accountNum   = xpt.geneRandAccountNum();
        String createClause = xpt.geneCreateAccountClause(accountNum, "1000");

        Client client = new Client(createClause);
        client.sendXMLAndGetReply();
    }
}
