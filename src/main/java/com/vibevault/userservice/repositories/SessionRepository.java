package com.vibevault.userservice.repositories;

import com.vibevault.userservice.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Override
    <S extends Session> S save(S entity);
}
