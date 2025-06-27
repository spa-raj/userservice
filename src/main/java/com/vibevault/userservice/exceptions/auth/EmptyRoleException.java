package com.vibevault.userservice.exceptions.auth;

public class EmptyRoleException extends RuntimeException {
    public EmptyRoleException(String message) {
        super(message);
    }
}
