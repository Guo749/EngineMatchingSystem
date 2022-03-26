package demo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Worker implements Runnable{
    /* since int has only 10 digits, if reach 10, report error */
    private static final int MAX_INT_DIGIT = 10;

    /* client socket */
    private Socket client;

    /* server socket */
    public ServerSocket serverSocket   = null;

    /* the stream we read from the client */
    public DataInputStream in          =  null;

    public Worker(Socket client, ServerSocket ss){
        this.serverSocket = ss;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            /* step2: read the content, both len & message */
            this.in = new DataInputStream(
                new BufferedInputStream(client.getInputStream()));

            int xmlLen = readLine();

            /* step3: process it accordingly */
            String xml = readNum(xmlLen);

            XmlParser xmlParser = new XmlParser();
            String reply = xmlParser.processXML(xml);
            StringBuilder sb = new StringBuilder();
            sb.append(reply.length()).append("\n").append(reply);

            /* step4: write it back the result */
            writeToClient(client, sb.toString());
            System.out.println("write back to the client ");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(this.in != null)
                    this.in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(this.client != null)
                    this.client.close();
            } catch (IOException e) {
                e.printStackTrace();
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
            if(counter == MAX_INT_DIGIT){
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
