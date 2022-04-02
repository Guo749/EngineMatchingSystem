package demo;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
     * @return the execute result
     */
    public String processXML(String xml) throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(xml);

        ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(input);
        Document resultsDoc = builder.newDocument();

        doc.getDocumentElement().normalize();
        String rootEle = doc.getDocumentElement().getNodeName();

        //xml is about <create> .... </create>
        if(CREATE_TAG.equals(rootEle)){
            List<Command> actions = parseCreate(doc);
            if(actions.size() == 0){
                throw new IllegalArgumentException("wrong template with no request");
            }

            CreateOrderTransaction cot = new CreateOrderTransaction(actions);
            cot.execute(resultsDoc);
            return formCreateReply(actions);
        }else if(TRANS_TAG.equals(rootEle)){// about <transactions> </tran>
            List<Element> resultList = parseAndExecuteTransactions(doc, resultsDoc);
            String resultString = createResultsReply(resultsDoc, resultList);
//            System.out.println(resultString);
            return resultString;
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
    private List<Command> parseCreate(Document doc) throws IllegalArgumentException{
        List<Command> actions = new ArrayList<>();

        /* step1: parse xml into action list */
        Element  docEle = doc.getDocumentElement();
        NodeList nl     = docEle.getChildNodes();
        int      len    = nl.getLength();

        for(int i = 0; i < len; i++){
            if(nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nl.item(i);
                if(el.getNodeName().contains("account")){
                    String accountNum = el.getAttribute("id");
                    int balance    = Integer.parseInt(el.getAttribute("balance"));
                    actions.add(new CreateAccount(new Account(balance, accountNum)));

                }else if(el.getNodeName().contains("symbol")){
                    NodeList childNodes = el.getChildNodes();
                    String sym = el.getAttribute("sym");
                    for(int j = 0; j < childNodes.getLength(); j++){
                        if(childNodes.item(j).getNodeType() == Node.ELEMENT_NODE){
                            Element acco = (Element) childNodes.item(j);
                            if(acco.getNodeName().contains("account")){
                                String accountNum = acco.getAttribute("id");
                                int share    = Integer.parseInt(acco.getTextContent());
                                actions.add(new PutSymbol(new Account(0, accountNum), sym,share));
                            }
                        }
                    }

                }
            }
        }

        /* step2: do the transaction */
        return actions;
    }

    /**
     * Parse transactions
     * @param doc is the DOM object for this request
     */
    public List<Element> parseAndExecuteTransactions(Document doc, Document results) {
        List<Element> resultList = new ArrayList<>();
        Element element = doc.getDocumentElement();
        Integer accountID = null;
        Account account = null;
        String accountIdErrorMsg = null;
        try {
            String accountIDStr = checkHasAttributeAndGetIt(element, "id");
            accountID = Integer.parseInt(accountIDStr);
            account = Database.checkAccountIdExistsAndGetIt(accountID);
        }
        catch (Exception e) {
            accountIdErrorMsg = "There is no account ID or account ID is invalid";
        }

        NodeList childNodes = element.getChildNodes();
        if (childNodes.getLength() <= 0) {
            resultList.add(createParsingErrorReply(results, "There must be at least one transaction"));
            return resultList;
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            try {
                if (accountIdErrorMsg != null) {
                    throw new IllegalArgumentException(accountIdErrorMsg);
                }
                Element childElement = (Element) childNode;
                Transaction transaction = null;
                switch (childElement.getNodeName()) {
                    case "order" -> transaction = parseOrderTransaction(account, childElement);
                    case "query" -> transaction = parseQueryTransaction(accountID, childElement);
                    case "cancel" -> transaction = parseCancelTransaction(accountID, childElement);
                    default -> throw new IllegalArgumentException("Transaction type " + childElement.getNodeName() + " is invalid");
                }
                resultList.add(transaction.execute(results));
            }
            catch (Exception e) {
                resultList.add(createParsingErrorReply(results, e.getMessage()));
            }
        }
        return resultList;
    }

    private OrderTransaction parseOrderTransaction(Account account, Element element) {
        String sym = checkHasAttributeAndGetIt(element, "sym");
        String amountStr = checkHasAttributeAndGetIt(element, "amount");
        String limitStr = checkHasAttributeAndGetIt(element, "limit");
        try {
            double amount = Double.parseDouble(amountStr);
            double limit = Double.parseDouble(limitStr);
            return new OrderTransaction(account, sym, amount, limit);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid amount or limit value");
        }
    }

    private QueryTransaction parseQueryTransaction(int accountId, Element element) {
        String transactionIdStr = checkHasAttributeAndGetIt(element, "id");
        try {
            int transactionId = Integer.parseInt(transactionIdStr);
            return new QueryTransaction(accountId, transactionId);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid transaction id");
        }
    }

    private CancelTransaction parseCancelTransaction(int accountId, Element element) {
        String transactionIdStr = checkHasAttributeAndGetIt(element, "id");
        try {
            int transactionId = Integer.parseInt(transactionIdStr);
            return new CancelTransaction(accountId, transactionId);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid transaction id");
        }
    }

    private String checkHasAttributeAndGetIt(Element element, String attribute) {
        if (element.hasAttribute(attribute)) {
            return element.getAttribute(attribute);
        }
        else {
            throw new IllegalArgumentException("Attribute " + attribute + " expected");
        }
    }

    /**
     * based on the command, if it has executed successfully
     * we form the concrete reply
     *
     * @param actions command list
     * @return the string form of reply
     */
    public String formCreateReply(List<Command> actions) {
        StringBuilder res = new StringBuilder();
        res.append("<results>\n");
        for(Command command : actions){
            if(command instanceof CreateAccount){
                CreateAccount ca = (CreateAccount) command;
                if(ca.successfulExecute){
                    res.append("<created id=\"").append(ca.account.getAccountNum()).append("\"/>\n");
                }else{
                    res.append("<error id=\"").append(ca.account.getAccountNum()).append("\">").append("Account already exists").append("</error>\n");
                }
            }else{
                PutSymbol ps = (PutSymbol) command;
                if(ps.successfulExecute){
                    res.append("<created sym=\"").append(ps.symbol).append("\" id=\"").append(ps.account.getAccountNum()).append("\"/>\n");
                }else{
                    res.append("<error sym=\"").append(ps.symbol).append("\" id=\"").append(ps.account.getAccountNum()).append("\">").append("ACCOUNT DOES NOT EXIST").append("</error>\n");
                }
            }
        }
        res.append("</results>\n");

//        System.out.println(res.toString());

        return res.toString();
    }

    public Element createParsingErrorReply(Document results, String errorMsg) {
        Element errorReply = results.createElement("error");
        errorReply.appendChild(results.createTextNode("Parsing Error: " + errorMsg));
        return errorReply;
    }

    public String createResultsReply(Document results, List<Element> resultList) {
        try {
            Element rootElement = results.createElement("results");
            results.appendChild(rootElement);
            for (Element resultElement : resultList) {
                rootElement.appendChild(resultElement);
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(results), new StreamResult(writer));
//            return DOMUtils.prettyPrint(results);
            return writer.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "<error>Cannot create a results reply</error>\n";
    }
}
