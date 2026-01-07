package com.vibevault.userservice.security.repositories;

import java.util.Optional;

import com.vibevault.userservice.security.models.AuthorizationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AuthorizationConsentRepository extends JpaRepository<AuthorizationConsent, AuthorizationConsent.AuthorizationConsentId> {
    /**
 * Finds the AuthorizationConsent for the specified registered client ID and principal name.
 *
 * @param registeredClientId the client identifier of the registered OAuth2 client
 * @param principalName      the principal (user) name associated with the consent
 * @return an Optional containing the matching AuthorizationConsent, or Optional.empty() if none exists
 */
 
    Optional<AuthorizationConsent> findByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
 
 /**
 * Deletes the AuthorizationConsent matching the given registered client ID and principal name.
 *
 * @param registeredClientId the client identifier associated with the consent
 * @param principalName the principal (user) name associated with the consent
 */
  
    @Modifying
    @Transactional
    void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
}