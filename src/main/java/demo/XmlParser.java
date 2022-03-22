package demo;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class XmlParser {

    /* root element is <create> */
    private final String CREATE_TAG = "create";

    /* root element is <transactions> */
    private final String TRANS_TAG = "transactions";

    /**
     * Used to process XML received from client
     *  note the data has not been verified, needs to do that in functions
     *
     * @param xml the xml the client sent
     */
    public void processXML(String xml) throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(xml);

        ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(input);

        doc.getDocumentElement().normalize();
        String rootEle = doc.getDocumentElement().getNodeName();

        //xml is about <create> .... </create>
        if(CREATE_TAG.equals(rootEle)){
            doCreate(doc);
        }else if(TRANS_TAG.equals(rootEle)){// about <transactions> </tran>
            // TODO: Should this be parseAndExecuteTransactions and return the execution results?
            List<Transaction> transactionList = parseTransactions(doc);
        }else{
            throw new IllegalArgumentException("wrong xml template");
        }
    }

    /**
     * Used to handle create request
     *
     * @param doc the DOM object for this request
     * TODO: Not sure whether the processing order matters: If there is a symbol creation for account 123456 (originally
     *            not exist), and account 123456 is created after this symbol creation, the below code will allow it.
     */
    private void doCreate(Document doc) throws IllegalArgumentException{
        NodeList accountList = doc.getElementsByTagName("account");
        NodeList symbolList  = doc.getElementsByTagName("symbol");
        boolean accountExist = false;
        boolean symbolExist  = false;

        /* step1: first to deal with creating account */
        if(accountList != null && accountList.getLength() != 0){
            accountExist = true;
            createAccount(accountList);
        }

        /* step2: check if there is any symbol to manupalate */
        if(symbolList != null && symbolList.getLength() != 0){
            symbolExist = true;
            createSym(symbolList);
        }

        /* step3: check if two are all null*/
        if(!accountExist  && !symbolExist){
            throw new IllegalArgumentException("bad xml template");
        }
    }

    /**
     * This is used to create the corresponding symbol
     * @param symbolList the symbol list to create
     */
    private void createSym(NodeList symbolList) {
        DBHelper dbHelper = new DBHelper();

        for(int i = 0; i < symbolList.getLength(); i++){
            Node node = symbolList.item(i);

            if(node.getNodeType() == Node.ELEMENT_NODE){
                Element symbol = (Element) node;
                String symbolName = symbol.getAttribute("sym");

                NodeList accountList = symbol.getElementsByTagName("account");
                for(int j = 0; j < accountList.getLength(); j++){
                    Node subNode = accountList.item(j);
                    if(subNode.getNodeType() == Node.ELEMENT_NODE){
                        Element detail   = (Element) subNode;
                        String accountId = detail.getAttribute("id");
                        String amount    = detail.getTextContent();

                        String query     = "SELECT * FROM SYMBOL WHERE sym = " + symbolName + ";";
                        String queryRes  = dbHelper.execReadOnlyCommand(query);

                        if(queryRes.length() != 0){
                            throw new IllegalArgumentException("wrong data");
                        }

                        String sql = "INSERT INTO SYMBOL () + VALUES () ";
                        boolean res = dbHelper.execNotReadOnlyCommand(sql);
                        if(!res){
                            throw new IllegalArgumentException("wrong data");
                        }else{
                            System.out.println("create successfully");
                        }
                    }
                }
            }
        }

        dbHelper.garbageCollection();
    }

    /**
     * Used to create account, first to check if the account is exist
     * todo: verify the input for accountID and balance
     * @param accountList the account list to create
     */
    private void createAccount(NodeList accountList){
        DBHelper dbHelper = new DBHelper();
        for(int i = 0; i < accountList.getLength(); i++){
            Node node = accountList.item(i);

            if(node.getNodeType() == Node.ELEMENT_NODE){
                Element account = (Element)node;
                String accountId = account.getAttribute("id");
                int balance   = Integer.parseInt(account.getAttribute("balance"));

                System.out.println("account is " + accountId + " " + balance);


                String queryAccount = "SELECT * FROM ACCOUNT WHERE ID = " + accountId;
                String query = dbHelper.execReadOnlyCommand(queryAccount);

                if(query.length() == 0){
                    throw new IllegalArgumentException("account already exist");
                }

                String createAccount = "INSERT INTO ACCOUNT (ID, BALANCE)" +
                    "VALUES (" + accountId + ", " + balance + ");";
                boolean res = dbHelper.execNotReadOnlyCommand(createAccount);

                if(!res){
                    throw new IllegalArgumentException("wrong execution");
                }
            }
        }

        dbHelper.garbageCollection();
    }

    /**
     * Parse transactions
     * @param doc is the DOM object for this request
     */
    public List<Transaction> parseTransactions(Document doc) {
        System.out.println("Transactions found");
        Element element = doc.getDocumentElement();
        // TODO: Go to the database to check whether the account id exists
        String accountID = checkHasAttributeAndGetIt(element, "id");

        NodeList childNodes = element.getChildNodes();
        List<Transaction> transactionList = new ArrayList<Transaction>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                Transaction transaction = null;
                switch (childElement.getNodeName()) {
                    case "order" -> transaction = parseOrderTransaction(accountID, childElement);
                    case "query" -> transaction = parseQueryTransaction(accountID, childElement);
                    case "cancel" -> transaction = parseCancelTransaction(accountID, childElement);
                    default -> throw new IllegalArgumentException("Transaction type " + childElement.getNodeName() + " is invalid");
                }
                transactionList.add(transaction);
                // TODO: Should this part (execute transactions) be put into the Server class - run method? But
                //      how to return error messages in order if it is in the Server class?
                // TODO: Perhaps execute can return the result
                transaction.execute();
            }
        }

        if (transactionList.size() <= 0) {
            throw new IllegalArgumentException("There must be at least one child inside the transactions tag");
        }
        return transactionList;
    }

    private OrderTransaction parseOrderTransaction(String accountId, Element element) {
        String sym = checkHasAttributeAndGetIt(element, "sym");
        String amountStr = checkHasAttributeAndGetIt(element, "amount");
        String limitStr = checkHasAttributeAndGetIt(element, "limit");
        try {
            double amount = Float.parseFloat(amountStr);
            double limit = Float.parseFloat(limitStr);
            return new OrderTransaction(accountId, sym, amount, limit);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("amount or limit value is invalid");
        }
    }

    private QueryTransaction parseQueryTransaction(String accountId, Element element) {
        return new QueryTransaction(accountId);
    }

    private CancelTransaction parseCancelTransaction(String accountId, Element element) {
        return new CancelTransaction(accountId);
    }

    private String checkHasAttributeAndGetIt(Element element, String attribute) {
        if (element.hasAttribute(attribute)) {
            return element.getAttribute(attribute);
        }
        else {
            throw new IllegalArgumentException("Attribute " + attribute + " expected");
        }
    }
}
