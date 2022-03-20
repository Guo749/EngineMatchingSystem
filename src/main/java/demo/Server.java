package demo;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    /* server socket */
    public ServerSocket serverSocket   = null;

    /* the stream we read from the client */
    public DataInputStream in          =  null;

    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        System.out.println("Server started");

        //initialize the db
        DBHelper dbHelper = new DBHelper();
        DBHelper dbHelper1 = new DBHelper();
    }

    /**
     * Constantly receive connection from the client
     * and read entire request
     */
    public void run(){
        while(true) {
            Socket client = null;
            try {
                /* step1: accept the connection */
                client = this.serverSocket.accept();
                System.out.println("Client accepted");

                /* step2: read the content, both len & message */
                this.in = new DataInputStream(
                    new BufferedInputStream(client.getInputStream()));

                int xmlLen = readLine();

                /* step3: process it accordingly */
                String xml = readNum(xmlLen);

                XmlParser xmlParser = new XmlParser();
                xmlParser.processXML(xml);

                /* step4: write it back the result */
                writeToClient(client, "what I receive " + xmlLen);

            } catch (Exception e){//whatever bad happens, we close and regard it as bad reqeust
                e.printStackTrace();
            } finally {
                if(client != null){
                    try {
                        client.close();
                    } catch (IOException e) {
                        System.out.println("cannot close the client");
                    }
                }
            }
        }
    }

    /**
     * Used to read the first line of XML, the length of the xml
     * if the format is wrong, we throw exception
     *
     * @return the first line of the stream
     */
    private Integer readLine() throws IOException, IllegalArgumentException {
        int c;
        int counter = 0;
        StringBuilder res = new StringBuilder();

        while(true){
            if(counter == 11){
                throw new IllegalArgumentException("bad request, cannot get length");
            }

            c = this.in.read();
            if(c == '\n'){
                break;
            }else if(Character.isDigit(c)){
                char ch = (char) c;
                res.append(ch);
            }else{
                throw new IOException("bad format for length in xml");
            }

            counter++;
        }

        try {
            return Integer.parseInt(res.toString());
        }catch(NumberFormatException e){
            throw new IllegalArgumentException("bad format for content length");
        }
    }

    /**
     * Used to read num bytes from client
     * todo: too many system calls, may improve based on buffer zone
     *
     * @param num # of bytes to read
     * @return the content
     */
    private String readNum(int num) throws IOException {
        int c;
        StringBuilder res = new StringBuilder();

        while(num != 0) {
            c = this.in.read();
            res.append(((char)c));
            num--;
        }

        return res.toString();
    }

    /**
     * Wrapper for write function
     *
     * @param client the client to pass the information
     * @param content the message to deliver
     */
    public void writeToClient(Socket client, String content){
        try {
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());
            dos.writeUTF(content);
        } catch (IOException e) {
            System.out.println("write error");
        }

    }

}
