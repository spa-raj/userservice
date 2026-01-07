package com.vibevault.userservice.security.services;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.vibevault.userservice.security.jackson.CustomSecurityJacksonModule;
import com.vibevault.userservice.security.models.Authorization;
import com.vibevault.userservice.security.repositories.AuthorizationRepository;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2DeviceCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.OAuth2UserCode;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Component
public class JpaOAuth2AuthorizationService implements OAuth2AuthorizationService {
    private final AuthorizationRepository authorizationRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final JsonMapper jsonMapper;

    /**
     * Create a JpaOAuth2AuthorizationService that persists OAuth2Authorizations using the given repositories
     * and configures a JsonMapper with security and custom modules for safe polymorphic JSON (de)serialization.
     *
     * @param authorizationRepository repository used to persist and load Authorization entities
     * @param registeredClientRepository repository used to load RegisteredClient instances by id
     * @throws IllegalArgumentException if {@code authorizationRepository} or {@code registeredClientRepository} is null
     */
    public JpaOAuth2AuthorizationService(AuthorizationRepository authorizationRepository, RegisteredClientRepository registeredClientRepository) {
        Assert.notNull(authorizationRepository, "authorizationRepository cannot be null");
        Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
        this.authorizationRepository = authorizationRepository;
        this.registeredClientRepository = registeredClientRepository;

        ClassLoader classLoader = JpaOAuth2AuthorizationService.class.getClassLoader();
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
     * Persist the given OAuth2Authorization in the backing repository.
     *
     * @param authorization the authorization to persist; must not be null
     */
    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        this.authorizationRepository.save(toEntity(authorization));
    }

