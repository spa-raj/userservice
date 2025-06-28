package com.vibevault.userservice.controllers;

import com.vibevault.userservice.dtos.role.*;
import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;
import com.vibevault.userservice.services.AuthService;
import com.vibevault.userservice.services.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private RoleController roleController;

    private AutoCloseable closeable;

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    private static final String AUTH_TOKEN = "Bearer testtoken";
    private static final String ROLE_ID = UUID.randomUUID().toString();

    private UserRole adminUserRole;
    private UserRole userUserRole;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

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

    @Test
    void createRole_shouldReturnCreated_whenAdmin() {
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto();
        requestDto.setRoleName("MODERATOR");
        requestDto.setDescription("Moderator role");

        Role createdRole = new Role();
        createdRole.setName("MODERATOR");
        createdRole.setDescription("Moderator role");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.createRole("MODERATOR", "Moderator role")).thenReturn(createdRole);

        ResponseEntity<CreateRoleResponseDto> response = roleController.createRole(requestDto, AUTH_TOKEN);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MODERATOR", response.getBody().getRoleName());
        assertEquals("Moderator role", response.getBody().getDescription());
        assertEquals("Role created successfully", response.getBody().getMessage());
    }

    @Test
    void createRole_shouldReturnForbidden_whenNotAdmin() {
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto();
        requestDto.setRoleName("MODERATOR");
        requestDto.setDescription("Moderator role");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(userUserRole));

        ResponseEntity<CreateRoleResponseDto> response = roleController.createRole(requestDto, AUTH_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createRole_shouldReturnUnauthorized_whenNoUserRole() {
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto();
        requestDto.setRoleName("MODERATOR");
        requestDto.setDescription("Moderator role");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.emptyList());

        ResponseEntity<CreateRoleResponseDto> response = roleController.createRole(requestDto, AUTH_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void updateRole_shouldReturnOk_whenAdmin() {
        UpdateRoleRequestDto requestDto = new UpdateRoleRequestDto("MODERATOR", "Updated desc");
        Role updatedRole = new Role();
        updatedRole.setName("MODERATOR");
        updatedRole.setDescription("Updated desc");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.updateRole(ROLE_ID, "MODERATOR", "Updated desc")).thenReturn(updatedRole);

        ResponseEntity<UpdateRoleResponseDto> response = roleController.updateRole(ROLE_ID, requestDto, AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MODERATOR", response.getBody().getRoleName());
        assertEquals("Updated desc", response.getBody().getDescription());
        assertEquals("Role updated successfully", response.getBody().getMessage());
    }

    @Test
    void updateRole_shouldReturnForbidden_whenNotAdmin() {
        UpdateRoleRequestDto requestDto = new UpdateRoleRequestDto("MODERATOR", "Updated desc");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(userUserRole));

        ResponseEntity<UpdateRoleResponseDto> response = roleController.updateRole(ROLE_ID, requestDto, AUTH_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateRole_shouldReturnUnauthorized_whenNoUserRole() {
        UpdateRoleRequestDto requestDto = new UpdateRoleRequestDto("MODERATOR", "Updated desc");

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.emptyList());

        ResponseEntity<UpdateRoleResponseDto> response = roleController.updateRole(ROLE_ID, requestDto, AUTH_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getAllRoles_shouldReturnOk_whenAdmin() {
        List<Role> roles = Arrays.asList(
                new Role("ADMIN", "Administrator"),
                new Role("USER", "Regular user")
        );

        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.getAllRoles()).thenReturn(roles);

        ResponseEntity<List<GetRoleResponseDto>> response = roleController.getAllRoles(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("ADMIN", response.getBody().get(0).getRoleName());
    }

    @Test
    void getAllRoles_shouldReturnForbidden_whenNotAdmin() {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(userUserRole));

        ResponseEntity<List<GetRoleResponseDto>> response = roleController.getAllRoles(AUTH_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllRoles_shouldReturnUnauthorized_whenNoUserRole() {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.emptyList());

        ResponseEntity<List<GetRoleResponseDto>> response = roleController.getAllRoles(AUTH_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getRoleById_shouldReturnOk_whenAdmin() {
        Role role = new Role("ADMIN", "Administrator");
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.getRoleById(ROLE_ID)).thenReturn(role);

        ResponseEntity<GetRoleResponseDto> response = roleController.getRoleById(ROLE_ID, AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ADMIN", response.getBody().getRoleName());
    }

    @Test
    void getRoleById_shouldReturnForbidden_whenNotAdmin() {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(userUserRole));

        ResponseEntity<GetRoleResponseDto> response = roleController.getRoleById(ROLE_ID, AUTH_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getRoleById_shouldReturnUnauthorized_whenNoUserRole() {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.emptyList());

        ResponseEntity<GetRoleResponseDto> response = roleController.getRoleById(ROLE_ID, AUTH_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getRoleById_shouldReturnNotFound_whenRoleServiceReturnsNull() {
        when(authService.validateToken(AUTH_TOKEN)).thenReturn(Collections.singletonList(adminUserRole));
        when(roleService.getRoleById(ROLE_ID)).thenReturn(null);

        ResponseEntity<GetRoleResponseDto> response = roleController.getRoleById(ROLE_ID, AUTH_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}