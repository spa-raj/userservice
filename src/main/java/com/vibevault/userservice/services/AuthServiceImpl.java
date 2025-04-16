package com.vibevault.userservice.services;

import com.vibevault.userservice.exceptions.*;
import com.vibevault.userservice.models.Session;
import com.vibevault.userservice.models.SessionStatus;
import com.vibevault.userservice.models.User;
import com.vibevault.userservice.repositories.SessionRepository;
import com.vibevault.userservice.repositories.UserRepository;
import com.vibevault.userservice.services.utils.ClientInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {
    private final SessionRepository sessionRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
    }
    @Override
    public Session login(String email, String password)throws InvalidCredentialsException {
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        if(optionalUser.isEmpty()){
            throw new InvalidCredentialsException("Invalid credentials");
        }
        User user = optionalUser.get();
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return createSession(user);
    }

    private Session createSession(User user) {
        Session session = new Session();
        session.setUser(user);
        session.setToken(RandomStringUtils.random(16,0,100,true,true,null,new SecureRandom()));
        session.setDeleted(false);

        // Set the session expiration time to 1 hour from now
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        session.setExpiredAt(calendar.getTime());
        session.setStatus(SessionStatus.ACTIVE);
        session.setIpAddress(ClientInfo.getClientIpAddress());
        session.setDevice(ClientInfo.getUserAgent());
        session.setCreatedBy(user.getId());
        session.setLastModifiedBy(user.getId());

        return sessionRepository.save(session);
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
    public User validateToken(String token)throws InvalidTokenException,TokenExpiredException,UserNotFoundException {
        Optional<Session> optionalSession = sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE);
        if(optionalSession.isEmpty()){
            throw new InvalidTokenException("Invalid token");
        }
        Session session = optionalSession.get();
        if(session.getExpiredAt().before(new Date())){
            throw new TokenExpiredException("Token expired");
        }
        User user = session.getUser();
        if(user == null){
            throw new UserNotFoundException("User not found");
        }
        return user;
    }

    @Override
    public void logout(String email,String token)throws TokenExpiredException,UserNotFoundException,InvalidTokenException, InvalidCredentialsException {
        Optional<Session> optionalSession = sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE);
        if(optionalSession.isPresent()){
            Session session = optionalSession.get();
            if(session.getExpiredAt().before(new Date())){
                throw new TokenExpiredException("Token already expired. Can't logout.");
            }
            User user = session.getUser();
            if(user == null){
                throw new UserNotFoundException("User not found");
            }
            if(!user.getEmail().equals(email)){
                throw new InvalidCredentialsException("Token does not belong to the user");
            }
            session.setDeleted(true);
            session.setStatus(SessionStatus.LOGGED_OUT);
            sessionRepository.save(session);
        }
        else{
            throw new InvalidTokenException("Invalid token");
        }
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
