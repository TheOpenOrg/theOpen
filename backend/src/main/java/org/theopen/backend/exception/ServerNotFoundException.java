package org.theopen.backend.exception;

public class ServerNotFoundException extends RuntimeException {
    public ServerNotFoundException() {
        super("Server not found");
    }
}

