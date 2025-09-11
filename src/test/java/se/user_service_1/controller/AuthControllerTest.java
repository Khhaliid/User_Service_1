package se.user_service_1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.user_service_1.dto.AuthenticationRequest;
import se.user_service_1.dto.AuthenticationResponse;
import se.user_service_1.dto.RegisterRequest;
import se.user_service_1.exception.BadRequestException;
import se.user_service_1.model.User;
import se.user_service_1.repository.UserRepository;
import se.user_service_1.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest validRegisterRequest;
    private AuthenticationRequest validAuthRequest;
    private AuthenticationResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("testuser", "password123");
        validAuthRequest = new AuthenticationRequest("testuser", "password123");
        mockResponse = AuthenticationResponse.builder()
                .token("mock-jwt-token")
                .build();
    }

    @Test
    @DisplayName("Should register new user successfully")
    void register_ValidRequest_ReturnsToken() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // When
        var response = authController.register(validRegisterRequest);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("mock-jwt-token", response.getBody().getToken());

        verify(userRepository, times(2)).findByUsername("testuser"); // Controller calls it twice
        verify(authenticationService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should reject registration when username exists")
    void register_ExistingUsername_ThrowsBadRequest() {
        // Given
        User existingUser = User.builder().username("testuser").build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        // When & Then
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authController.register(validRegisterRequest)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(authenticationService, never()).register(any());
    }

    @Test
    @DisplayName("Should authenticate user successfully")
    void authenticate_ValidCredentials_ReturnsToken() {
        // Given
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(mockResponse);

        // When
        var response = authController.authenticate(validAuthRequest);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("mock-jwt-token", response.getBody().getToken());

        verify(authenticationService).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    @DisplayName("Should handle authentication failure")
    void authenticate_InvalidCredentials_ThrowsBadRequest() {
        // Given
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BadRequestException("Invalid credentials"));

        // When & Then
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authController.authenticate(validAuthRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null register request")
    void register_NullRequest_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> authController.register(null));

        verify(userRepository, never()).findByUsername(anyString());
        verify(authenticationService, never()).register(any());
    }

    @Test
    @DisplayName("Should handle null authentication request")
    void authenticate_NullRequest_ThrowsException() {
        // When & Then
        assertThrows(Exception.class, () -> authController.authenticate(null));

        verify(authenticationService, never()).authenticate(any());
    }
}