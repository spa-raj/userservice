package com.vibevault.userservice.services;

import java.util.HashSet;
import java.util.Set;

public class Consts {
    public static final int MIN_PASSWORD_LENGTH = 10;
    public static final int JWT_EXPIRATION_TIME = 86400000; // 1 day in milliseconds
    public static final String JWT_ISSUER = "vibevault";
    public static final String JWT_AUDIENCE = "vibevault-userservice: auth";

}
