package com.vibevault.userservice.controllers;

import tools.jackson.databind.json.JsonMapper;
import com.vibevault.userservice.dtos.auth.LoginRequestDto;
import com.vibevault.userservice.dtos.auth.LoginResponseDto;
import com.vibevault.userservice.dtos.auth.LogoutRequestDto;
import com.vibevault.userservice.dtos.auth.SignupRequestDto;

import com.vibevault.userservice.models.Role;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;
import com.vibevault.userservice.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private JsonMapper jsonMapper;

    private User user;
    private Role role;
    private UserRole userRole;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhoneNumber("1234567890");

        role = new Role();
        role.setName("USER");
        role.setDescription("Standard user");

        userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
    }

    @Test
    void testSignupSuccess() throws Exception {
        SignupRequestDto signupRequest = new SignupRequestDto();
        signupRequest.setEmail("john.doe@example.com");
        signupRequest.setPassword("password");
        signupRequest.setName("John Doe");
        signupRequest.setPhone("1234567890");
        signupRequest.setRole("USER");

        when(authService.signup(
                eq("john.doe@example.com"),
                eq("password"),
                eq("John Doe"),
                eq("1234567890"),
                eq("USER")
        )).thenReturn(userRole);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.userEmail").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.role").value(role.toString()));
    }

    @Test
    void testSignupFailure() throws Exception {
        SignupRequestDto signupRequest = new SignupRequestDto();
        signupRequest.setEmail("fail@example.com");
        signupRequest.setPassword("password");
        signupRequest.setName("Fail User");
        signupRequest.setPhone("0000000000");
        signupRequest.setRole("USER");

        when(authService.signup(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password");

        LoginResponseDto loginResponse = new LoginResponseDto();
        loginResponse.setToken("mock-token");
        loginResponse.setSessionId("mock-session-id");

        when(authService.login(eq("john.doe@example.com"), eq("password")))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "mock-token"))
                .andExpect(header().string("Session-Id", "mock-session-id"))
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.SessionId").value("mock-session-id"));
    }

    @Test
    void testLoginFailure() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("fail@example.com");
        loginRequest.setPassword("wrong");

        when(authService.login(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testValidateTokenSuccess() throws Exception {
        String token = "mock-token";
        List<UserRole> userRoles = Collections.singletonList(userRole);

        when(authService.validateToken(eq(token))).thenReturn(userRoles);

        mockMvc.perform(post("/auth/validate")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void testValidateTokenFailure() throws Exception {
        String token = "invalid-token";
        when(authService.validateToken(eq(token))).thenReturn(null);

        mockMvc.perform(post("/auth/validate")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogout() throws Exception {
        LogoutRequestDto logoutRequest = new LogoutRequestDto();
        logoutRequest.setUserEmail("john.doe@example.com");
        logoutRequest.setToken("mock-token");

        doNothing().when(authService).logout(eq("john.doe@example.com"), eq("mock-token"));

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());
    }
}