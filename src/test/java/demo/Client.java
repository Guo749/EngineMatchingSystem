package demo;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


/**
 * Fake Client Side, when testing
 * instantiate one and send request
 */
public class Client {
    private final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";


    public Socket socket;

    private BufferedReader br;

    private DataOutputStream dos;

    private final static int MAX_INT_DIGIT = 11;

    public Client(String file) throws IOException {
        this.socket = new Socket("localhost", 12345);

        System.out.println("connection establish");

        this.br = new BufferedReader(new StringReader(file));
        this.dos = new DataOutputStream(this.socket.getOutputStream());
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

        System.out.println(request);
        //step2: write it to the client
        this.dos.writeUTF(request);

        //step3: read it
        String data = readFromServer();
        System.out.println("what we receive + -------");
        System.out.println(data);
    }

    private String readFromServer() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        char[] buffer = new char[8192];
        StringBuilder res = new StringBuilder();
        while(br.ready()){
            int len = br.read(buffer, 0, 8192);
            res.append(buffer, 0, len);
        }

        return res.toString();
    }
}