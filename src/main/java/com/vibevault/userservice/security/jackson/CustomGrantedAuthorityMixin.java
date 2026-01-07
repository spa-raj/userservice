package com.vibevault.userservice.security.jackson;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = CustomGrantedAuthorityDeserializer.class)
public abstract class CustomGrantedAuthorityMixin {
}
