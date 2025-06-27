package com.vibevault.userservice.dtos.role;

import lombok.*;

@Data
public class CreateRoleRequestDto {
    private String roleName;
    private String description;
}