    /**
     * Remove the persisted authorization that corresponds to the given OAuth2Authorization.
     *
     * @param authorization the authorization whose stored record will be deleted (identified by its id)
     * @throws IllegalArgumentException if {@code authorization} is {@code null}
     */
    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        this.authorizationRepository.deleteById(authorization.getId());
    }

    /**
     * Locate an OAuth2 authorization by its identifier.
     *
     * @param id the authorization identifier; must not be empty
     * @return the matching {@link OAuth2Authorization}, or `null` if none exists
     */
    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        return this.authorizationRepository.findById(id).map(this::toObject).orElse(null);
    }

    /**
     * Locate an OAuth2Authorization by a token string, optionally constrained to a specific token type.
     *
     * @param token the token value to search for (must not be empty)
     * @param tokenType the token type to restrict the lookup (e.g., state, code, access_token, refresh_token, id_token, user_code, device_code); if `null`, search across all supported token fields
     * @return the matching OAuth2Authorization, or `null` if none is found
     */
    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");

        Optional<Authorization> result;
        if (tokenType == null) {
            result = this.authorizationRepository.findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValueOrOidcIdTokenValueOrUserCodeValueOrDeviceCodeValue(token);
        } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            result = this.authorizationRepository.findByState(token);
        } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            result = this.authorizationRepository.findByAuthorizationCodeValue(token);
        } else if (OAuth2ParameterNames.ACCESS_TOKEN.equals(tokenType.getValue())) {
            result = this.authorizationRepository.findByAccessTokenValue(token);
        } else if (OAuth2ParameterNames.REFRESH_TOKEN.equals(tokenType.getValue())) {
            result = this.authorizationRepository.findByRefreshTokenValue(token);
        } else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
            result = this.authorizationRepository.findByOidcIdTokenValue(token);
        } else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
            result = this.authorizationRepository.findByUserCodeValue(token);
        } else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
            result = this.authorizationRepository.findByDeviceCodeValue(token);
        } else {
            result = Optional.empty();
        }

        return result.map(this::toObject).orElse(null);
    }

    /**
     * Converts a persisted Authorization entity into an OAuth2Authorization instance.
     *
     * <p>Reconstructs the registered client reference, principal name, grant type, authorized scopes,
     * attributes, and all supported tokens (authorization code, access token, refresh token, OIDC ID
     * token, user code, device code) including their issued/expiry instants and per-token metadata
     * and claims.</p>
     *
     * @param entity the persisted Authorization entity to convert
     * @return an OAuth2Authorization populated from the entity
     * @throws DataRetrievalFailureException if the RegisteredClient referenced by the entity cannot be found
     */
    private OAuth2Authorization toObject(Authorization entity) {
        RegisteredClient registeredClient = this.registeredClientRepository.findById(entity.getRegisteredClientId());
        if (registeredClient == null) {
            throw new DataRetrievalFailureException(
                    "The RegisteredClient with id '" + entity.getRegisteredClientId() + "' was not found in the RegisteredClientRepository.");
        }

        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(entity.getId())
                .principalName(entity.getPrincipalName())
                .authorizationGrantType(resolveAuthorizationGrantType(entity.getAuthorizationGrantType()))
                .authorizedScopes(StringUtils.commaDelimitedListToSet(entity.getAuthorizedScopes()))
                .attributes(attributes -> attributes.putAll(parseMap(entity.getAttributes())));
        if (entity.getState() != null) {
            builder.attribute(OAuth2ParameterNames.STATE, entity.getState());
        }

        if (entity.getAuthorizationCodeValue() != null) {
            OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
                    entity.getAuthorizationCodeValue(),
                    entity.getAuthorizationCodeIssuedAt(),
                    entity.getAuthorizationCodeExpiresAt());
            builder.token(authorizationCode, metadata -> metadata.putAll(parseMap(entity.getAuthorizationCodeMetadata())));
        }

        if (entity.getAccessTokenValue() != null) {
            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    entity.getAccessTokenValue(),
                    entity.getAccessTokenIssuedAt(),
                    entity.getAccessTokenExpiresAt(),
                    StringUtils.commaDelimitedListToSet(entity.getAccessTokenScopes()));
            builder.token(accessToken, metadata -> metadata.putAll(parseMap(entity.getAccessTokenMetadata())));
        }

        if (entity.getRefreshTokenValue() != null) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                    entity.getRefreshTokenValue(),
                    entity.getRefreshTokenIssuedAt(),
                    entity.getRefreshTokenExpiresAt());
            builder.token(refreshToken, metadata -> metadata.putAll(parseMap(entity.getRefreshTokenMetadata())));
        }

        if (entity.getOidcIdTokenValue() != null) {
            OidcIdToken idToken = new OidcIdToken(
                    entity.getOidcIdTokenValue(),
                    entity.getOidcIdTokenIssuedAt(),
                    entity.getOidcIdTokenExpiresAt(),
                    parseMap(entity.getOidcIdTokenClaims()));
            builder.token(idToken, metadata -> metadata.putAll(parseMap(entity.getOidcIdTokenMetadata())));
        }

        if (entity.getUserCodeValue() != null) {
            OAuth2UserCode userCode = new OAuth2UserCode(
                    entity.getUserCodeValue(),
                    entity.getUserCodeIssuedAt(),
                    entity.getUserCodeExpiresAt());
            builder.token(userCode, metadata -> metadata.putAll(parseMap(entity.getUserCodeMetadata())));
        }

        if (entity.getDeviceCodeValue() != null) {
            OAuth2DeviceCode deviceCode = new OAuth2DeviceCode(
                    entity.getDeviceCodeValue(),
                    entity.getDeviceCodeIssuedAt(),
                    entity.getDeviceCodeExpiresAt());
            builder.token(deviceCode, metadata -> metadata.putAll(parseMap(entity.getDeviceCodeMetadata())));
        }

        return builder.build();
    }

    /**
     * Convert an OAuth2Authorization into a populated Authorization entity suitable for persistence.
     *
     * Copies the authorization's id, registered client id, principal name, grant type, authorized scopes,
     * attributes, and state, and extracts all token payloads (authorization code, access token, refresh token,
     * OIDC ID token, user code, device code) including token values, issuedAt/expiresAt timestamps, token metadata,
     * access token scopes, and OIDC ID token claims.
     *
     * @param authorization the OAuth2Authorization to convert
     * @return an Authorization entity containing the data from the provided OAuth2Authorization
     */
    private Authorization toEntity(OAuth2Authorization authorization) {
        Authorization entity = new Authorization();
        entity.setId(authorization.getId());
        entity.setRegisteredClientId(authorization.getRegisteredClientId());
        entity.setPrincipalName(authorization.getPrincipalName());
        entity.setAuthorizationGrantType(authorization.getAuthorizationGrantType().getValue());
        entity.setAuthorizedScopes(StringUtils.collectionToDelimitedString(authorization.getAuthorizedScopes(), ","));
        entity.setAttributes(writeMap(authorization.getAttributes()));
        entity.setState(authorization.getAttribute(OAuth2ParameterNames.STATE));

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
        setTokenValues(
                authorizationCode,
                entity::setAuthorizationCodeValue,
                entity::setAuthorizationCodeIssuedAt,
                entity::setAuthorizationCodeExpiresAt,
                entity::setAuthorizationCodeMetadata
        );

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
                authorization.getToken(OAuth2AccessToken.class);
        setTokenValues(
                accessToken,
                entity::setAccessTokenValue,
                entity::setAccessTokenIssuedAt,
                entity::setAccessTokenExpiresAt,
                entity::setAccessTokenMetadata
        );
        if (accessToken != null && accessToken.getToken().getScopes() != null) {
            entity.setAccessTokenScopes(StringUtils.collectionToDelimitedString(accessToken.getToken().getScopes(), ","));
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
                authorization.getToken(OAuth2RefreshToken.class);
        setTokenValues(
                refreshToken,
                entity::setRefreshTokenValue,
                entity::setRefreshTokenIssuedAt,
                entity::setRefreshTokenExpiresAt,
                entity::setRefreshTokenMetadata
        );

        OAuth2Authorization.Token<OidcIdToken> oidcIdToken =
                authorization.getToken(OidcIdToken.class);
        setTokenValues(
                oidcIdToken,
                entity::setOidcIdTokenValue,
                entity::setOidcIdTokenIssuedAt,
                entity::setOidcIdTokenExpiresAt,
                entity::setOidcIdTokenMetadata
        );
        if (oidcIdToken != null) {
            entity.setOidcIdTokenClaims(writeMap(oidcIdToken.getClaims()));
        }

        OAuth2Authorization.Token<OAuth2UserCode> userCode =
                authorization.getToken(OAuth2UserCode.class);
        setTokenValues(
                userCode,
                entity::setUserCodeValue,
                entity::setUserCodeIssuedAt,
                entity::setUserCodeExpiresAt,
                entity::setUserCodeMetadata
        );

        OAuth2Authorization.Token<OAuth2DeviceCode> deviceCode =
                authorization.getToken(OAuth2DeviceCode.class);
        setTokenValues(
                deviceCode,
                entity::setDeviceCodeValue,
                entity::setDeviceCodeIssuedAt,
                entity::setDeviceCodeExpiresAt,
                entity::setDeviceCodeMetadata
        );

        return entity;
    }

    /**
     * If the given token is present, supplies its value, issued-at time, expiry time,
     * and serialized metadata to the provided consumers.
     *
     * @param token the OAuth2 authorization token to read values from; if {@code null}, no consumers are invoked
     * @param tokenValueConsumer receives the token's raw string value
     * @param issuedAtConsumer receives the token's issued-at instant (may be {@code null})
     * @param expiresAtConsumer receives the token's expiry instant (may be {@code null})
     * @param metadataConsumer receives the token metadata serialized as a JSON string
     */
    private void setTokenValues(
            OAuth2Authorization.Token<?> token,
            Consumer<String> tokenValueConsumer,
            Consumer<Instant> issuedAtConsumer,
            Consumer<Instant> expiresAtConsumer,
            Consumer<String> metadataConsumer) {
        if (token != null) {
            OAuth2Token oAuth2Token = token.getToken();
            tokenValueConsumer.accept(oAuth2Token.getTokenValue());
            issuedAtConsumer.accept(oAuth2Token.getIssuedAt());
            expiresAtConsumer.accept(oAuth2Token.getExpiresAt());
            metadataConsumer.accept(writeMap(token.getMetadata()));
        }
    }

    /**
     * Parse a JSON-formatted string into a Map of keys to deserialized values.
     *
     * @param data the JSON string to parse (may be null or empty resulting in appropriate mapping)
     * @return a Map<String, Object> representing the deserialized JSON structure
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
     * Serialize a metadata map to its JSON string representation.
     *
     * @param metadata the map of metadata values to serialize; may be empty but not necessarily non-null
     * @return the JSON string representation of the provided metadata map
     * @throws IllegalArgumentException if the metadata cannot be serialized to JSON
     */
    private String writeMap(Map<String, Object> metadata) {
        try {
            return this.jsonMapper.writeValueAsString(metadata);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    /**
     * Resolve a grant type identifier to the corresponding AuthorizationGrantType.
     *
     * <p>Maps standard grant type values ("authorization_code", "client_credentials",
     * "refresh_token", "urn:ietf:params:oauth:grant-type:device_code") to their
     * predefined AuthorizationGrantType constants; for any other identifier returns
     * a new AuthorizationGrantType constructed from the provided string.</p>
     *
     * @param authorizationGrantType the grant type identifier string (standard or custom)
     * @return the matching AuthorizationGrantType constant for known types, or a new AuthorizationGrantType for a custom identifier
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
}