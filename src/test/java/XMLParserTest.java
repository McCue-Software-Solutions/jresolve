import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;

class XMLParserTest {
    Document doc;

    XMLParserTest() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = dBuilder.parse(new File("src/test/files/response.xml"));
    }

    @Test
    void extractXMLVersion() {
        System.out.println(doc.getXmlVersion());
    }

    @Test
    void extractStringTextContent() {
        NodeList l = doc.getElementsByTagName("str");
        for (int i = 0; i < l.getLength(); i++) {
            Node node = l.item(i);
            System.out.println(node.getTextContent());
        }
    }
}