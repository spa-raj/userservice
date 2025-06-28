package com.vibevault.userservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibevault.userservice.dtos.role.CreateRoleRequestDto;
import com.vibevault.userservice.dtos.role.UpdateRoleRequestDto;
import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;
import com.vibevault.userservice.services.AuthService;
import com.vibevault.userservice.services.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = RoleController.class, excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class })
public class RoleControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_TOKEN = "Bearer testtoken";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    private static final String ROLE_ID = UUID.randomUUID().toString();

    private UserRole adminUserRole;
    private UserRole userUserRole;

    @BeforeEach
    void setUp() {
        Role adminRole = new Role();
        adminRole.setName(ADMIN_ROLE);
        adminRole.setDescription("Administrator");

        Role userRole = new Role();
        userRole.setName(USER_ROLE);
        userRole.setDescription("Regular user");

        adminUserRole = new UserRole();
        adminUserRole.setRole(adminRole);

        userUserRole = new UserRole();
        userUserRole.setRole(userRole);
    }

    // --- CREATE ROLE ---

    @Test
    void createRole_shouldReturnCreated_whenAdmin() throws Exception {
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto();
        requestDto.setRoleName("MODERATOR");
        requestDto.setDescription("Moderator role");

        Role createdRole = new Role();
        createdRole.setName("MODERATOR");
        createdRole.setDescription("Moderator role");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.createRole(anyString(), anyString())).thenReturn(createdRole);

        mockMvc.perform(post("/roles/create")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleName").value("MODERATOR"))
                .andExpect(jsonPath("$.description").value("Moderator role"))
                .andExpect(jsonPath("$.message").value("Role created successfully"));
    }

    @Test
    void createRole_shouldReturnForbidden_whenNotAdmin() throws Exception {
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto();
        requestDto.setRoleName("MODERATOR");
        requestDto.setDescription("Moderator role");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(userUserRole));

        mockMvc.perform(post("/roles/create")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRole_shouldReturnUnauthorized_whenNoUserRole() throws Exception {
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto();
        requestDto.setRoleName("MODERATOR");
        requestDto.setDescription("Moderator role");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/roles/create")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    // --- UPDATE ROLE ---

    @Test
    void updateRole_shouldReturnOk_whenAdmin() throws Exception {
        UpdateRoleRequestDto requestDto = new UpdateRoleRequestDto("MODERATOR", "Updated desc");
        Role updatedRole = new Role();
        updatedRole.setName("MODERATOR");
        updatedRole.setDescription("Updated desc");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.updateRole(eq(ROLE_ID), anyString(), anyString())).thenReturn(updatedRole);

        mockMvc.perform(post("/roles/update/{roleId}", ROLE_ID)
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("MODERATOR"))
                .andExpect(jsonPath("$.description").value("Updated desc"))
                .andExpect(jsonPath("$.message").value("Role updated successfully"));
    }

    @Test
    void updateRole_shouldReturnForbidden_whenNotAdmin() throws Exception {
        UpdateRoleRequestDto requestDto = new UpdateRoleRequestDto("MODERATOR", "Updated desc");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(userUserRole));

        mockMvc.perform(post("/roles/update/{roleId}", ROLE_ID)
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateRole_shouldReturnUnauthorized_whenNoUserRole() throws Exception {
        UpdateRoleRequestDto requestDto = new UpdateRoleRequestDto("MODERATOR", "Updated desc");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/roles/update/{roleId}", ROLE_ID)
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateRole_shouldReturnNotFound_whenRoleServiceReturnsNull() throws Exception {
        UpdateRoleRequestDto requestDto = new UpdateRoleRequestDto("MODERATOR", "Updated desc");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.updateRole(eq(ROLE_ID), anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/roles/update/{roleId}", ROLE_ID)
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    // --- GET ALL ROLES ---

    @Test
    void getAllRoles_shouldReturnOk_whenAdmin() throws Exception {
        List<Role> roles = Arrays.asList(
                new Role("ADMIN", "Administrator"),
                new Role("USER", "Regular user")
        );

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.getAllRoles()).thenReturn(roles);

        mockMvc.perform(get("/roles")
                .header(AUTH_HEADER, AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$[1].roleName").value("USER"));
    }

    @Test
    void getAllRoles_shouldReturnForbidden_whenNotAdmin() throws Exception {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(userUserRole));

        mockMvc.perform(get("/roles")
                .header(AUTH_HEADER, AUTH_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllRoles_shouldReturnUnauthorized_whenNoUserRole() throws Exception {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/roles")
                .header(AUTH_HEADER, AUTH_TOKEN))
                .andExpect(status().isUnauthorized());
    }

    // --- GET ROLE BY ID ---

    @Test
    void getRoleById_shouldReturnOk_whenAdmin() throws Exception {
        Role role = new Role("ADMIN", "Administrator");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.getRoleById(ROLE_ID)).thenReturn(role);

        mockMvc.perform(get("/roles/{roleId}", ROLE_ID)
                .header(AUTH_HEADER, AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("ADMIN"))
                .andExpect(jsonPath("$.description").value("Administrator"));
    }

    @Test
    void getRoleById_shouldReturnForbidden_whenNotAdmin() throws Exception {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(userUserRole));

        mockMvc.perform(get("/roles/{roleId}", ROLE_ID)
                .header(AUTH_HEADER, AUTH_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRoleById_shouldReturnUnauthorized_whenNoUserRole() throws Exception {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/roles/{roleId}", ROLE_ID)
                .header(AUTH_HEADER, AUTH_TOKEN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRoleById_shouldReturnNotFound_whenRoleServiceReturnsNull() throws Exception {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.getRoleById(ROLE_ID)).thenReturn(null);

        mockMvc.perform(get("/roles/{roleId}", ROLE_ID)
                .header(AUTH_HEADER, AUTH_TOKEN))
                .andExpect(status().isNotFound());
    }
}