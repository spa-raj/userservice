package com.vibevault.userservice.services;

import com.vibevault.userservice.dtos.auth.LoginResponseDto;
import com.vibevault.userservice.exceptions.auth.*;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.models.UserRole;

import java.util.List;

public interface AuthService {
    public LoginResponseDto login(String email, String password)throws InvalidCredentialsException;
    public UserRole signup(String email, String password, String name, String phone, String role)throws EmptyEmailException, EmptyPasswordException, EmptyPhoneException, EmailAlreadyExistsException, PhoneAlreadyExistsException, UserNotFoundException, EmptyRoleException;
    public List<UserRole> validateToken(String token)throws InvalidTokenException, TokenExpiredException, UserNotFoundException;
    public void logout(String email,String token)throws TokenExpiredException,UserNotFoundException,InvalidTokenException, InvalidCredentialsException;
}
