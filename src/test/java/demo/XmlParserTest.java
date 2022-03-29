package demo;

import com.sun.javafx.image.PixelAccessor;
import javafx.util.Pair;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class XmlParserTest {
    private final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";


    @Test
    @Description("test generate clause")
    public void testClause(){
        System.out.println(geneCreateAccountClause(geneRandAccountNum(), "1000"));

        System.out.println("-----------");
        List<Pair<String, String>> list = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            list.add(new Pair<>(geneRandAccountNum(), "500"));
        }

        System.out.println(geneCreateSymClause("SYM", list));
    }



    @Test
    @Description("no action specify")
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
        String accountNum = geneRandAccountNum();
        String xml = "<?xml version = \"1.0\"?> <create><account id=\"" + accountNum+ "\" balance=\"1000\"/> </create>";

        XmlParser xmlParser = new XmlParser();
        xmlParser.processXML(xml);
    }

    @Test
    @Description("create duplicate account")
    public void testCreateDuplicate() throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException {
        Database.init();
        String accountNum = geneRandAccountNum();
        String xml = "<?xml version = \"1.0\"?> <create><account id=\"" + accountNum + "\" balance=\"1000\"/> </create>";

        XmlParser xmlParser = new XmlParser();
        String res1 = xmlParser.processXML(xml);

        String res2 = xmlParser.processXML(xml);

        assert(!res1.contains("ACCOUNT HAS EXISTED"));
        assert(res2.contains("ACCOUNT HAS EXISTED"));
    }

    @Test
    @Description("test one create and one put")
    public void testCreateAndPutSymbol() throws SQLException, ClassNotFoundException, ParserConfigurationException, IOException, SAXException {
        Database.init();

        String accountId = geneRandAccountNum();
        String createAccount = geneCreateAccountClause(accountId, "1000");

        List<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>(accountId, "500"));
        String createSym     = geneCreateSymClause("BTC", list);

        StringBuilder xml = new StringBuilder();
        xml.append(xmlHeader);
        xml.append("<create>\n");
        xml.append(createAccount);
        xml.append(createSym);
        xml.append("</create>\n");
        System.out.println(xml);

        XmlParser xp = new XmlParser();
        xp.processXML(xml.toString());
    }

    @Test
    @Description("")
    public void testMultipleCreateAndPut() throws SQLException, ClassNotFoundException, ParserConfigurationException, IOException, SAXException {
        Database.init();
        StringBuilder xml = new StringBuilder();
        String accountID1 = geneRandAccountNum();
        String accountID2 = geneRandAccountNum();

        String create1 = geneCreateAccountClause(accountID1, "500");
        String create2 = geneCreateAccountClause(accountID2, "1500");

        List<Pair<String, String>> list1 = new ArrayList<>();
        List<Pair<String, String>> list2 = new ArrayList<>();

        list1.add(new Pair<>(accountID1, "2000"));
        list2.add(new Pair<>(accountID2, "2000"));

        String put1  = geneCreateSymClause("BTC", list1);
        String put2  = geneCreateSymClause("ABC", list2);

        xml.append(xmlHeader);
        xml.append("<create>\n");
        xml.append(create1);
        xml.append(create2);
        xml.append(put1);
        xml.append(put2);
        xml.append("</create>\n");
        System.out.println(xml);

        XmlParser xp = new XmlParser();
        xp.processXML(xml.toString());

    }

    @Test
    public void TestDoCreateAccount() throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException {
        /* make sure db is inited */
        Database.init();
        String  xml = "test";
//        String xml = """
//            <?xml version="1.0" encoding="UTF-8"?>
//            <create>
//             <account id="123456" balance="1000"/>
//              <symbol sym="SPY">
//              <account id="123458">100000</account>
//              <account id="123459">100000</account>
//              </symbol>
//             <account id="123457" balance="1000"/>
//            </create>
//            """;

        XmlParser xmlParser = new XmlParser();
        assertThrows(IllegalArgumentException.class, () -> xmlParser.processXML(xml));
    }

    @Test
    @Description("test output for actions reply")
    public void testStringReply(){
        List<Command> actions = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            if(i % 2 == 0){
                CreateAccount ca = new CreateAccount(new Account(32.2, geneRandAccountNum()));
                ca.successfulExecute = (i % 4 == 0);
                actions.add(ca);
            }else{
                PutSymbol ps = new PutSymbol(new Account(32.2, geneRandAccountNum()), "BTC", 100.0);
                ps.successfulExecute = (i % 3 == 0);
                actions.add(ps);
            }
        }

        XmlParser xp = new XmlParser();
        System.out.println(xp.formCreateReply(actions));
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



    /*********************** Helper Method **********************************/


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


    /**
     * Generate 6 digits random number, used by account
     */
    public String geneRandAccountNum(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 10; i++){
            Random r  = new Random();
            int digit = r.nextInt() % 10;
            if(digit < 0){
                digit += 10;
            }

            sb.append(digit);
        }

        return sb.toString();
    }

    /**
     * Given several arguments, we generate the corresponding create
     * account clause
     */
    public String geneCreateAccountClause(String account, String Balance){
        String res = "<account id=\"" + account + "\" balance=\"" + Balance + "\"/>\n";

        return res;
    }

    /**
     * Given several arguments, we generate put symbol clause
     */
    public String  geneCreateSymClause(String symbol, List<Pair<String, String>> list){
        StringBuilder sb = new StringBuilder();
        sb.append("<symbol sym=\"").append(symbol).append("\">\n");

        for(Pair<String, String> pair : list){
            sb.append("<account id=\"").append(pair.getKey()).append("\"").append(">")
                .append(pair.getValue()).append("</account>\n");
        }

        sb.append("</symbol>\n");
        return sb.toString();
    }

    public List<Pair<String, String>> generateSymList(int num){
        List<Pair<String, String>> list = new ArrayList<>();

        for(int i = 0; i < num; i++){
            list.add(new Pair<>(geneRandAccountNum(), String.valueOf((new Random().nextInt()) % 5000)));
        }

        return list;
    }

}
