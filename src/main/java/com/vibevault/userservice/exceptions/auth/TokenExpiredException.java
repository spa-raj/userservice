package com.vibevault.userservice.exceptions.auth;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
    public TokenExpiredException(Throwable cause) {
        super(cause);
    }
}
