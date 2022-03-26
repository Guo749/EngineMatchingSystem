package demo;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XmlParserTest {
    @Test
    public void testTopLevel() throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException {
        Database.init();
        String xml = "<?xml version = \"1.0\"?> <create> </create>";

        XmlParser xmlParser = new XmlParser();
        assertThrows(IllegalArgumentException.class, () -> xmlParser.processXML(xml));
    }

    @Test
    @Description("test simple create one account")
    public void testCreateOne() throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException {
        Database.init();
        String xml = "<?xml version = \"1.0\"?> <create><account id=\"123456\" balance=\"1000\"/> </create>";

        XmlParser xmlParser = new XmlParser();
        xmlParser.processXML(xml);
    }

    @Test
    public void TestDoCreateAccount() throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException {
        /* make sure db is inited */
        Database.init();
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <create>
             <account id="123456" balance="1000"/>
              <symbol sym="SPY">
              <account id="123458">100000</account>
              <account id="123459">100000</account>
              </symbol>
             <account id="123457" balance="1000"/>
            </create>
            """;

        XmlParser xmlParser = new XmlParser();
        xmlParser.processXML(xml);
    }


    @Test
    public void testParseOrderTransactions() throws ParserConfigurationException, IOException, SAXException {
        String xml = "<?xml version = \"1.0\"?> <transactions id=\"1234\"> <order sym=\"SPY\" amount=\"100\" limit=\"145.67\"/> </transactions>";
        List<Transaction> transactionList = getTransactionList(xml);

        assertEquals(1, transactionList.size());
        assertEquals(OrderTransaction.class, transactionList.get(0).getClass());
        Order order = ((OrderTransaction) transactionList.get(0)).getOrder();
        assertEquals("SPY", order.getSym());
        assertEquals(100, order.getAmount(), 0.001);
        assertEquals(145.67, order.getPriceLimit(), 0.001);
    }

    @Test
    public void testParseQueryAndCancelTransactions() throws ParserConfigurationException, IOException, SAXException {
        String xml = "<?xml version = \"1.0\"?> <transactions id=\"1234\"> " +
                "<order sym=\"SPY\" amount=\"100\" limit=\"145.67\"/>" +
                "<query id=\"853\"/>" +
                "<cancel id=\"6996\"/>" +
                " </transactions>";
        List<Transaction> transactionList = getTransactionList(xml);
        assertEquals(3, transactionList.size());

        assertEquals(QueryTransaction.class, transactionList.get(1).getClass());
        QueryTransaction queryTransaction = (QueryTransaction) transactionList.get(1);
        assertEquals(1234, queryTransaction.getAccountId());
        assertEquals(853, queryTransaction.getTransactionId());

        assertEquals(CancelTransaction.class, transactionList.get(2).getClass());
        CancelTransaction cancelTransaction = (CancelTransaction) transactionList.get(2);
        assertEquals(1234, cancelTransaction.getAccountId());
        assertEquals(6996, cancelTransaction.getTransactionId());
    }


    @Test
    private void TestDoCreateAndSym() throws ParserConfigurationException, IOException, SAXException {
        String xml =
            """
                <?xml version="1.0" encoding="UTF-8"?>
                <create>
                 <account id="123456" balance="1000"/>
                 <symbol sym="SPY">
                 <account id="123456">100000</account>
                 </symbol>
                </create>            
            """;

        XmlParser xmlParser = new XmlParser();
        xmlParser.processXML(xml);
    }

    private List<Transaction> getTransactionList(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(xml);

        ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(input);
        doc.getDocumentElement().normalize();

        XmlParser xmlParser = new XmlParser();
        return xmlParser.parseTransactions(doc);
    }
}
