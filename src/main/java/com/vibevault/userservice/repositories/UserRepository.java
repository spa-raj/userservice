package com.vibevault.userservice.repositories;

import com.vibevault.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Override
    Optional<User> findById(UUID uuid);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByPhoneNumber(String phoneNumber);

    @Override
    <S extends User> S save(S entity);
}
