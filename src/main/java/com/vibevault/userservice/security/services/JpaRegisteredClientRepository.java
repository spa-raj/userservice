package com.vibevault.userservice.security.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vibevault.userservice.security.jackson.CustomSecurityJacksonModule;
import com.vibevault.userservice.security.models.Client;
import com.vibevault.userservice.security.repositories.ClientRepository;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Component
public class JpaRegisteredClientRepository implements RegisteredClientRepository {
    private final ClientRepository clientRepository;
    private final JsonMapper jsonMapper;

    /**
     * Create a JpaRegisteredClientRepository backed by the given ClientRepository and initialize
     * a JsonMapper configured for secure polymorphic (de)serialization with security and OAuth2 modules.
     *
     * @param clientRepository repository used to persist and retrieve client entities
     */
    public JpaRegisteredClientRepository(ClientRepository clientRepository) {
        Assert.notNull(clientRepository, "clientRepository cannot be null");
        this.clientRepository = clientRepository;

        ClassLoader classLoader = JpaRegisteredClientRepository.class.getClassLoader();
        // Configure validator to allow all application and Spring Security types
        BasicPolymorphicTypeValidator.Builder validatorBuilder = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubTypeIsArray()
                .allowIfSubType("com.vibevault.userservice.")
                .allowIfSubType("org.springframework.security.")
                .allowIfSubType("java.");

        this.jsonMapper = JsonMapper.builder()
                .addModules(SecurityJacksonModules.getModules(classLoader, validatorBuilder))
                .addModule(new OAuth2AuthorizationServerJacksonModule())
                .addModule(new CustomSecurityJacksonModule())
                .build();
    }

    /**
     * Persists the given RegisteredClient to the backing ClientRepository.
     *
     * @param registeredClient the client to persist; must not be null
     * @throws IllegalArgumentException if {@code registeredClient} is null
     */
    @Override
    public void save(RegisteredClient registeredClient) {
        Assert.notNull(registeredClient, "registeredClient cannot be null");
        this.clientRepository.save(toEntity(registeredClient));
    }

