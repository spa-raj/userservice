package com.vibevault.userservice.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    public EmailAlreadyExistsException(Throwable cause) {
        super(cause);
    }
    public EmailAlreadyExistsException() {
        super("Email already exists");
    }
}
