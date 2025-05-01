package com.vibevault.userservice.repositories;

import com.vibevault.userservice.models.JWT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JWTRepository extends JpaRepository<JWT, UUID>{
    @Override
    <S extends JWT> S save(S entity);

    @Override
    Optional<JWT> findById(UUID uuid);

    @Query(nativeQuery = true, value = "SELECT * FROM jwt WHERE user_id = ?1 AND is_deleted = ?2 ORDER BY created_at DESC LIMIT 1")
    Optional<JWT> findTopByUser_IdEqualsAndDeletedEquals(UUID userId, boolean deleted);

    @Query(nativeQuery = true, value = "SELECT * FROM jwt WHERE user_id = ?1 AND is_deleted = ?2")
    List<JWT> findAllByUser_IdAndDeletedIs(UUID userId, boolean deleted);
}
