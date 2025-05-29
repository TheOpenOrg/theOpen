package org.theopen.backend.exception;

public class TrialAlreadyUsedException extends RuntimeException {
    public TrialAlreadyUsedException() {
        super("Trial already used");
    }
}

