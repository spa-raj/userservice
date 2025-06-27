package com.vibevault.userservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity(name = "sessions")
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Session extends BaseModel{
    @ManyToOne
    private User user;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> role;
    private String token;
    private String device;
    private String ipAddress;
    private Date expiredAt;

    @Enumerated(EnumType.ORDINAL)
    private SessionStatus status;
}
