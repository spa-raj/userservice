package com.vibevault.userservice.exceptions;

public class EmptyPasswordException extends RuntimeException {
    public EmptyPasswordException() {
        super("Password cannot be empty");
    }

    public EmptyPasswordException(String message) {
        super(message);
    }

    public EmptyPasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyPasswordException(Throwable cause) {
        super(cause);
    }
}
