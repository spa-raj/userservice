package com.vibevault.userservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponseDto {
    private String userEmail;
    private String name;
    private String phone;
}
