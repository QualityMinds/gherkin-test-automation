package de.qualityminds.gta.steps.rest.exceptions;


public class ResponseMappingException extends Exception {
    public ResponseMappingException(String message) {
        super(message);
    }

    public ResponseMappingException(String message, Throwable e) {
        super(message, e);
    }
}
