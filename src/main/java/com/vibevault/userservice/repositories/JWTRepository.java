package com.vibevault.userservice.repositories;

import com.vibevault.userservice.models.JWT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JWTRepository extends JpaRepository<JWT, UUID>{
    @Override
    <S extends JWT> S save(S entity);

    @Override
    Optional<JWT> findById(UUID uuid);
}
