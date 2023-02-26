package dev.mccue.resolve.maven;

public class ModelParseException extends Throwable{
    private final String message;

    public ModelParseException(String message) {
        this.message = message;
    }
    
}
