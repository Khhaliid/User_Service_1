package se.user_service_1.service;

import se.user_service_1.model.User;
import se.user_service_1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded-password")
                .build();

        // Set the master key for testing
        ReflectionTestUtils.setField(userService, "masterKey", "test-master-key");
    }

    @Test
    @DisplayName("Should load user by username successfully")
    void loadUserByUsername_ExistingUser_ReturnsUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // When
        UserDetails result = userService.loadUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        assertTrue(result.isEnabled());

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void loadUserByUsername_NonExistentUser_ThrowsUsernameNotFoundException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should handle null username gracefully")
    void loadUserByUsername_NullUsername_ThrowsUsernameNotFoundException() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(null)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername(null);
    }

    @Test
    @DisplayName("Should handle empty username")
    void loadUserByUsername_EmptyUsername_ThrowsUsernameNotFoundException() {
        // Given
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("");
    }

    @Test
    @DisplayName("Should handle username with special characters")
    void loadUserByUsername_SpecialCharacters_ReturnsUserDetails() {
        // Given
        String specialUsername = "user@example.com";
        User specialUser = User.builder()
                .username(specialUsername)
                .password("password")
                .build();
        when(userRepository.findByUsername(specialUsername)).thenReturn(Optional.of(specialUser));

        // When
        UserDetails result = userService.loadUserByUsername(specialUsername);

        // Then
        assertNotNull(result);
        assertEquals(specialUsername, result.getUsername());
        verify(userRepository).findByUsername(specialUsername);
    }

    @Test
    @DisplayName("Should return user with empty authorities collection")
    void loadUserByUsername_ValidUser_ReturnsEmptyAuthorities() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // When
        UserDetails result = userService.loadUserByUsername("testuser");

        // Then
        assertNotNull(result.getAuthorities());
        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Should verify all UserDetails boolean methods return true")
    void loadUserByUsername_ValidUser_AllBooleanMethodsReturnTrue() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // When
        UserDetails result = userService.loadUserByUsername("testuser");

        // Then
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        assertTrue(result.isEnabled());
    }

    @Test
    @DisplayName("Should handle case sensitive usernames")
    void loadUserByUsername_CaseSensitive_HandledCorrectly() {
        // Given
        when(userRepository.findByUsername("TestUser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // When & Then - Different case should not match
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("TestUser"));

        // But exact case should match
        UserDetails result = userService.loadUserByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
}