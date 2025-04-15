package com.vibevault.userservice.services;

import com.vibevault.userservice.exceptions.*;
import com.vibevault.userservice.models.Session;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.repositories.SessionRepository;
import com.vibevault.userservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, SessionRepository sessionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public Session login(String email, String password) {
        return null;
    }

    @Override
    public User signup(String email, String password, String name, String phone)throws EmptyEmailException, EmptyPasswordException, EmptyPhoneException, EmailAlreadyExistsException, PhoneAlreadyExistsException, UserNotFoundException {
        if (email == null || email.isEmpty()) {
            throw new EmptyEmailException("Email cannot be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new EmptyPasswordException("Password cannot be empty");
        }
        if (phone == null || phone.isEmpty()) {
            throw new EmptyPhoneException("Phone number cannot be empty");
        }
        Optional<User> existingUser = userRepository.findUserByEmail(email);
        if(existingUser.isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        Optional<User> existingPhone = userRepository.findUserByPhoneNumber(phone);
        if(existingPhone.isPresent()){
            throw new PhoneAlreadyExistsException("Phone number already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        String[] names = name.split(" ");
        user.setFirstName(names[0]);

        if (names.length > 1) {
            user.setLastName(names[1]);
        } else {
            user.setLastName("");
        }
        user.setPhoneNumber(phone);

        User savedUser = userRepository.save(user);

        // Then manually update the audit fields with the new ID
        return updateUserAuditorFields(savedUser.getId());
    }


    @Override
    public User validateToken(String token) {
        return null;
    }

    @Override
    public void logout(String token) {

    }

    @Transactional
    protected User updateUserAuditorFields(UUID userId)throws UserNotFoundException {
        Optional<User> optionalUser=userRepository.findById(userId);
        User user;
        if(optionalUser.isPresent()){
            user=optionalUser.get();
            user.setCreatedBy(userId);
            user.setLastModifiedBy(userId);
            user= userRepository.save(user);
        }
        else{
            throw new UserNotFoundException("User not found");
        }
        return user;
    }
}
