package com.vibevault.userservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity(name = "users")
@Table(indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phonenumber", columnList = "phoneNumber")
})
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseModel{
    private String firstName;
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private String phoneNumber;
}
