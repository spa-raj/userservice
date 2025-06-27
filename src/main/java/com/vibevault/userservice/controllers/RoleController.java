package com.vibevault.userservice.controllers;

import com.vibevault.userservice.dtos.auth.UserDto;
import com.vibevault.userservice.dtos.role.*;
import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;
import com.vibevault.userservice.services.AuthService;
import com.vibevault.userservice.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private RoleService roleService;
    private AuthService authService;

    @Autowired
    public RoleController(RoleService roleService,
                          AuthService authService) {
        this.roleService = roleService;
        this.authService = authService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateRoleResponseDto> createRole(@RequestBody CreateRoleRequestDto createRoleRequestDto,
                                                            @RequestHeader("Authorization") String authToken) {
        List<UserRole> userRole = authService.validateToken(authToken);
        if (userRole == null || userRole.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        for (UserRole ur : userRole) {
            User user = ur.getUser();
            Role role = ur.getRole();
            if (role.getName().equals("ADMIN")) {
                // Admin can create roles
                break;
            } else {
                // If the user is not an admin, return unauthorized
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
        Role role = roleService.createRole(createRoleRequestDto.getRoleName(),createRoleRequestDto.getDescription());
        CreateRoleResponseDto responseDto = new CreateRoleResponseDto();
        if (role != null) {
            responseDto.setRoleName(role.getName());
            responseDto.setDescription(role.getDescription());
            responseDto.setMessage("Role created successfully");
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } else {
            responseDto.setMessage("Failed to create role");
            return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/update/{roleId}")
    public ResponseEntity<UpdateRoleResponseDto> updateRole(@PathVariable String roleId,
                                           @RequestBody UpdateRoleRequestDto updateRoleRequestDto,
                                           @RequestHeader("Authorization") String authToken) {
        List<UserRole> userRole = authService.validateToken(authToken);
        if (userRole == null || userRole.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        for (UserRole ur : userRole) {
            User user = ur.getUser();
            Role role = ur.getRole();
            if (role.getName().equals("ADMIN")) {
                // Admin can update roles
                break;
            } else {
                // If the user is not an admin, return unauthorized
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
        Role updatedRole = roleService.updateRole(roleId, updateRoleRequestDto.getRoleName(), updateRoleRequestDto.getDescription());
        UpdateRoleResponseDto updateRoleResponseDto;
        if (updatedRole != null) {
            updateRoleResponseDto = new UpdateRoleResponseDto(updatedRole.getName(),
                    updatedRole.getDescription());
            return new ResponseEntity<>(updateRoleResponseDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("")
    public ResponseEntity<List<GetRoleResponseDto>> getAllRoles(@RequestHeader("Authorization") String authToken) {
        List<UserRole> userRole = authService.validateToken(authToken);
        if (userRole == null || userRole.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        for (UserRole ur : userRole) {
            User user = ur.getUser();
            Role role = ur.getRole();
            if (role.getName().equals("ADMIN")) {
                // Admin can view all roles
                break;
            } else {
                // If the user is not an admin, return unauthorized
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
        List<Role> roles = roleService.getAllRoles();
        return new ResponseEntity<>(GetRoleResponseDto.fromRoles(roles), HttpStatus.OK);
    }

}
