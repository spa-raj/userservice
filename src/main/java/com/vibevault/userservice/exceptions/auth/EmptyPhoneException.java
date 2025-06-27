package com.vibevault.userservice.exceptions.auth;

public class EmptyPhoneException extends RuntimeException {
    public EmptyPhoneException(String message) {
        super(message);
    }
    public EmptyPhoneException(String message, Throwable cause) {
        super(message, cause);
    }
    public EmptyPhoneException(Throwable cause) {
        super(cause);
    }
    public EmptyPhoneException() {
        super("Phone number cannot be empty");
    }
}
