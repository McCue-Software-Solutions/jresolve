package dev.mccue.resolve.util;

public interface SaxHandler {
    public void startElement(String tagName);
    public void characters(Character[] ch, int start, int length);
    public void endElement(String tagName);
}
