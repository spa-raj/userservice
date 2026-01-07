package com.vibevault.userservice.security.repositories;

import java.util.Optional;

import com.vibevault.userservice.security.models.AuthorizationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AuthorizationConsentRepository extends JpaRepository<AuthorizationConsent, AuthorizationConsent.AuthorizationConsentId> {
    Optional<AuthorizationConsent> findByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);

    @Modifying
    @Transactional
    void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
}