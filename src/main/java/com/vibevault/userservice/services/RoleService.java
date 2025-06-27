package com.vibevault.userservice.services;

import com.vibevault.userservice.models.Role;

import java.util.List;

public interface RoleService {
    public Role createRole(String roleName, String description);
    public Role updateRole(String roleId, String roleName, String description);
    public List<Role> getAllRoles();
}
