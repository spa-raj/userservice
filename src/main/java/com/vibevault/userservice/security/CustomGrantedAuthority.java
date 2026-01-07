package com.vibevault.userservice.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vibevault.userservice.models.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.io.Serial;

@Getter
@Setter
@NoArgsConstructor
@JsonDeserialize(as = CustomGrantedAuthority.class)
public class CustomGrantedAuthority implements GrantedAuthority {
    @Serial
    private static final long serialVersionUID = 1L;

    private Role role;

    public CustomGrantedAuthority(Role role) {
        this.role = role;
    }

    @Override
    @JsonIgnore
    public @Nullable String getAuthority() {
        return this.role.getName();
    }
}
