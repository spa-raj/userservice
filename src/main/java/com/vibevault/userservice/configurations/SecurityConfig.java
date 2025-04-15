package com.vibevault.userservice.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // allow all requests to the application
        http.authorizeHttpRequests((request) -> {
                    try {
                        request
                                .anyRequest().permitAll()
                                .and().csrf().disable()
                                .cors().disable();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return http.build();
    }
}
