package com.vibevault.userservice.services;

import com.vibevault.userservice.exceptions.auth.RoleNotFoundException;
import com.vibevault.userservice.exceptions.role.RoleAlreadyExistsException;
import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RoleServiceImpl implements RoleService {
    private RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    @Override
    public Role createRole(String roleName, String description) {
        Optional<Role> existingRole = roleRepository.findRoleByName(roleName);
        if (existingRole.isPresent()) {
            throw new RoleAlreadyExistsException("Role with name " + roleName + " already exists.");
        }
        Role newRole = new Role();
        newRole.setName(roleName.toUpperCase());
        newRole.setDescription(description);
        return roleRepository.save(newRole);
    }

    @Override
    public Role updateRole(String roleId, String roleName, String description) {
        Optional<Role> existingRole = roleRepository.findById(UUID.fromString(roleId));
        if (existingRole.isEmpty()) {
            throw new RoleNotFoundException("Role with ID " + roleId + " not found.");
        }
        Role roleToUpdate = existingRole.get();
        roleToUpdate.setName(roleName);
        roleToUpdate.setDescription(description);
        return roleRepository.save(roleToUpdate);
    }
}
