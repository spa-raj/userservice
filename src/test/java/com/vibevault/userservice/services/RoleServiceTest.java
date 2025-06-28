package com.vibevault.userservice.services;

import com.vibevault.userservice.exceptions.auth.RoleNotFoundException;
import com.vibevault.userservice.exceptions.role.RoleAlreadyExistsException;
import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRoleWithUniqueName() {
        String roleName = "admin";
        String description = "Administrator role";
        when(roleRepository.findRoleByName(roleName)).thenReturn(Optional.empty());

        Role savedRole = new Role();
        savedRole.setName(roleName.toUpperCase());
        savedRole.setDescription(description);

        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        Role result = roleService.createRole(roleName, description);

        assertEquals(roleName.toUpperCase(), result.getName());
        assertEquals(description, result.getDescription());
        verify(roleRepository).findRoleByName(roleName);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testUpdateExistingRole() {
        String roleId = UUID.randomUUID().toString();
        String oldName = "USER";
        String oldDescription = "Old description";
        String newName = "manager";
        String newDescription = "Manager role";

        Role existingRole = new Role();
        existingRole.setName(oldName);
        existingRole.setDescription(oldDescription);

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.of(existingRole));

        Role updatedRole = new Role();
        updatedRole.setName(newName.toUpperCase());
        updatedRole.setDescription(newDescription);

        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);

        Role result = roleService.updateRole(roleId, newName, newDescription);

        assertEquals(newName.toUpperCase(), result.getName());
        assertEquals(newDescription, result.getDescription());
        verify(roleRepository).findById(UUID.fromString(roleId));
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testGetAllRolesWithExistingRoles() {
        Role role1 = new Role();
        role1.setName("ADMIN");
        role1.setDescription("Administrator");

        Role role2 = new Role();
        role2.setName("USER");
        role2.setDescription("User");

        List<Role> roles = Arrays.asList(role1, role2);

        when(roleRepository.findAll()).thenReturn(roles);

        List<Role> result = roleService.getAllRoles();

        assertEquals(2, result.size());
        assertTrue(result.contains(role1));
        assertTrue(result.contains(role2));
        verify(roleRepository).findAll();
    }

    @Test
    void testCreateRoleWithDuplicateNameThrowsException() {
        String roleName = "admin";
        String description = "Administrator role";
        Role existingRole = new Role();
        existingRole.setName(roleName.toUpperCase());
        existingRole.setDescription(description);

        when(roleRepository.findRoleByName(roleName)).thenReturn(Optional.of(existingRole));

        assertThrows(RoleAlreadyExistsException.class, () -> {
            roleService.createRole(roleName, description);
        });

        verify(roleRepository).findRoleByName(roleName);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testUpdateNonExistentRoleThrowsException() {
        String roleId = UUID.randomUUID().toString();
        String newName = "manager";
        String newDescription = "Manager role";

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> {
            roleService.updateRole(roleId, newName, newDescription);
        });

        verify(roleRepository).findById(UUID.fromString(roleId));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testGetAllRolesWhenNoneExistThrowsException() {
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(RoleNotFoundException.class, () -> {
            roleService.getAllRoles();
        });

        verify(roleRepository).findAll();
    }

    @Test
    void testGetRoleByIdWithValidId() {
        String roleId = UUID.randomUUID().toString();
        Role expectedRole = new Role();
        expectedRole.setName("ADMIN");
        expectedRole.setDescription("Administrator role");

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.of(expectedRole));

        Role result = roleService.getRoleById(roleId);

        assertEquals(expectedRole.getName(), result.getName());
        assertEquals(expectedRole.getDescription(), result.getDescription());
        verify(roleRepository).findById(UUID.fromString(roleId));
    }

    @Test
    void testGetRoleByIdWithInvalidIdThrowsException() {
        String roleId = UUID.randomUUID().toString();

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> {
            roleService.getRoleById(roleId);
        });

        verify(roleRepository).findById(UUID.fromString(roleId));
    }

    @Test
    void testUpdateRoleWithNullName() {
        String roleId = UUID.randomUUID().toString();
        String oldName = "USER";
        String oldDescription = "Old description";
        String newDescription = "Updated description";

        Role existingRole = new Role();
        existingRole.setName(oldName);
        existingRole.setDescription(oldDescription);

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        Role result = roleService.updateRole(roleId, null, newDescription);

        // Name should remain unchanged when null is passed
        assertEquals(oldName, existingRole.getName());
        assertEquals(newDescription, existingRole.getDescription());
        verify(roleRepository).findById(UUID.fromString(roleId));
        verify(roleRepository).save(existingRole);
    }

    @Test
    void testUpdateRoleWithEmptyName() {
        String roleId = UUID.randomUUID().toString();
        String oldName = "USER";
        String oldDescription = "Old description";
        String newDescription = "Updated description";

        Role existingRole = new Role();
        existingRole.setName(oldName);
        existingRole.setDescription(oldDescription);

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        Role result = roleService.updateRole(roleId, "", newDescription);

        // Name should remain unchanged when empty string is passed
        assertEquals(oldName, existingRole.getName());
        assertEquals(newDescription, existingRole.getDescription());
        verify(roleRepository).findById(UUID.fromString(roleId));
        verify(roleRepository).save(existingRole);
    }

    @Test
    void testUpdateRoleWithNullDescription() {
        String roleId = UUID.randomUUID().toString();
        String oldName = "USER";
        String oldDescription = "Old description";
        String newName = "MANAGER";

        Role existingRole = new Role();
        existingRole.setName(oldName);
        existingRole.setDescription(oldDescription);

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        Role result = roleService.updateRole(roleId, newName, null);

        // Description should remain unchanged when null is passed
        assertEquals(newName.toUpperCase(), existingRole.getName());
        assertEquals(oldDescription, existingRole.getDescription());
        verify(roleRepository).findById(UUID.fromString(roleId));
        verify(roleRepository).save(existingRole);
    }

    @Test
    void testUpdateRoleWithEmptyDescription() {
        String roleId = UUID.randomUUID().toString();
        String oldName = "USER";
        String oldDescription = "Old description";
        String newName = "MANAGER";

        Role existingRole = new Role();
        existingRole.setName(oldName);
        existingRole.setDescription(oldDescription);

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        Role result = roleService.updateRole(roleId, newName, "");

        // Description should remain unchanged when empty string is passed
        assertEquals(newName.toUpperCase(), existingRole.getName());
        assertEquals(oldDescription, existingRole.getDescription());
        verify(roleRepository).findById(UUID.fromString(roleId));
        verify(roleRepository).save(existingRole);
    }

    @Test
    void testUpdateRoleWithSameName() {
        String roleId = UUID.randomUUID().toString();
        String existingName = "USER";
        String oldDescription = "Old description";
        String newDescription = "Updated description";

        Role existingRole = new Role();
        existingRole.setName(existingName);
        existingRole.setDescription(oldDescription);

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        Role result = roleService.updateRole(roleId, existingName.toLowerCase(), newDescription);

        // Name should remain unchanged when same name is passed (case insensitive)
        assertEquals(existingName, existingRole.getName());
        assertEquals(newDescription, existingRole.getDescription());
        verify(roleRepository).findById(UUID.fromString(roleId));
        verify(roleRepository).save(existingRole);
    }

    @Test
    void testUpdateRoleWithSameDescription() {
        String roleId = UUID.randomUUID().toString();
        String oldName = "USER";
        String existingDescription = "Existing description";
        String newName = "MANAGER";

        Role existingRole = new Role();
        existingRole.setName(oldName);
        existingRole.setDescription(existingDescription);

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        Role result = roleService.updateRole(roleId, newName, existingDescription);

        // Description should remain unchanged when same description is passed
        assertEquals(newName.toUpperCase(), existingRole.getName());
        assertEquals(existingDescription, existingRole.getDescription());
        verify(roleRepository).findById(UUID.fromString(roleId));
        verify(roleRepository).save(existingRole);
    }

    @Test
    void testUpdateRoleWithNoChanges() {
        String roleId = UUID.randomUUID().toString();
        String existingName = "USER";
        String existingDescription = "Existing description";

        Role existingRole = new Role();
        existingRole.setName(existingName);
        existingRole.setDescription(existingDescription);

        when(roleRepository.findById(UUID.fromString(roleId))).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        Role result = roleService.updateRole(roleId, existingName, existingDescription);

        // Both name and description should remain unchanged
        assertEquals(existingName, existingRole.getName());
        assertEquals(existingDescription, existingRole.getDescription());
        verify(roleRepository).findById(UUID.fromString(roleId));
        verify(roleRepository).save(existingRole);
    }

    @Test
    void testCreateRoleWithLowercaseName() {
        String roleName = "manager";
        String description = "Manager role";
        when(roleRepository.findRoleByName(roleName)).thenReturn(Optional.empty());

        Role savedRole = new Role();
        savedRole.setName(roleName.toUpperCase());
        savedRole.setDescription(description);

        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        Role result = roleService.createRole(roleName, description);

        // Name should be converted to uppercase
        assertEquals("MANAGER", result.getName());
        assertEquals(description, result.getDescription());
        verify(roleRepository).findRoleByName(roleName);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testCreateRoleWithMixedCaseName() {
        String roleName = "SuPeRaDmIn";
        String description = "Super admin role";
        when(roleRepository.findRoleByName(roleName)).thenReturn(Optional.empty());

        Role savedRole = new Role();
        savedRole.setName(roleName.toUpperCase());
        savedRole.setDescription(description);

        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        Role result = roleService.createRole(roleName, description);

        // Name should be converted to uppercase
        assertEquals("SUPERADMIN", result.getName());
        assertEquals(description, result.getDescription());
        verify(roleRepository).findRoleByName(roleName);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testGetAllRolesWithSingleRole() {
        Role role = new Role();
        role.setName("ADMIN");
        role.setDescription("Administrator");

        List<Role> roles = Collections.singletonList(role);

        when(roleRepository.findAll()).thenReturn(roles);

        List<Role> result = roleService.getAllRoles();

        assertEquals(1, result.size());
        assertEquals(role, result.get(0));
        verify(roleRepository).findAll();
    }
}

