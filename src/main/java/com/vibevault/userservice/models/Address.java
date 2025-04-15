package com.vibevault.userservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity(name = "addresses")
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Address extends BaseModel {
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    @ManyToOne
    private User user;
}