    /**
     * Retrieve the RegisteredClient with the specified id.
     *
     * @param id the repository identifier of the client
     * @return the RegisteredClient with the given id, or {@code null} if none is found
     */
    @Override
    public RegisteredClient findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        return this.clientRepository.findById(id).map(this::toObject).orElse(null);
    }

    /**
     * Locate a RegisteredClient by its client identifier.
     *
     * @param clientId the client identifier to look up; must not be null or empty
     * @return the RegisteredClient with the given clientId, or `null` if none is found
     * @throws IllegalArgumentException if {@code clientId} is null or empty
     */
    @Override
    public RegisteredClient findByClientId(String clientId) {
        Assert.hasText(clientId, "clientId cannot be empty");
        return this.clientRepository.findByClientId(clientId).map(this::toObject).orElse(null);
    }

    /**
     * Builds a RegisteredClient from a persistent Client entity.
     *
     * Converts the entity's comma-delimited fields into the corresponding sets (authentication methods,
     * authorization grant types, redirect URIs, post-logout redirect URIs, and scopes), resolves known
     * authentication methods and grant types, and applies client and token settings parsed from the
     * entity's JSON representations.
     *
     * @param client the persistence Client entity to convert
     * @return the RegisteredClient populated from the entity's fields and settings
     */
    private RegisteredClient toObject(Client client) {
        Set<String> clientAuthenticationMethods = StringUtils.commaDelimitedListToSet(
                client.getClientAuthenticationMethods());
        Set<String> authorizationGrantTypes = StringUtils.commaDelimitedListToSet(
                client.getAuthorizationGrantTypes());
        Set<String> redirectUris = StringUtils.commaDelimitedListToSet(
                client.getRedirectUris());
        Set<String> postLogoutRedirectUris = StringUtils.commaDelimitedListToSet(
                client.getPostLogoutRedirectUris());
        Set<String> clientScopes = StringUtils.commaDelimitedListToSet(
                client.getScopes());

        RegisteredClient.Builder builder = RegisteredClient.withId(client.getId())
                .clientId(client.getClientId())
                .clientIdIssuedAt(client.getClientIdIssuedAt())
                .clientSecret(client.getClientSecret())
                .clientSecretExpiresAt(client.getClientSecretExpiresAt())
                .clientName(client.getClientName())
                .clientAuthenticationMethods(authenticationMethods ->
                        clientAuthenticationMethods.forEach(authenticationMethod ->
                                authenticationMethods.add(resolveClientAuthenticationMethod(authenticationMethod))))
                .authorizationGrantTypes((grantTypes) ->
                        authorizationGrantTypes.forEach(grantType ->
                                grantTypes.add(resolveAuthorizationGrantType(grantType))))
                .redirectUris((uris) -> uris.addAll(redirectUris))
                .postLogoutRedirectUris((uris) -> uris.addAll(postLogoutRedirectUris))
                .scopes((scopes) -> scopes.addAll(clientScopes));

        Map<String, Object> clientSettingsMap = parseMap(client.getClientSettings());
        builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build());

        Map<String, Object> tokenSettingsMap = parseMap(client.getTokenSettings());
        builder.tokenSettings(TokenSettings.withSettings(tokenSettingsMap).build());

        return builder.build();
    }

    /**
     * Converts a RegisteredClient into a Client persistence entity.
     *
     * The resulting Client contains scalar fields copied from the RegisteredClient, collection
     * fields serialized as comma-delimited strings, and client/token settings serialized as JSON.
     *
     * @param registeredClient the RegisteredClient to convert
     * @return a Client entity populated with the registered client's data
     */
    private Client toEntity(RegisteredClient registeredClient) {
        List<String> clientAuthenticationMethods = new ArrayList<>(registeredClient.getClientAuthenticationMethods().size());
        registeredClient.getClientAuthenticationMethods().forEach(clientAuthenticationMethod ->
                clientAuthenticationMethods.add(clientAuthenticationMethod.getValue()));

        List<String> authorizationGrantTypes = new ArrayList<>(registeredClient.getAuthorizationGrantTypes().size());
        registeredClient.getAuthorizationGrantTypes().forEach(authorizationGrantType ->
                authorizationGrantTypes.add(authorizationGrantType.getValue()));

        Client entity = new Client();
        entity.setId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientIdIssuedAt(registeredClient.getClientIdIssuedAt() != null
                ? registeredClient.getClientIdIssuedAt()
                : java.time.Instant.now());
        entity.setClientSecret(registeredClient.getClientSecret());
        entity.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        entity.setClientName(registeredClient.getClientName());
        entity.setClientAuthenticationMethods(StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods));
        entity.setAuthorizationGrantTypes(StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes));
        entity.setRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getRedirectUris()));
        entity.setPostLogoutRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getPostLogoutRedirectUris()));
        entity.setScopes(StringUtils.collectionToCommaDelimitedString(registeredClient.getScopes()));
        entity.setClientSettings(writeMap(registeredClient.getClientSettings().getSettings()));
        entity.setTokenSettings(writeMap(registeredClient.getTokenSettings().getSettings()));

        return entity;
    }

    /**
     * Parse a JSON object string into a map.
     *
     * @param data the JSON string representing an object
     * @return a Map<String, Object> containing the parsed JSON object
     * @throws IllegalArgumentException if the input cannot be parsed as JSON
     */
    private Map<String, Object> parseMap(String data) {
        try {
            return this.jsonMapper.readValue(data, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    /**
     * Serialize the provided map to a JSON string.
     *
     * @param data the map to serialize; keys are strings and values are arbitrary JSON-compatible objects
     * @return the JSON string representation of the map
     * @throws IllegalArgumentException if serialization fails
     */
    private String writeMap(Map<String, Object> data) {
        try {
            return this.jsonMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    /**
     * Resolve an AuthorizationGrantType from its string representation.
     *
     * @param authorizationGrantType the grant type value to resolve (e.g. "authorization_code")
     * @return the matching standard AuthorizationGrantType constant, or a new AuthorizationGrantType constructed with the provided value for custom grant types
     */
    private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        } else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        } else if (AuthorizationGrantType.DEVICE_CODE.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.DEVICE_CODE;
        }
        return new AuthorizationGrantType(authorizationGrantType);              // Custom authorization grant type
    }

    /**
     * Resolve a string representation to a standard ClientAuthenticationMethod constant or create a custom one.
     *
     * @param clientAuthenticationMethod the string value of the client authentication method (e.g. "client_secret_basic", "client_secret_post", "none")
     * @return the corresponding standard `ClientAuthenticationMethod` for known values, or a new `ClientAuthenticationMethod` constructed from the provided value for custom methods
     */
    private static ClientAuthenticationMethod resolveClientAuthenticationMethod(String clientAuthenticationMethod) {
        if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
        } else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_POST;
        } else if (ClientAuthenticationMethod.NONE.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.NONE;
        }
        return new ClientAuthenticationMethod(clientAuthenticationMethod);      // Custom client authentication method
    }
}