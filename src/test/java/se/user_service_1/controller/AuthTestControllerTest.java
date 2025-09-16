package se.user_service_1.controller;

import se.user_service_1.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthTestControllerTest {

    @InjectMocks
    private AuthTestController authTestController;

    @Test
    @DisplayName("Should return authenticated user details when user is provided")
    void testAccess_AuthenticatedUser_ReturnsUserDetails() {
        // Given
        User mockUser = User.builder()
                .username("testuser")
                .password("encoded-password")
                .build();

        // When
        ResponseEntity<String> response = authTestController.testAccess(mockUser);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("✅ Authenticated as: testuser", response.getBody());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user is null")
    void testAccess_NullUser_ThrowsUnauthorized() {
        // When & Then
        assertThrows(
                se.user_service_1.exception.UnauthorizedException.class,
                () -> authTestController.testAccess(null)
        );
    }

    @Test
    @DisplayName("Should handle user with different username")
    void testAccess_DifferentUsername_ReturnsCorrectMessage() {
        // Given
        User mockUser = User.builder()
                .username("admin")
                .password("password")
                .build();

        // When
        ResponseEntity<String> response = authTestController.testAccess(mockUser);

        // Then
        assertEquals("✅ Authenticated as: admin", response.getBody());
    }

    @Test
    @DisplayName("Should handle user with special characters in username")
    void testAccess_SpecialCharactersUsername_ReturnsCorrectMessage() {
        // Given
        User mockUser = User.builder()
                .username("user@example.com")
                .password("password")
                .build();

        // When
        ResponseEntity<String> response = authTestController.testAccess(mockUser);

        // Then
        assertEquals("✅ Authenticated as: user@example.com", response.getBody());
    }
}