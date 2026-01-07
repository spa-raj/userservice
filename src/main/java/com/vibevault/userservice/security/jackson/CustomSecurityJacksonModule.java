package com.vibevault.userservice.security.jackson;

import com.vibevault.userservice.security.CustomGrantedAuthority;
import com.vibevault.userservice.security.CustomUserDetails;
import tools.jackson.databind.module.SimpleModule;

public class CustomSecurityJacksonModule extends SimpleModule {

    public CustomSecurityJacksonModule() {
        super(CustomSecurityJacksonModule.class.getName());
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixIn(CustomUserDetails.class, CustomUserDetailsMixin.class);
        context.setMixIn(CustomGrantedAuthority.class, CustomGrantedAuthorityMixin.class);
    }
}
