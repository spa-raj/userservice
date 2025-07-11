package com.vibevault.userservice;

import com.vibevault.userservice.security.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.time.Instant;
import java.util.UUID;

@SpringBootApplication
public class UserserviceApplication {

    private static RegisteredClientRepository registeredClientRepository;
    private static PasswordEncoder passwordEncoder;

    @Autowired
    public UserserviceApplication(RegisteredClientRepository registeredClientRepository,
                                  PasswordEncoder passwordEncoder) {
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static void addSampleRegisteredClient(){
        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("oidc-client")
                .clientSecret(passwordEncoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://oauth.pstmn.io/v1/browser-callback")
                .postLogoutRedirectUri("http://127.0.0.1:8080/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .clientIdIssuedAt(Instant.now())
                .build();
        registeredClientRepository.save(oidcClient);
    }
    public static void main(String[] args) {
        SpringApplication.run(UserserviceApplication.class, args);
        UserserviceApplication.addSampleRegisteredClient();
    }

}
