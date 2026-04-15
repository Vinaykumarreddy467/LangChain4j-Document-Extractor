package com.vinay.docextractor.parser;

/**
 * Thrown when a document type has no registered parser.
 */
public class UnsupportedDocumentTypeException extends RuntimeException {

    public UnsupportedDocumentTypeException(String message) {
        super(message);
    }

    public UnsupportedDocumentTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
