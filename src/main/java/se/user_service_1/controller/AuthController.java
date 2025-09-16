package se.user_service_1.controller;

import io.swagger.v3.oas.annotations.Operation;
import se.user_service_1.dto.AuthenticationRequest;
import se.user_service_1.dto.AuthenticationResponse;
import se.user_service_1.dto.RegisterRequest;
import se.user_service_1.exception.BadRequestException;
import se.user_service_1.model.ActivityLog;
import se.user_service_1.model.User;
import se.user_service_1.repository.UserRepository;
import se.user_service_1.service.ActivityLogService;
import se.user_service_1.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * AuthController handles user registration and login.
 * PASSWORD: delegates to AuthenticationService to register/login with JWT.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    /**
     * Registers a new user in the request.
     *
     * @param request contains username, password
     * @return AuthenticationResponse with JWT token
     * @throws BadRequestException if username already exists
     */
    @Operation(summary = "Register new user", description = "Register new user")
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        long startTime = System.currentTimeMillis();

        // Check if username is already taken
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }
        else {
            // Otherwise, use password-based flow via AuthenticationService (returns JWT)
            log.info("register – delegate to AuthenticationService for username={}", request.getUsername());
            AuthenticationResponse response = authenticationService.register(request);
            log.debug("register – returning register response for username={}", request.getUsername());

            Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                long responseTime = System.currentTimeMillis() - startTime;
                activityLogService.logActivity(user, ActivityLog.ActivityType.REGISTER, responseTime);
            }
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Authenticates a user based on the provided credentials.
     * Delegates to AuthenticationService, which handles PASSWORD flows.
     *
     * @param request contains username, password
     * @return AuthenticationResponse with JWT token confirmation
     */
    @Operation(summary = "Login", description = "Login with username and password")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        // Get responseTime for activityLog
        long startTime = System.currentTimeMillis();

        log.info("authenticate – login attempt username={}", request.getUsername());
        AuthenticationResponse response = authenticationService.authenticate(request);
        log.info("authenticate – login successful username={}", request.getUsername());

        // Log activity for the activityLog
        Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            long responseTime = System.currentTimeMillis() - startTime;
            activityLogService.logActivity(user, ActivityLog.ActivityType.LOGIN, responseTime);
        }

        return ResponseEntity.ok(response);
    }
}
