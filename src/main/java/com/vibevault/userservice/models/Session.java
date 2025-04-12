package com.vibevault.userservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.Date;

@Entity(name = "sessions")
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Session extends BaseModel{
    @ManyToOne
    private User user;
    private String token;
    private String device;
    private String ipAddress;
    private Date expiredAt;
}
