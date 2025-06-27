package com.vibevault.userservice.dtos.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoleResponseDto {
    private String roleName;
    private String description;
    private String message;

    public CreateRoleResponseDto(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
        this.message = "Role created successfully";
    }
}
