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

    /**
     * Constructs a deserializer configured to deserialize instances of CustomGrantedAuthority.
     */
    public CustomGrantedAuthorityDeserializer() {
        super(CustomGrantedAuthority.class);
    }

    /**
     * Deserialize JSON into a CustomGrantedAuthority by extracting an optional nested "role" object.
     *
     * If the "role" node is present, its `id`, `name`, and `description` fields are read and set on a Role;
     * absent fields are left unset. The resulting Role is used to construct the returned CustomGrantedAuthority.
     *
     * @param p the JsonParser positioned at the value to deserialize
     * @param ctxt the deserialization context
     * @return the deserialized CustomGrantedAuthority containing the populated Role
     */
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