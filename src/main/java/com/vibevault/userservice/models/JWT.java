package com.vibevault.userservice.models;

import jakarta.persistence.Entity;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "jwt")
public class JWT extends BaseModel{
    private String secret;
}
