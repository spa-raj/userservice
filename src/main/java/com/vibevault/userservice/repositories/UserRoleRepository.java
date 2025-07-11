package com.vibevault.userservice.repositories;

import com.vibevault.userservice.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    @Override
    <S extends UserRole> S save(S entity);
    long countUserRoleByRole_Id(UUID roleId);

    Optional<List<UserRole>> findUserRoleByUser_Id(UUID userId);

    List<UserRole> findUserRolesByUser_Id(UUID userId);
}
