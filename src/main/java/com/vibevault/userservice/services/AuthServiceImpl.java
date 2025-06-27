package com.vibevault.userservice.services;

import com.vibevault.userservice.dtos.auth.LoginResponseDto;
import com.vibevault.userservice.exceptions.auth.*;
import com.vibevault.userservice.models.*;
import com.vibevault.userservice.repositories.*;
import com.vibevault.userservice.services.utils.ClientInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;

import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {
    private final SessionRepository sessionRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JWTRepository jwtRepository;
    private  KeyLocatorImpl keyLocator;
    private RoleRepository roleRepository;
    private UserRoleRepository userRoleRepository;
    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           SessionRepository sessionRepository,
                           JWTRepository jwtRepository,
                           KeyLocatorImpl keyLocator,
                           RoleRepository roleRepository,
                           UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
        this.jwtRepository = jwtRepository;
        this.keyLocator = keyLocator;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }
    @Override
    public LoginResponseDto login(String email, String password)throws InvalidCredentialsException {
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        if(optionalUser.isEmpty()){
            throw new InvalidCredentialsException("Invalid credentials");
        }
        User user = optionalUser.get();
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new InvalidCredentialsException("Invalid credentials");
        }

        Optional<List<UserRole>> optionalUserRoleList = userRoleRepository.findUserRoleByUser_Id(user.getId());
        if(optionalUserRoleList.isEmpty()) {
               throw new InvalidUserException("User does not have a role assigned");
        }
        List<UserRole> userRoleList = optionalUserRoleList.get();
        Session session = createSession(userRoleList);
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setToken(session.getToken());
        loginResponseDto.setSessionId(String.valueOf(session.getId()));

        return loginResponseDto;
    }

    private Session createSession(List<UserRole> userRoleList) {
        Session session = new Session();
        User user = userRoleList.getFirst().getUser();
        if (user == null || user.getId() == null || user.isDeleted()) {
            throw new UserNotFoundException("User not found");
        }

        List<Role> roles = new ArrayList<>();
        for (UserRole userRole : userRoleList) {
            Role role = userRole.getRole();
            if (role != null && !role.isDeleted()) {
                roles.add(role);
            }
        }

        if (roles.isEmpty() || roles.stream().anyMatch(Role::isDeleted) || roles.stream().anyMatch(role -> role.getId() == null)) {
            throw new RoleNotFoundException("Role not found");
        }

        session.setUser(user);
        session.setRole(roles);

        String jws = getJWT(userRoleList);
        session.setToken(jws);
        session.setDeleted(false);

        // Set the session expiration time to 1 day from now
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 24);
        session.setExpiredAt(calendar.getTime());
        session.setStatus(SessionStatus.ACTIVE);
        session.setIpAddress(ClientInfo.getClientIpAddress());
        session.setDevice(ClientInfo.getUserAgent());
        session.setCreatedBy(user.getId());
        session.setLastModifiedBy(user.getId());

        return sessionRepository.save(session);
    }

    private String getJWT(List<UserRole> userRoleList) {
        User user = userRoleList.getFirst().getUser();
        List<Role> roles = new ArrayList<>();
        for (UserRole userRole : userRoleList) {
            Role role = userRole.getRole();
            if (role != null && !role.isDeleted()) {
                roles.add(role);
            }
        }

        MacAlgorithm alg = Jwts.SIG.HS512;
        SecretKey key=null;
        String kid=null;

        Optional<JWT> optionalJWT = jwtRepository.findTopByUser_IdEqualsAndDeletedEquals(user.getId(),false);

        boolean createNewKey = true;
        if (optionalJWT.isPresent()) {
            JWT existingJwt = optionalJWT.get();
            Instant keyCreationTime = existingJwt.getCreatedAt().toInstant();
            Instant now = Instant.now();

            if (Duration.between(keyCreationTime, now).toDays() < Consts.JWT_SECRET_EXPIRATION_TIME_IN_DAYS) {
                try {
                    byte[] decodedKey = Base64.getDecoder().decode(existingJwt.getSecret());
                    key = Keys.hmacShaKeyFor(decodedKey);
                    kid = existingJwt.getId().toString();
                    createNewKey = false;
                } catch (IllegalArgumentException e) {
                    System.err.println("Error decoding existing key, creating a new one: " + e.getMessage());
                }
            }
        }
        if(createNewKey) {
            List<JWT> oldActiveJwts = jwtRepository.findAllByUser_IdAndDeletedIs(user.getId(), false);
            for (JWT oldJwt : oldActiveJwts) {
                oldJwt.setDeleted(true);
            }
            if (!oldActiveJwts.isEmpty()) {
                jwtRepository.saveAll(oldActiveJwts);
            }

            key = alg.key().build();
            JWT jwtRecord = new JWT();
            jwtRecord.setSecret(Base64.getEncoder().encodeToString(key.getEncoded()));
            jwtRecord.setUser(user);
            jwtRecord.setCreatedAt(new Date());
            jwtRecord.setDeleted(false);

            JWT savedJwt = jwtRepository.save(jwtRecord);
            kid = savedJwt.getId().toString();
        }

        String jws = Jwts.builder()
                        .header().keyId(kid)
                        .and()
                        .issuer(Consts.JWT_ISSUER)
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + Consts.JWT_EXPIRATION_TIME))
                        .subject(user.getId().toString())
                        .claim("email", user.getEmail())
                        .claim("roles", roles.stream()
                                .map(Role::getName)
                                .toList())
                        .audience()
                        .add(Consts.JWT_AUDIENCE)
                        .and()
                        .id(kid)
                        .signWith(key,alg)
                        .compact();
        return jws;
    }

    @Override
    @Transactional
    public UserRole signup(String email, String password, String name, String phone,String role)throws EmptyEmailException, EmptyPasswordException, EmptyPhoneException, EmailAlreadyExistsException, PhoneAlreadyExistsException, UserNotFoundException, EmptyRoleException {
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
        if(role == null || role.isEmpty()) {
            throw new EmptyRoleException("Role cannot be empty");
        }
        Optional<Role> optionalRole = roleRepository.findByName(role);
        if(optionalRole.isEmpty()){
            throw new RoleNotFoundException("Role not found: " + role);
        }
        Role userRole = optionalRole.get();
        UserRole userRoleEntity = new UserRole();
        userRoleEntity.setUser(updateUserAuditorFields(savedUser.getId()));
        userRoleEntity.setRole(userRole);
        UserRole savedUserRole = userRoleRepository.save(userRoleEntity);
        // Then manually update the audit fields with the new ID
        return savedUserRole;
    }
