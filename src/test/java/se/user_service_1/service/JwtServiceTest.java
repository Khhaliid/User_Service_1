//package se.user_service_1.service;
//
//import io.jsonwebtoken.ExpiredJwtException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class JwtServiceTest {
//
//    private JwtService jwtService;
//
//    // giltig Base64-nyckel (minst 32 bytes efter decoding)
//    private final String secret = "dGhpcy1pc19hX3N1cGVyX3NlY3JldF90ZXN0X2tleV8xMjM0NTY=";
//    private final long expirationMillis = 1000L * 60 * 60; // 1 timme
//
//    @BeforeEach
//    void setUp() {
//        jwtService = new JwtService(secret, expirationMillis);
//    }
//
//    private UserDetails buildTestUser() {
//        return User.withUsername("testuser")
//                .password("password123")
//                .authorities("ROLE_USER")
//                .build();
//    }
//
//    @Test
//    @DisplayName("Should generate a valid token for a user")
//    void generateToken_ReturnsValidToken() {
//        UserDetails userDetails = buildTestUser();
//
//        String token = jwtService.generateToken(userDetails);
//
//        assertNotNull(token);
//        assertEquals("testuser", jwtService.extractUsername(token));
//        assertTrue(jwtService.isTokenValid(token, userDetails));
//    }
//
//    @Test
//    @DisplayName("Should return false when username does not match")
//    void isTokenValid_WrongUsername_ReturnsFalse() {
//        UserDetails userDetails = buildTestUser();
//        String token = jwtService.generateToken(userDetails);
//
//        UserDetails otherUser = User.withUsername("otheruser")
//                .password("irrelevant")
//                .authorities("ROLE_USER")
//                .build();
//
//        boolean result = jwtService.isTokenValid(token, otherUser);
//
//        assertFalse(result);
//    }
//
//    @Test
//    @DisplayName("Should throw ExpiredJwtException for expired token in isTokenValid")
//    void isTokenValid_ExpiredToken_ThrowsException() throws InterruptedException {
//        JwtService shortLivedJwtService = new JwtService(secret, 1); // 1 ms expiration
//
//        UserDetails userDetails = buildTestUser();
//        String token = shortLivedJwtService.generateToken(userDetails);
//
//        Thread.sleep(5);
//
//        assertThrows(ExpiredJwtException.class,
//                () -> shortLivedJwtService.isTokenValid(token, userDetails));
//    }
//
//    @Test
//    @DisplayName("Should throw ExpiredJwtException when extracting username from expired token")
//    void extractUsername_ExpiredToken_ThrowsException() throws InterruptedException {
//        JwtService shortLivedJwtService = new JwtService(secret, 1);
//
//        UserDetails userDetails = buildTestUser();
//        String token = shortLivedJwtService.generateToken(userDetails);
//
//        Thread.sleep(5);
//
//        assertThrows(ExpiredJwtException.class,
//                () -> shortLivedJwtService.extractUsername(token));
//    }
//}
