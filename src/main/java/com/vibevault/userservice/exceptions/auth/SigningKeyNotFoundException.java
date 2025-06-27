package com.vibevault.userservice.exceptions.auth;

public class SigningKeyNotFoundException extends RuntimeException {
    public SigningKeyNotFoundException(String message) {
        super(message);
    }
}
