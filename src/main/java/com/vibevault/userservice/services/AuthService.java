package com.vibevault.userservice.services;

import com.vibevault.userservice.dtos.LoginResponseDto;
import com.vibevault.userservice.exceptions.*;
import com.vibevault.userservice.models.Session;
import com.vibevault.userservice.models.User;

public interface AuthService {
    public LoginResponseDto login(String email, String password)throws InvalidCredentialsException;
    public User signup(String email, String password, String name, String phone)throws EmptyEmailException, EmptyPasswordException, EmptyPhoneException, EmailAlreadyExistsException, PhoneAlreadyExistsException, UserNotFoundException;
    public User validateToken(String token)throws InvalidTokenException, TokenExpiredException, UserNotFoundException;
    public void logout(String email,String token)throws TokenExpiredException,UserNotFoundException,InvalidTokenException, InvalidCredentialsException;
}
