package com.vibevault.userservice.security.repositories;

import java.util.Optional;

import com.vibevault.userservice.security.models.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizationRepository extends JpaRepository<Authorization, String> {
    /**
 * Finds an Authorization whose state field matches the given state token.
 *
 * @param state the state token to match against the Authorization's `state` field
 * @return an Optional containing the matching Authorization if present, or an empty Optional if none found
 */
Optional<Authorization> findByState(String state);
    /**
 * Finds an Authorization by its authorization code value.
 *
 * @param authorizationCode the authorization code value to match
 * @return an Optional containing the Authorization whose authorizationCodeValue equals the given authorizationCode, or empty if no match is found
 */
Optional<Authorization> findByAuthorizationCodeValue(String authorizationCode);
    /**
 * Finds an Authorization by its access token value.
 *
 * @param accessToken the access token value to match
 * @return an Optional containing the Authorization whose accessTokenValue equals the provided token, or empty if none found
 */
Optional<Authorization> findByAccessTokenValue(String accessToken);
    /**
 * Finds an Authorization whose refresh token value matches the given token.
 *
 * @param refreshToken the refresh token value to match
 * @return an Optional containing the matching Authorization if present, empty otherwise
 */
Optional<Authorization> findByRefreshTokenValue(String refreshToken);
    /**
 * Locates an Authorization by the stored OpenID Connect ID token value.
 *
 * @param idToken the OIDC ID token value to match against stored authorizations
 * @return an Optional containing the matching Authorization if found, otherwise an empty Optional
 */
Optional<Authorization> findByOidcIdTokenValue(String idToken);
    /**
 * Finds an Authorization by its user code value.
 *
 * @param userCode the user code to match against the Authorization's userCodeValue
 * @return an Optional containing the Authorization with the specified user code, or empty if none is found
 */
Optional<Authorization> findByUserCodeValue(String userCode);
    /**
 * Finds an Authorization whose device code value matches the given device code.
 *
 * @param deviceCode the device code to match against the Authorization's deviceCodeValue
 * @return an Optional containing the matching Authorization, or an empty Optional if no match is found
 */
Optional<Authorization> findByDeviceCodeValue(String deviceCode);
    /**
     * Finds an Authorization whose state, authorization code, access token, refresh token,
     * OIDC ID token, user code, or device code equals the provided token.
     *
     * @param token the token value to match against any of the authorization's token fields
     * @return an Optional containing the matching Authorization if one exists, otherwise an empty Optional
     */
    @Query("select a from Authorization a where a.state = :token" +
            " or a.authorizationCodeValue = :token" +
            " or a.accessTokenValue = :token" +
            " or a.refreshTokenValue = :token" +
            " or a.oidcIdTokenValue = :token" +
            " or a.userCodeValue = :token" +
            " or a.deviceCodeValue = :token"
    )
    Optional<Authorization> findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValueOrOidcIdTokenValueOrUserCodeValueOrDeviceCodeValue(@Param("token") String token);
}