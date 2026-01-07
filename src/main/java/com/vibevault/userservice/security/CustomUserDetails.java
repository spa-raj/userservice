package com.vibevault.userservice.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;
import com.vibevault.userservice.repositories.UserRoleRepository;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@JsonDeserialize(as = CustomUserDetails.class)
public class CustomUserDetails implements UserDetails {
    private final User user;
    @JsonIgnore
    private UserRoleRepository userRoleRepository;
    // Cached authorities for serialization - loaded once and stored
    private List<GrantedAuthority> cachedAuthorities;

    public CustomUserDetails(User user,
                             UserRoleRepository userRoleRepository) {
        this.user = user;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @JsonIgnore
    public @NullMarked Collection<? extends GrantedAuthority> getAuthorities() {
        if (cachedAuthorities != null) {
            return cachedAuthorities;
        }
        cachedAuthorities = new ArrayList<>();
        if (userRoleRepository != null) {
            Optional<List<UserRole>> optionalUserRoleList = userRoleRepository.findUserRoleByUser_Id(user.getId());
            if (optionalUserRoleList.isPresent()) {
                for (UserRole userRole : optionalUserRoleList.get()) {
                    cachedAuthorities.add(new CustomGrantedAuthority(userRole.getRole()));
                }
            }
        }
        return cachedAuthorities;
    }

    // Setter for deserialization
    public void setCachedAuthorities(List<GrantedAuthority> authorities) {
        this.cachedAuthorities = authorities;
    }

    // Getter for serialization
    public List<GrantedAuthority> getCachedAuthorities() {
        // Ensure authorities are loaded before serialization
        if (cachedAuthorities == null) {
            getAuthorities();
        }
        return cachedAuthorities;
    }

    @Override
    @JsonIgnore
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    @JsonIgnore
    public @NullMarked String getUsername() {
        return user.getEmail();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
