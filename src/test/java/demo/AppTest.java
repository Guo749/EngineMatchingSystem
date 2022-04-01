package demo;

import javafx.util.Pair;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppTest {
    XmlParserTest xpt = new XmlParserTest();


    @Test
    @Timeout(15 * 1000)
    @Description("Integration Test")
    public void integrationTest() throws IOException, InterruptedException {
        /* if we want to run this, we need to make sure db service is on*/

        //make sure the server is booted
        Thread.sleep(5000);

        String accountNum   = xpt.geneRandAccountNum();
        String createClause = xpt.geneCreateAccountClause(accountNum, "1000");
        List<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>(accountNum, "500"));
        String putSymClause = xpt.geneCreateSymClause("BTC", list);

        Client client = new Client(createClause + "\n" + putSymClause);
        client.sendXMLAndGetReply();

        /* this time, error should be expected */
        Client client2 = new Client(createClause + "\n" + putSymClause);
        client2.sendXMLAndGetReply();
    }
}
