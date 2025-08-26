package se.user_service_1.security;

import se.user_service_1.model.User;
import se.user_service_1.service.JwtService;
import se.user_service_1.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;
    private String validToken;

    @BeforeEach
    void setUp() {
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .build();

        validToken = "valid.jwt.token";

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should skip authentication for /auth/register path")
    void doFilterInternal_RegisterPath_SkipsAuthentication() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/auth/register");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        verify(userService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Should skip authentication for /auth/login path")
    void doFilterInternal_LoginPath_SkipsAuthentication() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/auth/login");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        verify(userService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Should continue filter chain when Authorization header is missing")
    void doFilterInternal_MissingAuthHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("Should continue filter chain when Authorization header doesn't start with Bearer")
    void doFilterInternal_InvalidAuthHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Basic invalid");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("Should authenticate user when valid JWT is provided")
    void doFilterInternal_ValidJWT_AuthenticatesUser() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUsername(validToken);
        verify(userService).loadUserByUsername("testuser");
        verify(jwtService).isTokenValid(validToken, userDetails);
        verify(securityContext).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when JWT is invalid")
    void doFilterInternal_InvalidJWT_DoesNotAuthenticate() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).isTokenValid(validToken, userDetails);
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when user is already authenticated")
    void doFilterInternal_AlreadyAuthenticated_SkipsAuthentication() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(userService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).isTokenValid(anyString(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle null username from JWT")
    void doFilterInternal_NullUsername_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(userService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
}