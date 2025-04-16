package com.vibevault.userservice.services;

import com.vibevault.userservice.exceptions.*;
import com.vibevault.userservice.models.Session;
import com.vibevault.userservice.models.User;

public interface AuthService {
    public Session login(String email, String password)throws InvalidCredentialsException;
    public User signup(String email, String password, String name, String phone)throws EmptyEmailException, EmptyPasswordException, EmptyPhoneException, EmailAlreadyExistsException, PhoneAlreadyExistsException, UserNotFoundException;
    public User validateToken(String token)throws InvalidTokenException, TokenExpiredException, UserNotFoundException;
    public void logout(String token);
}
