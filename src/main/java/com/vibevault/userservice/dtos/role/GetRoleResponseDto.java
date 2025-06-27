package com.vibevault.userservice.dtos.role;

import com.vibevault.userservice.models.Role;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class GetRoleResponseDto {
    private String roleId;
    private String roleName;
    private String description;

    public static GetRoleResponseDto fromRole(Role role) {
        GetRoleResponseDto response = new GetRoleResponseDto();
        response.setRoleName(role.getName());
        response.setDescription(role.getDescription());
        response.setRoleId(String.valueOf(role.getId()));
        return response;
    }

    public static List<GetRoleResponseDto> fromRoles(List<Role> roles) {
        return roles.stream()
                .map(GetRoleResponseDto::fromRole)
                .collect(Collectors.toList());
    }
}
