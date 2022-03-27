package demo;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


/**
 * Fake Client Side, when testing
 * instantiate one and send request
 */
public class Client {
    private final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";


    public Socket socket;

    private BufferedReader br;

    private OutputStream os;

    private final static int MAX_INT_DIGIT = 11;

    public Client(String file) throws IOException {
        this.socket = new Socket("localhost", 12345);

        System.out.println("connection establish");

        this.br = new BufferedReader(new StringReader(file));
        this.os = this.socket.getOutputStream();
    }

    public void sendXMLAndGetReply() throws IOException {
        //step1: form the request
        StringBuilder content = new StringBuilder();
        content.append(xmlHeader);
        content.append("<create>\n");
        char[] buffer = new char[1024];
        while(this.br.ready()){
            int len = this.br.read(buffer);
            if(len == -1)
                break;
            content.append(buffer, 0, len);
        }
        content.append("</create>");


        int len = content.length();
        String request = len + "\n" + content;

        //step2: write it to the client
        this.os.write(request.getBytes(StandardCharsets.UTF_8));

        //step3: read it
        String data = readFromServer();

        System.out.println("----- client: what we receive -----");
        System.out.println(data);
    }

    private String readFromServer() throws IOException {
        StringBuilder res = new StringBuilder();
        InputStream is = this.socket.getInputStream();
        byte[] bytes   = new byte[1024];
        int len;

        while((len = is.read(bytes)) != -1){
            res.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
        }

        return res.toString();
    }
}