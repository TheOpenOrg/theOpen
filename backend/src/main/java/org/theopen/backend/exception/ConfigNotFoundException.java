package org.theopen.backend.exception;

public class ConfigNotFoundException extends RuntimeException {
    public ConfigNotFoundException() {
        super("Config not found");
    }
}

