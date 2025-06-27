package com.vibevault.userservice.exceptions.auth;

public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException(String message) {
        super(message);
    }
    public PhoneAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    public PhoneAlreadyExistsException(Throwable cause) {
        super(cause);
    }
    public PhoneAlreadyExistsException() {
        super("Phone number already exists");
    }
}
