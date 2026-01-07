package com.vibevault.userservice.security.jackson;

import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.security.CustomGrantedAuthority;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.UUID;

public class CustomGrantedAuthorityDeserializer extends StdDeserializer<CustomGrantedAuthority> {

    public CustomGrantedAuthorityDeserializer() {
        super(CustomGrantedAuthority.class);
    }

    @Override
    public CustomGrantedAuthority deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = p.readValueAsTree();
        JsonNode roleNode = node.get("role");

        Role role = new Role();
        if (roleNode != null) {
            if (roleNode.has("id") && !roleNode.get("id").isNull()) {
                role.setId(UUID.fromString(roleNode.get("id").stringValue()));
            }
            if (roleNode.has("name")) {
                role.setName(roleNode.get("name").stringValue());
            }
            if (roleNode.has("description")) {
                role.setDescription(roleNode.get("description").stringValue());
            }
        }

        return new CustomGrantedAuthority(role);
    }
}
