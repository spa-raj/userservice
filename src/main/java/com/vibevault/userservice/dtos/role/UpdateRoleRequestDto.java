package com.vibevault.userservice.dtos.role;

import lombok.Data;

@Data
public class UpdateRoleRequestDto {
    private String roleName;
    private String description;

    public UpdateRoleRequestDto(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }
}
