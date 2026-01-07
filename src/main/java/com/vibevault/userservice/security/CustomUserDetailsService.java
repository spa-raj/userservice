package com.vibevault.userservice.security;

import com.vibevault.userservice.models.User;
import com.vibevault.userservice.repositories.UserRepository;
import com.vibevault.userservice.repositories.UserRoleRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.Optional;

@Getter
@Setter
@Service
@JsonDeserialize(as = CustomUserDetailsService.class)
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Creates a CustomUserDetailsService wired with the repositories required to load users and their roles.
     *
     * @param userRepository repository used to find users (by email)
     * @param userRoleRepository repository used to resolve user roles
     */
    public CustomUserDetailsService(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }
    /**
     * Loads user authentication details for the given username (email).
     *
     * @param username the user's email address used to locate the account
     * @return a UserDetails object representing the user's authentication and authorities
     * @throws UsernameNotFoundException if no user exists with the provided email
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findUserByEmail(username);
        if(optionalUser.isEmpty()){
            throw new UsernameNotFoundException("User not found with email: " + username);
        } else {
            return new CustomUserDetails(optionalUser.get(), this.userRoleRepository);
        }
    }
}