package org.theopen.backend.exception;

public class InactiveConfigException extends RuntimeException {
    public InactiveConfigException() {
        super("Inactive config");
    }
}

