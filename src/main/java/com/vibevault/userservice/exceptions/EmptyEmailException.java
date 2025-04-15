package com.vibevault.userservice.exceptions;

public class EmptyEmailException extends RuntimeException {
    public EmptyEmailException() {
        super("Email cannot be empty");
    }

    public EmptyEmailException(String message) {
        super(message);
    }

    public EmptyEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyEmailException(Throwable cause) {
        super(cause);
    }
}
