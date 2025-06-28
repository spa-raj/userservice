package com.vibevault.userservice.services;


import com.vibevault.userservice.dtos.auth.LoginResponseDto;
import com.vibevault.userservice.exceptions.auth.*;
import com.vibevault.userservice.models.*;
import com.vibevault.userservice.repositories.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private JWTRepository jwtRepository;
    @Mock
    private KeyLocatorImpl keyLocator;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;

    private AuthService authService;

    @Spy
    private AuthServiceImpl authServiceSpy;
    private User testUser;
    private Role testRole;
    private UserRole testUserRole;
    private List<UserRole> testUserRoles;
    private Session testSession;
    private JWT testJwt;

    @BeforeEach
    void setUp() {
        AuthServiceImpl serviceImpl = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                sessionRepository,
                jwtRepository,
                keyLocator,
                roleRepository,
                userRoleRepository
        );

        authServiceSpy = spy(serviceImpl);
        authService = authServiceSpy;
        // Set up test data
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhoneNumber("1234567890");
        testUser.setDeleted(false);

        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setName("USER");
        testRole.setDeleted(false);

        testUserRole = new UserRole();
        testUserRole.setId(UUID.randomUUID());
        testUserRole.setUser(testUser);
        testUserRole.setRole(testRole);

        testUserRoles = List.of(testUserRole);

        testSession = new Session();
        testSession.setId(UUID.randomUUID());
        testSession.setUser(testUser);
        testSession.setRole(List.of(testRole));
        testSession.setToken("valid.test.token");
        testSession.setExpiredAt(new Date(System.currentTimeMillis() + 86400000)); // 24 hours from now
        testSession.setStatus(SessionStatus.ACTIVE);
        testSession.setDeleted(false);

        testJwt = new JWT();
        testJwt.setId(UUID.randomUUID());
        testJwt.setUser(testUser);
