package com.vibevault.userservice.configurations;

import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;
import com.vibevault.userservice.repositories.RoleRepository;
import com.vibevault.userservice.repositories.UserRepository;
import com.vibevault.userservice.repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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
        // Create admin role if it doesn't exist
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ADMIN");
                    return roleRepository.save(newRole);
                });

        // Check if any admin users exist
        if (userRoleRepository.countUserRoleByRole_Id(adminRole.getId()) == 0) {
            // No admins exist, create the first admin user
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

            // Assign admin role to user
            UserRole userRole = new UserRole();
            userRole.setUser(adminUser);
            userRole.setRole(adminRole);
            userRole.setAssignedAt(new Date());
            userRoleRepository.save(userRole);
        }

        // Initialize OAuth2 client if it doesn't exist
        initializeOAuth2Client();
    }

    private void initializeOAuth2Client() {
        if (registeredClientRepository.findByClientId(oauth2ClientId) != null) {
            return; // Client already exists
        }

        String clientSecret = oauth2ClientSecret != null
                ? oauth2ClientSecret
                : UUID.randomUUID().toString();

        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(oauth2ClientId.trim())
                .clientIdIssuedAt(Instant.now())
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientName(oauth2ClientId.trim())
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

        // Log the generated secret if it was auto-generated
        if (oauth2ClientSecret == null) {
            System.out.println("=".repeat(60));
            System.out.println("OAuth2 Client Created:");
            System.out.println("  Client ID: " + oauth2ClientId);
            System.out.println("  Client Secret: " + clientSecret);
            System.out.println("  (Set oauth2.client.secret in application.properties to use a fixed secret)");
            System.out.println("=".repeat(60));
        }
    }
}