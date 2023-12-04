package me.test.resilience4j.exception;

public class IgnoreException extends RuntimeException {

    public IgnoreException(String message) {
        super(message);
    }
}
