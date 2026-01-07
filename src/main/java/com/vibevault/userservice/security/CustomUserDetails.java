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
@JsonDeserialize(as = CustomUserDetails.class)
public class CustomUserDetails implements UserDetails {
    private final User user;
    @JsonIgnore
    @Setter
    private UserRoleRepository userRoleRepository;
    // Cached authorities for serialization - loaded once and stored
    private List<GrantedAuthority> cachedAuthorities;

    /**
     * Constructs a CustomUserDetails that adapts a domain User for Spring Security and uses a repository to load the user's roles.
     *
     * @param user the domain User to adapt
     * @param userRoleRepository repository used to load the user's roles; may be null if role loading is not available
     */
    public CustomUserDetails(User user,
                             UserRoleRepository userRoleRepository) {
        this.user = user;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Provides the user's authorities as a collection of granted authorities, loading and caching them when needed.
     *
     * @return a non-null Collection of {@link GrantedAuthority} representing the user's roles; empty if the user has no roles or roles are unavailable.
     */
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

    /**
     * Store a pre-populated list of granted authorities into the cachedAuthorities field, typically during deserialization.
     *
     * @param authorities the list of GrantedAuthority instances to cache; may be null
     */
    public void setCachedAuthorities(List<GrantedAuthority> authorities) {
        this.cachedAuthorities = authorities;
    }

    /**
     * Return the cached list of granted authorities, loading them if not already populated for serialization.
     *
     * @return the cached list of {@link GrantedAuthority} instances (never null; may be empty)
     */
    public List<GrantedAuthority> getCachedAuthorities() {
        // Ensure authorities are loaded before serialization
        if (cachedAuthorities == null) {
            getAuthorities();
        }
        return cachedAuthorities;
    }

    /**
     * Accesses the wrapped user's password.
     *
     * @return the user's password, or {@code null} if not set.
     */
    @Override
    @JsonIgnore
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    /**
     * Exposes the wrapped User's email address as the security principal username.
     *
     * @return the user's email address used as the username
     */
    @Override
    @JsonIgnore
    public @NullMarked String getUsername() {
        return user.getEmail();
    }

    /**
     * Indicates whether the user's account has not expired.
     *
     * @return `true` if the user's account is not expired, `false` otherwise.
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    /**
     * Indicates whether the user's account is not locked.
     *
     * @return true if the account is not locked, false otherwise.
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    /**
     * Indicates whether the user's credentials are not expired.
     *
     * @return `true` if the user's credentials are not expired, `false` otherwise.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    /**
     * Indicates whether the user account is enabled.
     *
     * @return `true` if the user is enabled, `false` otherwise.
     */
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}