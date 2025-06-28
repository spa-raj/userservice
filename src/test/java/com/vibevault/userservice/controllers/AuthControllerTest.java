package com.vibevault.userservice.controllers;

import com.vibevault.userservice.dtos.auth.*;
import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;
import com.vibevault.userservice.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignupSuccess() {
        SignupRequestDto requestDto = new SignupRequestDto("test@example.com", "password", "John Doe", "1234567890", "USER");
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setPhoneNumber("1234567890");
        Role role = new Role();
        role.setName("USER");
        role.setDescription("User role");
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);

        when(authService.signup(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(userRole);

        ResponseEntity<SignupResponseDto> response = authController.signup(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("test@example.com", response.getBody().getUserEmail());
        assertEquals("1234567890", response.getBody().getPhone());
        assertTrue(response.getBody().getRole().contains("USER"));
    }

    @Test
    void testLoginSuccess() {
        LoginRequestDto loginRequestDto = new LoginRequestDto("test@example.com", "password");
        LoginResponseDto loginResponseDto = new LoginResponseDto("session123", "token123");

        when(authService.login(anyString(), anyString())).thenReturn(loginResponseDto);

        ResponseEntity<LoginResponseDto> response = authController.login(loginRequestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("session123", response.getBody().getSessionId());
        assertEquals("token123", response.getBody().getToken());
        assertEquals("token123", response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("session123", response.getHeaders().getFirst("Session-Id"));
    }

    @Test
    void testValidateTokenSuccess() {
        String token = "validToken";
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setEmail("jane@example.com");
        user.setPhoneNumber("5551234567");
        Role role1 = new Role();
        role1.setName("ADMIN");
        role1.setDescription("Admin role");
        Role role2 = new Role();
        role2.setName("USER");
        role2.setDescription("User role");
        UserRole userRole1 = new UserRole();
        userRole1.setUser(user);
        userRole1.setRole(role1);
        UserRole userRole2 = new UserRole();
        userRole2.setUser(user);
        userRole2.setRole(role2);

        when(authService.validateToken(token)).thenReturn(List.of(userRole1, userRole2));

        ResponseEntity<UserDto> response = authController.validateToken(token);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jane@example.com", response.getBody().getEmail());
        assertEquals("Jane Smith", response.getBody().getName());
        assertEquals("5551234567", response.getBody().getPhone());
        assertTrue(response.getBody().getRoles().containsAll(List.of("ADMIN", "USER")));
    }

    @Test
    void testSignupInvalidInput() {
        SignupRequestDto requestDto = new SignupRequestDto(null, null, null, null, null);

        when(authService.signup(any(), any(), any(), any(), any())).thenReturn(null);

        ResponseEntity<SignupResponseDto> response = authController.signup(requestDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testLoginUnauthorized() {
        LoginRequestDto loginRequestDto = new LoginRequestDto("wrong@example.com", "wrongpassword");

        when(authService.login(anyString(), anyString())).thenReturn(null);

        ResponseEntity<LoginResponseDto> response = authController.login(loginRequestDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testValidateTokenInvalidOrExpired() {
        String invalidToken = "invalidToken";

        when(authService.validateToken(invalidToken)).thenReturn(Collections.emptyList());

        ResponseEntity<UserDto> response = authController.validateToken(invalidToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testLogoutSuccess() {
        LogoutRequestDto logoutRequestDto = new LogoutRequestDto("token123", "test@example.com");

        doNothing().when(authService).logout(anyString(), anyString());

        ResponseEntity<Void> response = authController.logout(logoutRequestDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testLogoutInvalidOrExpiredToken() {
        LogoutRequestDto logoutRequestDto = new LogoutRequestDto("expiredToken", "test@example.com");

        doNothing().when(authService).logout(anyString(), anyString());

        ResponseEntity<Void> response = authController.logout(logoutRequestDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }
}