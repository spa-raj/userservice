package com.vibevault.userservice.security.jackson;

import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.security.CustomGrantedAuthority;
import com.vibevault.userservice.security.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomUserDetailsDeserializer extends StdDeserializer<CustomUserDetails> {

    public CustomUserDetailsDeserializer() {
        super(CustomUserDetails.class);
    }

    @Override
    public CustomUserDetails deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = p.readValueAsTree();
        JsonNode userNode = node.get("user");

        User user = new User();
        if (userNode != null) {
            if (userNode.has("id") && !userNode.get("id").isNull()) {
                user.setId(UUID.fromString(userNode.get("id").stringValue()));
            }
            if (userNode.has("email")) {
                user.setEmail(userNode.get("email").stringValue());
            }
            if (userNode.has("firstName")) {
                user.setFirstName(userNode.get("firstName").stringValue());
            }
            if (userNode.has("lastName")) {
                user.setLastName(userNode.get("lastName").stringValue());
            }
            if (userNode.has("phoneNumber")) {
                user.setPhoneNumber(userNode.get("phoneNumber").stringValue());
            }
            if (userNode.has("password")) {
                user.setPassword(userNode.get("password").stringValue());
            }
        }

        // Parse cached authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        JsonNode authoritiesNode = node.get("cachedAuthorities");
        if (authoritiesNode != null && authoritiesNode.isArray()) {
            for (JsonNode authNode : authoritiesNode) {
                JsonNode roleNode = authNode.get("role");
                if (roleNode != null) {
                    Role role = new Role();
                    if (roleNode.has("id") && !roleNode.get("id").isNull()) {
                        role.setId(UUID.fromString(roleNode.get("id").stringValue()));
                    }
                    if (roleNode.has("name")) {
                        role.setName(roleNode.get("name").stringValue());
                    }
                    if (roleNode.has("description")) {
                        role.setDescription(roleNode.get("description").stringValue());
                    }
                    authorities.add(new CustomGrantedAuthority(role));
                }
            }
        }

        // UserRoleRepository will be null, but we have cached authorities
        CustomUserDetails customUserDetails = new CustomUserDetails(user, null);
        customUserDetails.setCachedAuthorities(authorities);
        return customUserDetails;
    }
}
