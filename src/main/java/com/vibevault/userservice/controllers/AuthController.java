package com.vibevault.userservice.controllers;

import com.vibevault.userservice.dtos.*;
import com.vibevault.userservice.models.Session;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto signupRequestDto){
        User user = authService.signup(signupRequestDto.getEmail(),
                signupRequestDto.getPassword(),
                signupRequestDto.getName(),
                signupRequestDto.getPhone());

        if(user == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        SignupResponseDto signupResponseDto = new SignupResponseDto();
        signupResponseDto.setName(user.getFirstName()+" "+user.getLastName());
        signupResponseDto.setUserEmail(user.getEmail());
        signupResponseDto.setPhone(user.getPhoneNumber());

        return new ResponseEntity<>(signupResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        MultiValueMap<String, String> responseHeaders = new LinkedMultiValueMap<>();
        if(loginResponseDto == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        responseHeaders.put(HttpHeaders.AUTHORIZATION, Collections.singletonList(loginResponseDto.getToken()));
//        responseHeaders.put(HttpHeaders.SET_COOKIE, loginResponseDto.getToken());
        responseHeaders.put("Session-Id", Collections.singletonList(loginResponseDto.getSessionId()));

        HttpHeaders headers = new HttpHeaders(responseHeaders);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(loginResponseDto);
    }

    @PostMapping("/validate")
    public ResponseEntity<UserDto> validateToken(@RequestHeader("Authorization") String token){
        // Validate the token
        User user = authService.validateToken(token);
        if(user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getFirstName() + " " + user.getLastName());
        userDto.setPhone(user.getPhoneNumber());
        return new ResponseEntity<>(userDto, HttpStatus.ACCEPTED);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto logoutRequestDto){
        // Invalidate the token
        authService.logout(logoutRequestDto.getUserEmail(),logoutRequestDto.getToken());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
