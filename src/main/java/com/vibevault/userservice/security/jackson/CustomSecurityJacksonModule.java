package com.vibevault.userservice.security.jackson;

import com.vibevault.userservice.security.CustomGrantedAuthority;
import com.vibevault.userservice.security.CustomUserDetails;
import tools.jackson.databind.module.SimpleModule;

public class CustomSecurityJacksonModule extends SimpleModule {

    /**
     * Creates a Jackson module named after this class for configuring security-related MixIns.
     *
     * The module name is set to the class's fully qualified name.
     */
    public CustomSecurityJacksonModule() {
        super(CustomSecurityJacksonModule.class.getName());
    }

    /**
     * Registers MixIn mappings for security-related types on the provided Jackson setup context.
     * Specifically maps CustomUserDetails to CustomUserDetailsMixin and CustomGrantedAuthority to CustomGrantedAuthorityMixin.
     *
     * @param context the Jackson setup context used to register MixIns
     */
    @Override
    public void setupModule(SetupContext context) {
        context.setMixIn(CustomUserDetails.class, CustomUserDetailsMixin.class);
        context.setMixIn(CustomGrantedAuthority.class, CustomGrantedAuthorityMixin.class);
    }
}