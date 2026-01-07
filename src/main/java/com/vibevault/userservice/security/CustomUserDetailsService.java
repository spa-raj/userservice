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

    public CustomUserDetailsService(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }
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
