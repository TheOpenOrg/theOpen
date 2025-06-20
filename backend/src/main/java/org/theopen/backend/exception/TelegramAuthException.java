package org.theopen.backend.exception;

public class TelegramAuthException extends RuntimeException {
    public TelegramAuthException(String message) {
        super(message);
    }
}
