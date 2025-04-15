package com.vibevault.userservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.Date;

@Entity(name = "user_roles")
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRole extends BaseModel{
    @ManyToOne
    private User user;

    @ManyToOne
    private Role role;

    private Date assignedAt;

    @ManyToOne
    private User assignedBy;
}
