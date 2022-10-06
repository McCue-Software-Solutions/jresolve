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

import static org.junit.jupiter.api.Assertions.*;

class XMLParserTest {
    Document doc;

    XMLParserTest() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = dBuilder.parse(new File("src/test/files/response.xml"));
    }

    @Test
    void extractXMLVersion() {
        assertEquals(doc.getXmlVersion(), "1.0");
    }

    @Test
    void extractFirstStrNode() {
        Node first = doc.getElementsByTagName("str").item(0);
        assertEquals(first.getTextContent(), "guice");
        assertEquals(first.getNodeName(), "str");
        assertEquals(first.getAttributes().getNamedItem("name").getNodeValue(), "q");
    }
}