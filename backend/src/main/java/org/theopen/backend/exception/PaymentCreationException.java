package org.theopen.backend.exception;

public class PaymentCreationException extends RuntimeException {
    public PaymentCreationException() {
        super("Failed to create payment");
    }
}

