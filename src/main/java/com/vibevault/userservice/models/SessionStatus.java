package com.vibevault.userservice.models;

public enum SessionStatus {
    ACTIVE,
    INACTIVE,
    EXPIRED,
    BLACKLISTED,
    LOGGED_OUT;

    @Override
    public String toString() {
        return name();
    }

}
