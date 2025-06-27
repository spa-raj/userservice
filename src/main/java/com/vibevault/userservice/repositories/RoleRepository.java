package com.vibevault.userservice.repositories;

import com.vibevault.userservice.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    @Override
    <S extends Role> S save(S entity);

    @Override
    Optional<Role> findById(UUID uuid);

    Optional<Role> findByName(String name);

    Optional<Role> findRoleByName(String name);
}
