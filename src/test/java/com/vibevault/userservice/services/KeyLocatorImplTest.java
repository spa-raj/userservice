package com.vibevault.userservice.services;

import com.vibevault.userservice.exceptions.auth.SigningKeyNotFoundException;
import com.vibevault.userservice.models.JWT;
import com.vibevault.userservice.repositories.JWTRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ProtectedHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Optional;
import java.util.UUID;

import static com.vibevault.userservice.services.Consts.JWT_SECRET_ALGORITHM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyLocatorImplTest {

    @Mock
    private JWTRepository jwtRepository;

    @Mock
    private ProtectedHeader protectedHeader;

    private KeyLocatorImpl keyLocator;

    @BeforeEach
    void setUp() {
        keyLocator = new KeyLocatorImpl(jwtRepository);
    }

    @Test
    void successfullyLocatesKeyWhenValidHeaderAndKeyExists() {
        // Arrange
        UUID keyId = UUID.randomUUID();
        String validBase64Secret = "c2VjcmV0S2V5VmFsdWU="; // Base64 encoded "secretKeyValue"

        JWT jwt = new JWT();
        jwt.setId(keyId);
        jwt.setSecret(validBase64Secret);

        when(protectedHeader.getKeyId()).thenReturn(keyId.toString());
        when(jwtRepository.findById(keyId)).thenReturn(Optional.of(jwt));

        // Act
        SecretKey result = keyLocator.locate(protectedHeader);

        // Assert
        assertNotNull(result);
        assertEquals(JWT_SECRET_ALGORITHM, result.getAlgorithm());
        verify(jwtRepository).findById(keyId);
    }

    @Test
    void throwsJwtExceptionWhenKeyIdIsMissing() {
        // Arrange
        when(protectedHeader.getKeyId()).thenReturn(null);

        // Act & Assert
        JwtException exception = assertThrows(JwtException.class, () -> {
            keyLocator.locate(protectedHeader);
        });

        assertEquals("Missing 'kid' in header", exception.getMessage());
        verify(jwtRepository, never()).findById(any());
    }

    @Test
    void throwsMalformedJwtExceptionWhenKeyIdIsNotValidUUID() {
        // Arrange
        String invalidUUID = "not-a-uuid";
        when(protectedHeader.getKeyId()).thenReturn(invalidUUID);

        // Act & Assert
        MalformedJwtException exception = assertThrows(MalformedJwtException.class, () -> {
            keyLocator.locate(protectedHeader);
        });

        assertTrue(exception.getMessage().contains("Invalid 'kid' format"));
        verify(jwtRepository, never()).findById(any());
    }

    @Test
    void throwsSigningKeyNotFoundExceptionWhenKeyDoesNotExist() {
        // Arrange
        UUID keyId = UUID.randomUUID();
        when(protectedHeader.getKeyId()).thenReturn(keyId.toString());
        when(jwtRepository.findById(keyId)).thenReturn(Optional.empty());

        // Act & Assert
        SigningKeyNotFoundException exception = assertThrows(SigningKeyNotFoundException.class, () -> {
            keyLocator.locate(protectedHeader);
        });

        assertTrue(exception.getMessage().contains("Signing key not found for kid"));
        verify(jwtRepository).findById(keyId);
    }

    @Test
    void throwsMalformedJwtExceptionWhenSecretIsNotValidBase64() {
        // Arrange
        UUID keyId = UUID.randomUUID();
        String invalidBase64 = "!@#$%^&*()";

        JWT jwt = new JWT();
        jwt.setId(keyId);
        jwt.setSecret(invalidBase64);

        when(protectedHeader.getKeyId()).thenReturn(keyId.toString());
        when(jwtRepository.findById(keyId)).thenReturn(Optional.of(jwt));

        // Act & Assert
        MalformedJwtException exception = assertThrows(MalformedJwtException.class, () -> {
            keyLocator.locate(protectedHeader);
        });

        assertTrue(exception.getMessage().contains("Could not decode Base64 secret"));
        verify(jwtRepository).findById(keyId);
    }
}