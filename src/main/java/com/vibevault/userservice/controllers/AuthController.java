package com.vibevault.userservice.controllers;

import com.vibevault.userservice.dtos.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/signup")
    public SignupResponseDto signup(@RequestBody SignupRequestDto signupRequestDto){
        return null;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto){
        return null;

    }

    @PostMapping("/validate")
    public UserDto validateToken(@RequestHeader("token") String token){
        // Validate the token
        return null;
    }

    @PostMapping("/logout")
    public void logout(@RequestBody LogoutRequestDto logoutRequestDto){
        // Invalidate the token
    }
}
