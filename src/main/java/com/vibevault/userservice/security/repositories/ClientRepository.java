package com.vibevault.userservice.security.repositories;

import java.util.Optional;
import com.vibevault.userservice.security.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
    /**
 * Finds a Client by its clientId.
 *
 * @param clientId the client identifier to look up
 * @return an {@link Optional} containing the matching {@link Client} if found, otherwise an empty {@link Optional}
 */
Optional<Client> findByClientId(String clientId);
}