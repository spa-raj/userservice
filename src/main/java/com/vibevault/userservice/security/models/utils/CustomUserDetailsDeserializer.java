package com.vibevault.userservice.security.models.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibevault.userservice.security.models.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomUserDetailsDeserializer extends JsonDeserializer<CustomUserDetails> {
    @Override
    public CustomUserDetails deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        CustomUserDetails userDetails = new CustomUserDetails();

        if (node.has("username")) {
            userDetails.setUserName(node.get("username").asText());
        }

        if (node.has("password")) {
            userDetails.setPassword(node.get("password").asText());
        }

        if (node.has("authorities")) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            JsonNode authoritiesNode = node.get("authorities");
            if (authoritiesNode.isArray()) {
                for (JsonNode authorityNode : authoritiesNode) {
                    if (authorityNode.has("authority")) {
                        authorities.add(new SimpleGrantedAuthority(authorityNode.get("authority").asText()));
                    }
                }
            }
            userDetails.setAuthorities(authorities);
        }

        if (node.has("accountNonExpired")) {
            userDetails.setAccountNonExpired(node.get("accountNonExpired").asBoolean(true));
        }

        if (node.has("accountNonLocked")) {
            userDetails.setAccountNonLocked(node.get("accountNonLocked").asBoolean(true));
        }

        if (node.has("credentialsNonExpired")) {
            userDetails.setCredentialsNonExpired(node.get("credentialsNonExpired").asBoolean(true));
        }

        if (node.has("enabled")) {
            userDetails.setEnabled(node.get("enabled").asBoolean(true));
        }

        return userDetails;
    }
}