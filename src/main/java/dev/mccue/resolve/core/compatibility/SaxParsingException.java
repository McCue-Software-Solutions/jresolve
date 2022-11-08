package dev.mccue.resolve.core.compatibility;

public class SaxParsingException extends RuntimeException {
    private String message;

    public SaxParsingException(Exception e) {
        this.message = e.getMessage();
    }

    @Override
    public String getMessage() {
        return this.message;
    }
    
}
