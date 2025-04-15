package com.vibevault.userservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.*;

@Entity(name = "user_profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserProfile extends BaseModel{
    @OneToOne
    private User user;
    private String bio;
    private String profilePicture;
}
