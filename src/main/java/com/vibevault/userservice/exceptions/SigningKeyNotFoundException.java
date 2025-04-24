package com.vibevault.userservice.exceptions;

public class SigningKeyNotFoundException extends RuntimeException {
    public SigningKeyNotFoundException(String message) {
        super(message);
    }
}
