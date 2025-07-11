package com.vibevault.userservice.services.utils;

import com.vibevault.userservice.exceptions.auth.RoleNotFoundException;
import com.vibevault.userservice.exceptions.auth.UserNotFoundException;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;
import com.vibevault.userservice.repositories.RoleRepository;
import com.vibevault.userservice.repositories.UserRepository;
import com.vibevault.userservice.repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRoles {
    // Fetch the list of roles for a user
    private RoleRepository roleRepository;
    private UserRoleRepository userRoleRepository;

    @Autowired
    public UserRoles(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public List<String> getRolesForUser(User user){
        if(user == null || user.getId()== null) {
            throw new UserNotFoundException("User not found or invalid user ID.");
        }
        List<UserRole> userRoles = userRoleRepository.findUserRolesByUser_Id(user.getId());

        if (userRoles == null || userRoles.isEmpty()) {
            throw new UserNotFoundException("No roles found for user with ID: " + user.getId());
        }
        return userRoles.stream()
                .map(userRole -> roleRepository.findById(userRole.getRole().getId())
                        .orElseThrow(() -> new RoleNotFoundException("Role not found for ID: " + userRole.getRole().getId()))
                        .getName())
                .toList();
    }
}
