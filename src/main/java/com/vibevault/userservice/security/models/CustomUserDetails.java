package com.vibevault.userservice.security.models;

import com.vibevault.userservice.models.User;
import com.vibevault.userservice.services.utils.UserRoles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private String userEmail;
    private String password;
    private List<GrantedAuthority> authorities;

    public static CustomUserDetails fromUser(User user, UserRoles userRoles) {
        return new CustomUserDetails(user, userRoles);
    }
    private CustomUserDetails(User user, UserRoles userRoles) {
        this.userEmail = user.getEmail();
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
        return userEmail;
    }
}