// Create a properly Base64-encoded string representing a 512-bit key
        String validSecret = Base64.getEncoder().encodeToString(new byte[64]); // 64 bytes = 512 bits
        testJwt.setSecret(validSecret);
        testJwt.setCreatedAt(new Date());
        testJwt.setDeleted(false);
    }

    // LOGIN TESTS

    @Test
    void login_successfulLogin() throws InvalidCredentialsException {
        // Arrange
        when(userRepository.findUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), eq(testUser.getPassword()))).thenReturn(true);
        when(userRoleRepository.findUserRoleByUser_Id(testUser.getId())).thenReturn(Optional.of(testUserRoles));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // Mock JWT generation
        when(jwtRepository.findTopByUser_IdEqualsAndDeletedEquals(testUser.getId(), false)).thenReturn(Optional.of(testJwt));

        // Act
        LoginResponseDto result = authService.login(testUser.getEmail(), "correctPassword");

        // Assert
        assertNotNull(result);
        assertEquals(testSession.getToken(), result.getToken());
        assertEquals(testSession.getId().toString(), result.getSessionId());
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void login_invalidCredentials_userNotFound() {
        // Arrange
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login("nonexistent@example.com", "password");
        });
    }

    @Test
    void login_invalidCredentials_wrongPassword() {
        // Arrange
        when(userRepository.findUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), eq(testUser.getPassword()))).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(testUser.getEmail(), "wrongPassword");
        });
    }

    @Test
    void login_userWithoutRole() {
        // Arrange
        when(userRepository.findUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), eq(testUser.getPassword()))).thenReturn(true);
        when(userRoleRepository.findUserRoleByUser_Id(testUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidUserException.class, () -> {
            authService.login(testUser.getEmail(), "correctPassword");
        });
    }

    // SIGNUP TESTS

    @Test
    void signup_successfulSignup() throws Exception {
        // Arrange
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findUserByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // Act
        UserRole result = authService.signup("new@example.com", "password123", "New User", "9876543210", "USER");

        // Assert
        assertNotNull(result);
        assertEquals(testRole, result.getRole());
        verify(userRepository, times(2)).save(any(User.class));
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void signup_emptyEmail() {
        // Act & Assert
        assertThrows(EmptyEmailException.class, () -> {
            authService.signup("", "password", "Test User", "1234567890", "USER");
        });
    }

    @Test
    void signup_emptyPassword() {
        // Act & Assert
        assertThrows(EmptyPasswordException.class, () -> {
            authService.signup("test@example.com", "", "Test User", "1234567890", "USER");
        });
    }

    @Test
    void signup_emptyPhone() {
        // Act & Assert
        assertThrows(EmptyPhoneException.class, () -> {
            authService.signup("test@example.com", "password", "Test User", "", "USER");
        });
    }

    @Test
    void signup_emptyRole() {
        // Arrange
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findUserByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        assertThrows(EmptyRoleException.class, () -> {
            authService.signup("new@example.com", "password", "Test User", "1234567890", "");
        });
    }

    @Test
    void signup_emailAlreadyExists() {
        // Arrange
        when(userRepository.findUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.signup(testUser.getEmail(), "password", "Test User", "1234567890", "USER");
        });
    }

    @Test
    void signup_phoneAlreadyExists() {
        // Arrange
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findUserByPhoneNumber(testUser.getPhoneNumber())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(PhoneAlreadyExistsException.class, () -> {
            authService.signup("new@example.com", "password", "Test User", testUser.getPhoneNumber(), "USER");
        });
    }

    @Test
    void signup_roleNotFound() {
        // Arrange
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findUserByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        assertThrows(RoleNotFoundException.class, () -> {
            authService.signup("new@example.com", "password", "Test User", "1234567890", "INVALID_ROLE");
        });
    }

    // VALIDATE TOKEN TESTS

    @Test
    void validateToken_validToken() throws Exception {
        // Arrange
        String token = "valid.test.token";

        // Mock session
        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));

        // Mock JWT parsing
        Jws<Claims> mockJws = mock(Jws.class);
        Claims mockClaims = mock(Claims.class);
        when(mockJws.getPayload()).thenReturn(mockClaims);

        // Setup claims
        when(mockClaims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        when(mockClaims.get("email")).thenReturn(testUser.getEmail());
        when(mockClaims.getAudience()).thenReturn(Set.of(Consts.JWT_AUDIENCE));
        when(mockClaims.getIssuer()).thenReturn(Consts.JWT_ISSUER);
        when(mockClaims.getSubject()).thenReturn(testUser.getId().toString());
        when(mockClaims.getId()).thenReturn(testJwt.getId().toString());

        // Mock JWT parser
        doReturn(mockJws).when(authServiceSpy).parseAndVerifySignature(token);

        when(userRoleRepository.findUserRoleByUser_Id(testUser.getId())).thenReturn(Optional.of(testUserRoles));
        when(jwtRepository.findById(testJwt.getId())).thenReturn(Optional.of(testJwt));

        // Act
        List<UserRole> result = authServiceSpy.validateToken(token);

        // Assert
        assertNotNull(result);
        assertEquals(testUserRoles, result);
    }

    @Test
    void validateToken_invalidSession() {
        // Arrange
        String token = "invalid.token";
        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> {
            authService.validateToken(token);
        });
    }

    @Test
    void validateToken_expiredSession() {
        // Arrange
        String token = "expired.token";
        Session expiredSession = new Session();
        expiredSession.setExpiredAt(new Date(System.currentTimeMillis() - 1000)); // Expired

        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.of(expiredSession));

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> {
            authService.validateToken(token);
        });
    }

    @Test
    void validateToken_invalidSignature() throws Exception {
        // Arrange
        String token = "invalid.signature.token";

        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));

        doThrow(new InvalidTokenException("Invalid token signature"))
                .when(authServiceSpy).parseAndVerifySignature(token);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> {
            authServiceSpy.validateToken(token);
        });
    }

    @Test
    void validateToken_expiredToken() throws Exception {
        // Arrange
        String token = "expired.jwt.token";

        // Create a session with matching token
        Session expiredTokenSession = new Session();
        expiredTokenSession.setId(testSession.getId());
        expiredTokenSession.setUser(testSession.getUser());
        expiredTokenSession.setRole(testSession.getRole());
        expiredTokenSession.setToken(token); // Set token to match our test value
        expiredTokenSession.setExpiredAt(testSession.getExpiredAt());
        expiredTokenSession.setStatus(testSession.getStatus());
        expiredTokenSession.setDeleted(testSession.isDeleted());

        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.of(expiredTokenSession));

        when(userRoleRepository.findUserRoleByUser_Id(testUser.getId()))
                .thenReturn(Optional.of(testUserRoles));

        Jws<Claims> mockJws = mock(Jws.class);
        Claims mockClaims = mock(Claims.class);
        when(mockJws.getPayload()).thenReturn(mockClaims);

        // Setup all required claim values to pass validation until we reach the expiration check
        when(mockClaims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() - 1000));  // expired

        doReturn(mockJws).when(authServiceSpy).parseAndVerifySignature(anyString()); // use anyString() instead of specific token


        // Act & Assert
        assertThrows(TokenExpiredException.class, () -> {
            authServiceSpy.validateToken(token);
        });
    }

    @Test
    void validateToken_invalidEmail() throws Exception {
        // Arrange
        String token = "invalid.email.token";

        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));

        when(userRoleRepository.findUserRoleByUser_Id(testUser.getId()))
                .thenReturn(Optional.of(testUserRoles));

        Jws<Claims> mockJws = mock(Jws.class);
        Claims mockClaims = mock(Claims.class);
        when(mockJws.getPayload()).thenReturn(mockClaims);


        doReturn(mockJws).when(authServiceSpy).parseAndVerifySignature(token);


        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> {
            authServiceSpy.validateToken(token);
        });
    }

    // LOGOUT TESTS

    @Test
    void logout_successful() throws Exception {
        // Arrange
        String token = "valid.test.token";

        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));

        doReturn(testUserRoles).when(authServiceSpy).validateToken(token);

        // Act
        authServiceSpy.logout(testUser.getEmail(), token);

        // Assert
        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        Session savedSession = sessionCaptor.getValue();

        assertTrue(savedSession.isDeleted());
        assertEquals(SessionStatus.LOGGED_OUT, savedSession.getStatus());
    }

    @Test
    void logout_invalidToken() {
        // Arrange
        String token = "invalid.token";
        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> {
            authService.logout(testUser.getEmail(), token);
        });
    }

    @Test
    void logout_tokenExpired() {
        // Arrange
        String token = "expired.token";
        Session expiredSession = new Session();
        expiredSession.setUser(testUser);
        expiredSession.setExpiredAt(new Date(System.currentTimeMillis() - 1000)); // Expired

        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.of(expiredSession));

        // Act & Assert
        assertThrows(TokenExpiredException.class, () -> {
            authService.logout(testUser.getEmail(), token);
        });
    }

    @Test
    void logout_emailMismatch() {
        // Arrange
        String token = "valid.test.token";
        String wrongEmail = "wrong@example.com";

        when(sessionRepository.findSessionsByTokenEqualsAndStatusIs(token, SessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.logout(wrongEmail, token);
        });
    }
}