@Override
public List<UserRole> validateToken(String token)
        throws InvalidTokenException, TokenExpiredException, UserNotFoundException {

    Session session   = loadActiveSession(token);
    Jws<Claims> jws   = parseAndVerifySignature(token);
    Claims   claims   = jws.getPayload();
    User      user    = session.getUser();
    List<Role>      roles    = session.getRole();

    if (user == null || user.getId() == null || user.isDeleted()) {
        throw new UserNotFoundException("User not found");
    }
    if (roles == null || roles.isEmpty() || roles.stream().anyMatch(Role::isDeleted) || roles.stream().anyMatch(role -> role.getId() == null)) {
        throw new RoleNotFoundException("Role not found");
    }
    Optional<List<UserRole>> optionalUserRoleList = userRoleRepository.findUserRoleByUser_Id(user.getId());
    if (optionalUserRoleList.isEmpty()) {
        throw new UserNotFoundException("User does not have a role assigned");
    }
    List<UserRole> userRoleList = optionalUserRoleList.get();
    if (!userRoleList.stream().anyMatch(ur -> ur.getRole().getName().equals(roles.getFirst().getName()))) {
        throw new InvalidTokenException("Token role does not match user role");
    }
    if (!session.getToken().equals(token)) {
        throw new InvalidTokenException("Token does not match session token");
    }

    verifyTimestamps(claims.getExpiration(), session.getExpiredAt());
    verifyClaim(claims.get("email"),    user.getEmail(),   "email");
    verifyAudience(claims.getAudience());
    verifyIssuer(claims.getIssuer());
    verifySubject(claims.getSubject(),  user.getId().toString());
    verifyJwtRecordExists(claims.getId());
    return userRoleList;
}

    private Session loadActiveSession(String token) throws InvalidTokenException, TokenExpiredException {
        return sessionRepository
                .findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE)
                .filter(s -> !s.isDeleted())
                .filter(s -> !s.getExpiredAt().before(new Date()))
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired session"));
    }

    private Jws<Claims> parseAndVerifySignature(String token) throws InvalidTokenException {
        try {
            JwtParser parser = Jwts.parser()
                    .keyLocator(keyLocator)
                    .build();
            return parser.parseSignedClaims(token);
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid token signature");
        }
    }

    private void verifyTimestamps(Date jwtExpiry, Date sessionExpiry) throws TokenExpiredException {
        Date now = new Date();
        if (jwtExpiry.before(now) || sessionExpiry.before(now)) {
            throw new TokenExpiredException("Token expired");
        }
    }

    private void verifyClaim(Object actual, String expected, String name) throws InvalidTokenException {
        if (actual == null || !actual.equals(expected)) {
            throw new InvalidTokenException("Invalid " + name);
        }
    }

    private void verifyAudience(Set<String> audience) throws InvalidTokenException {
        if (!(audience.size() ==1) || !audience.contains(Consts.JWT_AUDIENCE)) {
            throw new InvalidTokenException("Invalid audience");
        }
    }

    private void verifyIssuer(String issuer) throws InvalidTokenException {
        if (!Consts.JWT_ISSUER.equals(issuer)) {
            throw new InvalidTokenException("Invalid issuer");
        }
    }

    private void verifySubject(String subject, String expectedSub) throws InvalidTokenException {
        if (!expectedSub.equals(subject)) {
            throw new InvalidTokenException("Invalid subject");
        }
    }

    private void verifyJwtRecordExists(String jti) throws InvalidTokenException {
        UUID uuid;
        try {
            uuid=UUID.fromString(jti);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid jti");
        }
        if (jwtRepository.findById(uuid).isEmpty()) {
            throw new InvalidTokenException("Unknown jti");
        }
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

            List<UserRole> userRoleListFromValidatedToken = validateToken(token);

            User userFromValidatedtoken = userRoleListFromValidatedToken.getFirst().getUser();
            if(userFromValidatedtoken == null){
                throw new InvalidTokenException("Invalid token");
            }
            if(!userFromValidatedtoken.getId().equals(user.getId()) || !userFromValidatedtoken.getEmail().equals(user.getEmail())){
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
