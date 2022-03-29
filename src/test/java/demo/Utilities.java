package demo;


import javafx.util.Pair;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Used to generate some info
 */
public class Utilities {
    private final static int WORK_LOAD = 5;

    @Test
    public void testMain() throws IOException {
        for(int i = 0; i < WORK_LOAD; i++) {
            String msg = generateWorkLoad();
            writeToFileWithLen(msg, i);
        }
    }

    private void writeToFileWithLen(String msg, int seq) throws IOException {
        int len = msg.length();

        FileWriter fw = new FileWriter("./src/test/java/demo/msg" + seq + ".txt");
        fw.write(len + "\n");
        fw.write(msg);
        fw.close();
    }

    private String generateWorkLoad(){
        StringBuilder sb = new StringBuilder();
        XmlParserTest xpt = new XmlParserTest();
        List<Pair<String, String>> accounts            = xpt.generateSymList(WORK_LOAD);
        List<String> createClauses       = new ArrayList<>();
        List<String> createSymbolClauses = new ArrayList<>();

        for(int i = 0; i < WORK_LOAD; i++){
            String cc = xpt.geneCreateAccountClause(accounts.get(i).getKey(), accounts.get(i).getValue());
            createClauses.add(cc);

            String sym = (i % 2 == 0) ? "BTC" : "ETC";

            String sc = xpt.geneCreateSymClause(sym, accounts);
            createSymbolClauses.add(sc);
        }

        sb.append("<create>\n");

        for(String s : createClauses){
            sb.append(s);
        }

        for(String s : createSymbolClauses){
            sb.append(s);
        }

        sb.append("</create>\n");

        return sb.toString();
    }


}
