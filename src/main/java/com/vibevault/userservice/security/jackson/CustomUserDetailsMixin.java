package com.vibevault.userservice.security.jackson;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = CustomUserDetailsDeserializer.class)
public abstract class CustomUserDetailsMixin {
}
