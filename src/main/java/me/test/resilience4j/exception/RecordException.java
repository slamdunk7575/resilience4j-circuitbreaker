package me.test.resilience4j.exception;

public class RecordException extends RuntimeException {

    public RecordException(String message) {
        super(message);
    }
}
