package com.vibevault.userservice.services;

import com.vibevault.userservice.exceptions.SigningKeyNotFoundException;
import com.vibevault.userservice.models.JWT;
import com.vibevault.userservice.repositories.JWTRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.UUID;

@Service
public class KeyLocatorImpl extends LocatorAdapter<Key> {
    private JWTRepository jwtRepository;

    public KeyLocatorImpl(JWTRepository jwtRepository) {
        this.jwtRepository = jwtRepository;
    }
    @Override
    public SecretKey locate(ProtectedHeader header) { // a JwsHeader or JweHeader
        // 1. Extract the key‐id ("kid") from the protected header
        String kid = header.getKeyId();
        if (kid == null) {
            throw new JwtException("Missing 'kid' in header");
        }

        UUID keyId;
        try {
            keyId = UUID.fromString(kid);
        } catch (IllegalArgumentException e) {
            throw new MalformedJwtException("Invalid 'kid' format: " + kid, e);
        }
        JWT jwtEntity = jwtRepository.findById(keyId)
                .orElseThrow(() -> new SigningKeyNotFoundException("Signing key not found for kid: " + kid));

        // 3. Decode the Base64‐encoded secret and build a SecretKeySpec

        byte[] secretBytes;
        try {
            secretBytes = Base64.getDecoder().decode(jwtEntity.getSecret());
        } catch (IllegalArgumentException e) {
            // Consider logging this event
            throw new MalformedJwtException("Could not decode Base64 secret for kid: " + kid, e);
        }
        MacAlgorithm algorithm = Jwts.SIG.HS512; // Assuming HS512 was used for signing

        // Use the JCA algorithm name obtained via getJcaName()
        return new SecretKeySpec(secretBytes, "HmacSHA512");
    }
}
