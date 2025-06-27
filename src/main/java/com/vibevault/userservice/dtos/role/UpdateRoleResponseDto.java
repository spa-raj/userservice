package com.vibevault.userservice.dtos.role;

import lombok.Data;

@Data
public class UpdateRoleResponseDto {
    private String roleName;
    private String description;
    private String message;
    public UpdateRoleResponseDto(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
        this.message = "Role updated successfully";
    }
}
