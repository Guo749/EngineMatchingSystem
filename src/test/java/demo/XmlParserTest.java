package demo;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class XmlParserTest {
    @Test
    public void testTopLevel() throws ParserConfigurationException, IOException, SAXException {
        String xml = "<?xml version = \"1.0\"?> <create> </create>";

        XmlParser xmlParser = new XmlParser();
        xmlParser.processXML(xml);
    }
}
