package com.vibevault.userservice.exceptions;

public class InvalidCredentialsExcpetion extends RuntimeException {
    public InvalidCredentialsExcpetion(String message) {
        super(message);
    }
    public InvalidCredentialsExcpetion(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidCredentialsExcpetion(Throwable cause) {
        super(cause);
    }
    public InvalidCredentialsExcpetion() {
        super("Invalid credentials");
    }
}
