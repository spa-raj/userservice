package com.vibevault.userservice.security.models;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "authorization_consent")
@IdClass(AuthorizationConsent.AuthorizationConsentId.class)
public class AuthorizationConsent {
    @Id
    private String registeredClientId;
    @Id
    private String principalName;
    @Column(length = 1000)
    private String authorities;

    public static class AuthorizationConsentId implements Serializable {
        private String registeredClientId;
        private String principalName;

        /**
         * Compares this AuthorizationConsentId to another object for equality.
         *
         * Two AuthorizationConsentId instances are equal when they are the same instance or when the other
         * object is also an AuthorizationConsentId with equal registeredClientId and principalName.
         *
         * @param o the object to compare with
         * @return `true` if equal, `false` otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AuthorizationConsentId that = (AuthorizationConsentId) o;
            return Objects.equals(registeredClientId, that.registeredClientId)
                    && Objects.equals(principalName, that.principalName);
        }

        /**
         * Computes a hash code for this composite key using the `registeredClientId` and `principalName`.
         *
         * @return an int hash code derived from `registeredClientId` and `principalName`
         */
        @Override
        public int hashCode() {
            return Objects.hash(registeredClientId, principalName);
        }
    }
}