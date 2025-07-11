package com.vibevault.userservice.security.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.services.utils.UserRoles;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(as = CustomUserDetails.class)
@Getter
@Setter
public class CustomUserDetails implements UserDetails {

    @JsonProperty(value = "username")
    private String userName;
    private String password;
    private List<GrantedAuthority> authorities;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    public static CustomUserDetails fromUser(User user, UserRoles userRoles) {
        return new CustomUserDetails(user, userRoles);
    }
    public CustomUserDetails() {
        // Default constructor for serialization/deserialization
    }
    private CustomUserDetails(User user, UserRoles userRoles) {
        this.userName = user.getEmail();
        this.password = user.getPassword();
        this.authorities = userRoles.getRolesForUser(user).stream()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
