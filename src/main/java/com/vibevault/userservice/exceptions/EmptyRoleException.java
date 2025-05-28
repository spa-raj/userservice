package com.vibevault.userservice.exceptions;

public class EmptyRoleException extends RuntimeException {
    public EmptyRoleException(String message) {
        super(message);
    }
}
