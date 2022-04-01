package demo;

import com.sun.javafx.image.PixelAccessor;
import javafx.util.Pair;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.crypto.Data;
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

        assert(!res1.contains("Account already exists"));
        assert(res2.contains("Account already exists"));
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
    @Description("multi create & put")
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
        assertThrows(SAXParseException.class, () -> xmlParser.processXML(xml));
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
    public void testParseOrderTransactions() throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException {
        Database.init();
        // Create account 123 and 234, add 100 SPY to account 123
        String xml = "<?xml version = \"1.0\"?> <create> <account id=\"123\" balance=\"15364\"/> " +
                "<account id=\"234\" balance=\"56478\"/> " +
                "<symbol sym=\"SPY\"> <account id=\"123\">100</account> </symbol>" +
                "</create>";
        XmlParser xmlParser = new XmlParser();
        System.out.println(xmlParser.processXML(xml));

        // Account 123 tries to sell 50 SPY (should success) and then 51 SPY (should fail)
        xml = "<?xml version = \"1.0\"?> <transactions id=\"123\">" +
                "<order sym=\"SPY\" amount=\"-50\" limit=\"200\"/> " +
                "<order sym=\"SPY\" amount=\"-51\" limit=\"14\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        // Account 234 tries to buy 20 SPY (should success)
        xml = "<?xml version = \"1.0\"?> <transactions id=\"234\">" +
                "<order sym=\"SPY\" amount=\"20\" limit=\"210\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        // Account 123 tries to query the status of the first order (should be -30 open and 20 executed)
        xml = "<?xml version = \"1.0\"?> <transactions id=\"123\">" +
                "<order sym=\"SPY\" amount=\"-10\" limit=\"14\"/> " +
                "<query id=\"2\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        // Account 123 tries to cancel the first order (should be -30 canceled and 20 executed)
        xml = "<?xml version = \"1.0\"?> <transactions id=\"123\">" +
                "<cancel id=\"2\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        // Account 123 tries to cancel the first order (should fail because there is no open order)
        xml = "<?xml version = \"1.0\"?> <transactions id=\"234\">" +
                "<cancel id=\"2\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        // Account 123 tries to query the status of the first order again (should be -30 canceled and 20 executed)
        xml = "<?xml version = \"1.0\"?> <transactions id=\"123\">" +
                "<query id=\"2\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        Account account = Database.checkAccountIdExistsAndGetIt(123);
        System.out.println(account.getBalance());
        account = Database.checkAccountIdExistsAndGetIt(234);
        System.out.println(account.getBalance());
    }

    @Test
    public void testComplexOrderTransactions() throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException {
        Database.init();
        // Create account 123 and 234, add 100 SPY to account 123
        String xml = "<?xml version = \"1.0\"?> <create> <account id=\"123\" balance=\"15364\"/> " +
                "<account id=\"234\" balance=\"56478\"/> " +
                "<symbol sym=\"SPY\"> <account id=\"123\">100</account> </symbol>" +
                "</create>";
        XmlParser xmlParser = new XmlParser();
        System.out.println(xmlParser.processXML(xml));

        // Account 123 tries to sell 50 SPY (should success) and then 51 SPY (should fail)
        xml = "<?xml version = \"1.0\"?> <transactions id=\"123\">" +
                "<order sym=\"SPY\" amount=\"-50\" limit=\"200\"/> " +
                "<order sym=\"SPY\" amount=\"-51\" limit=\"14\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        xml = "<?xml version = \"1.0\"?> <create> <account id=\"123\" balance=\"15364\"/> " +
                "<symbol sym=\"BIT\"> <account id=\"345\">100</account> </symbol>" +
                "<account id=\"345\" balance=\"99999\"/> " +
                "<symbol sym=\"BIT\"> <account id=\"345\">300</account> </symbol>" +
                "</create>";
        System.out.println(xmlParser.processXML(xml));

        // Account 234 tries to buy 20 SPY (should success)
        xml = "<?xml version = \"1.0\"?> <transactions id=\"234\">" +
                "<order sym=\"SPY\" amount=\"20\" limit=\"210\"/> " +
                "<order sym=\"BIT\" amount=\"50\" limit=\"100\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        // Account 123 tries to query the status of the first order (should be -30 open and 20 executed)
        xml = "<?xml version = \"1.0\"?> <transactions id=\"123\">" +
                "<order sym=\"SPY\" amount=\"-10\" limit=\"300\"/> " +
                "<order sym=\"BIT\" amount=\"150\" limit=\"90\"/> " +
                "<query id=\"2\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        xml = "<?xml version = \"1.0\"?> <transactions id=\"345\">" +
                "<order sym=\"BIT\" amount=\"-120\" limit=\"80\"/> " +
                "<order sym=\"BIT\" amount=\"-120\" /> " +
                "<order sym=\"BIT\" limit=\"80\"/> " +
                "<order amount=\"-120\" limit=\"80\"/> " +
                "<order sm=\"BIT\" amount=\"-120\" limit=\"80\"/> " +
                "<order sym=\"BIT\" amount=\"a\" limit=\"80\"/> " +
                "<order sym=\"BIT\" amount=\"-110\" limit=\"b\"/> " +
                "<cancel id=\"10\"/> " +
                "<cancel id=\"100\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        xml = "<?xml version = \"1.0\"?> <transactions id=\"234\">" +
                "<order sym=\"SPY\" amount=\"35\" limit=\"400\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        xml = "<?xml version = \"1.0\"?> <transactions id=\"123\">" +
                "<query id=\"2\"/> " +
                "<query id=\"5\"/> " +
                "<query id=\"9\"/> " +
                "<query id=\"15\"/> " +
                "<query id=\"8\"/> " +
                "<query id=\"10\"/> " +
                "<query id=\"11\"/> " +
                "<query id=\"91\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        xml = "<?xml version = \"1.0\"?> <transactions id=\"234\">" +
                "<query id=\"2\"/> " +
                "<query id=\"5\"/> " +
                "<query id=\"9\"/> " +
                "<query id=\"15\"/> " +
                "<query id=\"8\"/> " +
                "<query id=\"10\"/> " +
                "<query id=\"11\"/> " +
                "<query id=\"91\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        xml = "<?xml version = \"1.0\"?> <transactions id=\"345\">" +
                "<query id=\"2\"/> " +
                "<query id=\"5\"/> " +
                "<query id=\"9\"/> " +
                "<query id=\"15\"/> " +
                "<query id=\"8\"/> " +
                "<query id=\"10\"/> " +
                "<query id=\"11\"/> " +
                "<query id=\"91\"/> " +
                "</transactions>";
        System.out.println(xmlParser.processXML(xml));

        Account account = Database.checkAccountIdExistsAndGetIt(123);
        System.out.println(account.getBalance());
        account = Database.checkAccountIdExistsAndGetIt(234);
        System.out.println(account.getBalance());
        account = Database.checkAccountIdExistsAndGetIt(345);
        System.out.println(account.getBalance());
    }

    /*********************** Helper Method **********************************/

    private List<Element> getTransactionList(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(xml);

        ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(input);
        doc.getDocumentElement().normalize();

        XmlParser xmlParser = new XmlParser();
        return xmlParser.parseAndExecuteTransactions(doc, builder.newDocument());
    }


    /**
     * Generate 6 digits random number, used by account
     */
    public String geneRandAccountNum(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 20; i++){
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
