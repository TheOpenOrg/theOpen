package org.theopen.backend.exception;

public class ServerApiException extends RuntimeException {
    public ServerApiException(String message) {
        super(message);
    }
}
