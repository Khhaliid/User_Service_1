package se.user_service_1.controller;

import se.user_service_1.dto.AuthenticationRequest;
import se.user_service_1.dto.AuthenticationResponse;
import se.user_service_1.dto.RegisterRequest;
import se.user_service_1.exception.BadRequestException;
import se.user_service_1.model.User;
import se.user_service_1.repository.UserRepository;
import se.user_service_1.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController handles user registration and login.
 * Supports two authentication flows:
 * - API_KEY: creates a new user and returns a generated API key.
 * - PASSWORD: delegates to AuthenticationService to register/login with JWT.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user based on the authType in the request.
     * If authType = "API_KEY", create user, generate and return API key.
     * Otherwise, delegate to AuthenticationService for JWT registration.
     *
     * @param request contains username, password, and authType (API_KEY or PASSWORD)
     * @return AuthenticationResponse with either apiKey or JWT token
     * @throws BadRequestException if username already exists
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        log.info("register – authType={} username={}", request.getAuthType(), request.getUsername());

        // Check if username is already taken
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }
        else {
            // Otherwise, use password-based flow via AuthenticationService (returns JWT)
            log.info("register – delegate to AuthenticationService for username={}", request.getUsername());
            AuthenticationResponse response = authenticationService.register(request);
            log.debug("register – returning register response for username={}", request.getUsername());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Authenticates a user based on the provided credentials.
     * Delegates to AuthenticationService, which handles both API_KEY and PASSWORD flows.
     *
     * @param request contains username, password, and optional apiKey
     * @return AuthenticationResponse with JWT token or API key confirmation
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("authenticate – login attempt username={}", request.getUsername());
        AuthenticationResponse response = authenticationService.authenticate(request);
        log.info("authenticate – login successful username={}", request.getUsername());
        return ResponseEntity.ok(response);
    }
}
