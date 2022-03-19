package demo;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;

public class XmlParser {
    private final String CREATE_TAG = "create";
    private final String TRANS_TAG = "transactions";

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

        }else{
            throw new IllegalArgumentException("wrong xml template");
        }
    }

    /**
     * Used to handle create request
     *
     * @param doc the DOM object for this request
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
}
