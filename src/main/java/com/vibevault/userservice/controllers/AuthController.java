package com.vibevault.userservice.controllers;

import com.vibevault.userservice.dtos.*;
import com.vibevault.userservice.models.Session;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return null;
    }

    @PostMapping("/validate")
    public ResponseEntity<UserDto> validateToken(@RequestHeader("token") String token){
        // Validate the token
        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto logoutRequestDto){
        // Invalidate the token
        return null;
    }
}
