package com.vibevault.userservice.repositories;

import com.vibevault.userservice.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleRepostitory extends JpaRepository<UserRole, UUID> {
    @Override
    <S extends UserRole> S save(S entity);
}
