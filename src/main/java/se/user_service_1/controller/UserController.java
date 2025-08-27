package se.user_service_1.controller;

import se.user_service_1.dto.UserProfileRequest;
import se.user_service_1.dto.UserProfileResponse;
import se.user_service_1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.user_service_1.model.User;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal User currentUser) {
        log.info("getProfile – request for userId={}", currentUser.getId());
        UserProfileResponse profile = userService.getUserProfile(currentUser);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UserProfileRequest request) {
        log.info("updateProfile – request for userId={}", currentUser.getId());
        UserProfileResponse updatedProfile = userService.updateUserProfile(currentUser, request);
        return ResponseEntity.ok(updatedProfile);
    }
}