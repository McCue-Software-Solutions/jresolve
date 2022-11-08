package dev.mccue.resolve.core.compatibility;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import dev.mccue.resolve.maven.PomParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import dev.mccue.resolve.util.Tuple2;

public final class Utilities {
    private static String utf8Bom = "\ufeff";

    private static Optional<Tuple2<Integer, Integer>> entityIdxNoFrom(String s) {
        return entityIdx(s, 0);
    }

    private static Optional<Tuple2<Integer, Integer>> entityIdxFrom(String s, int fromIdx) {
        return entityIdx(s, fromIdx);
    }

    private static Optional<Tuple2<Integer, Integer>> entityIdx(String s, int fromIdx) {
        var i = fromIdx;
        Optional<Tuple2<Integer, Integer>> found = Optional.empty();
        while (found.isEmpty() && i < s.length()) {
            if (s.charAt(i) == '&') {
                var start = i;
                i += 1;
                var isAlpha = true;
                while (isAlpha && i < s.length()) {
                    var c = s.charAt(i);
                    if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z'))
                        isAlpha = false;
                    else
                        i += 1;
                }
                if (start + 1 < i && i < s.length()) {
                    assert (!isAlpha);
                    if (s.charAt(i) == ';') {
                        i += 1;
                        found = Optional.ofNullable(new Tuple2(start, i)); // what is this warning
                    }
                }
            } else
                i += 1;
        }
        return found;
    }

    private static String substituteEntities(String s) {
        var b = new StringBuilder();
        var a = s.toCharArray();

        var i = 0;
        var j = 0;
        while (j < s.length() && j < utf8Bom.length() && s.charAt(i) == utf8Bom.charAt(j)) {
            j += 1;
        }

        if (j == utf8Bom.length()) {
            i = j;
        }

        Optional<Tuple2<Integer, Integer>> found = Optional.empty();
        while (found.isPresent()) {
            var from = found.get().first();
            var to = found.get().second();

            b.append(a, i, from - i);

            var name = s.substring(from, to);
            var replacement = Entities.ENTITIES.getOrDefault(name, name);

            b.append(replacement);

            i = to;

            found = entityIdxFrom(s, i);
        }

        if (i == 0) {
            return s;
        } else {
            return b.append(a, i, s.length() - i).toString();
        }
    }

    protected static String xmlPreprocess(String s) {
        return substituteEntities(s);
    }

    private final static class XmlHandler extends DefaultHandler { // TODO PomParser -> SaxHandler
        private ContentHandler handler;

        public XmlHandler(ContentHandler handler) {
            this.handler = handler;
        }

        public void startElement(
                String uri,
                String localName,
                String qName,
                Attributes attributes) {
            try {
                handler.startElement(uri, localName, qName, attributes);
            } catch (SAXException e) {
                throw new SaxParsingException(e);
            }
        }

        public void characters(char[] ch, int start, int length) {
            try {
                handler.characters(ch, start, length);
            } catch (SAXException e) {
                throw new SaxParsingException(e);
            }
        }

        public void endElement(String uri, String localName, String qName) {
            try {
                handler.endElement(uri, localName, qName);
            } catch (SAXException e) {
                throw new SaxParsingException(e);
            }
        }
    }

    private static SAXParserFactory setSPF() {
        var spf0 = SAXParserFactory.newInstance();
        spf0.setNamespaceAware(false);
        return spf0;
    }

    private static SAXParserFactory spf = setSPF();

    public static <T extends ContentHandler> T xmlParseSax(String str, T handler) { // TODO this
                                                                         // PomParser thing
                                                                         // needs to be fixed
        var str0 = xmlPreprocess(str);
        try {
            var saxParser = spf.newSAXParser();
            var xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(new XmlHandler(handler));
            xmlReader.parse(new InputSource(new CharArrayReader(str0.toCharArray())));
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new SaxParsingException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return handler;
    }
}
