package me.test.resilience4j.exception;

public class RetryException extends RuntimeException {
    public RetryException(String message) {
        super(message);
    }
}
