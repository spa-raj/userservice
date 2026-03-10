package com.vibevault.userservice.configurations;

import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;
import com.vibevault.userservice.repositories.RoleRepository;
import com.vibevault.userservice.repositories.UserRepository;
import com.vibevault.userservice.repositories.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.firstname}")
    private String adminFirstName;

    @Value("${admin.lastname}")
    private String adminLastName;

    @Value("${oauth2.client.id:vibevault-client}")
    private String oauth2ClientId;

    @Value("${oauth2.client.secret:#{null}}")
    private String oauth2ClientSecret;

    @Value("${oauth2.client.redirect-uri:http://127.0.0.1:8080/login/oauth2/code/vibevault}")
    private String oauth2RedirectUri;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisteredClientRepository registeredClientRepository;

    @Autowired
    public DatabaseInitializer(UserRepository userRepository,
                               RoleRepository roleRepository,
                               UserRoleRepository userRoleRepository,
                               PasswordEncoder passwordEncoder,
                               RegisteredClientRepository registeredClientRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public void run(String... args) {
        Role adminRole = initializeAdminRole();
        initializeAdminUser(adminRole);
        initializeOAuth2Client();
    }

    private Role initializeAdminRole() {
        try {
            return roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName("ADMIN");
                        return roleRepository.save(newRole);
                    });
        } catch (DataIntegrityViolationException e) {
            log.warn("ADMIN role creation conflict (likely another pod) — fetching existing", e);
            return roleRepository.findByName("ADMIN").orElseThrow();
        }
    }

    private void initializeAdminUser(Role adminRole) {
        if (userRoleRepository.countUserRoleByRole_Id(adminRole.getId()) == 0) {
            try {
                Optional<User> existingUser = userRepository.findUserByEmail(adminEmail);
                User adminUser;

                if (existingUser.isPresent()) {
                    adminUser = existingUser.get();
                } else {
                    adminUser = new User();
                    adminUser.setEmail(adminEmail);
                    adminUser.setPassword(passwordEncoder.encode(adminPassword));
                    adminUser.setFirstName(adminFirstName);
                    adminUser.setLastName(adminLastName);
                    adminUser = userRepository.save(adminUser);
                }

                UserRole userRole = new UserRole();
                userRole.setUser(adminUser);
                userRole.setRole(adminRole);
                userRole.setAssignedAt(new Date());
                userRoleRepository.save(userRole);
            } catch (DataIntegrityViolationException e) {
                log.warn("Admin user/role assignment conflict (likely another pod)", e);
            }
        }
    }

    private void initializeOAuth2Client() {
        String trimmedClientId = oauth2ClientId.trim();
        if (registeredClientRepository.findByClientId(trimmedClientId) != null) {
            return;
        }

        try {
            String clientSecret = oauth2ClientSecret != null
                    ? oauth2ClientSecret
                    : UUID.randomUUID().toString();

            RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(trimmedClientId)
                    .clientIdIssuedAt(Instant.now())
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .clientName(trimmedClientId)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .redirectUri(oauth2RedirectUri.trim())
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope(OidcScopes.EMAIL)
                    .scope("read")
                    .scope("write")
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(true)
                            .requireProofKey(false)
                            .build())
                    .build();

            registeredClientRepository.save(registeredClient);
        } catch (DataIntegrityViolationException e) {
            log.warn("OAuth2 client registration conflict (likely another pod)", e);
        }
    }
}