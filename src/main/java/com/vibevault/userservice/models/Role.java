package com.vibevault.userservice.models;

import jakarta.persistence.Entity;
import lombok.*;

@Entity(name = "roles")
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role extends BaseModel{
    private String name;
    private String description;

    @Override
    public String toString() {
        return "Role{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
