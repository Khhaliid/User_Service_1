package se.user_service_1.service;

import se.user_service_1.dto.AuthenticationRequest;
import se.user_service_1.dto.AuthenticationResponse;
import se.user_service_1.dto.RegisterRequest;
import se.user_service_1.exception.BadRequestException;
import se.user_service_1.model.User;
import se.user_service_1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "password123");
        authRequest = new AuthenticationRequest("testuser", "password123");
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded-password")
                .build();
    }

    @Test
    @DisplayName("Should register user successfully and return JWT token")
    void register_ValidRequest_ReturnsAuthResponse() {
        // Given
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // When
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void authenticate_ValidCredentials_ReturnsAuthResponse() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser)).thenReturn("jwt-token");
        // authenticationManager.authenticate() returns void when successful

        // When
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtService).generateToken(mockUser);
    }

    @Test
    @DisplayName("Should throw BadRequestException when authentication fails")
    void authenticate_InvalidCredentials_ThrowsBadRequestException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authenticationService.authenticate(authRequest)
        );

        assertEquals("Invalid username or password", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when user not found after successful auth")
    void authenticate_UserNotFoundAfterAuth_ThrowsBadRequestException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        // authenticationManager.authenticate() succeeds (no mock needed)

        // When & Then
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authenticationService.authenticate(authRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should create user with correct encoded password")
    void register_ValidRequest_CreatesUserWithEncodedPassword() {
        // Given
        when(passwordEncoder.encode("password123")).thenReturn("super-secure-encoded");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // When
        authenticationService.register(registerRequest);

        // Then
        verify(userRepository).save(argThat(user ->
                "testuser".equals(user.getUsername()) &&
                        "super-secure-encoded".equals(user.getPassword())
        ));
    }
}