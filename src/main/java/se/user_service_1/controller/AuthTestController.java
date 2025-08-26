package se.user_service_1.controller;

import se.user_service_1.exception.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.user_service_1.model.User;

@RestController
@RequestMapping("/test-auth")
public class AuthTestController {

    @GetMapping
    public ResponseEntity<String> testAccess(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return ResponseEntity.ok("✅ Authenticated as: " + user.getUsername());
    }
}
