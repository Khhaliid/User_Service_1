package se.user_service_1.service;

import se.user_service_1.dto.UserProfileRequest;
import se.user_service_1.dto.UserProfileResponse;
import se.user_service_1.model.ActivityLog;
import se.user_service_1.model.User;
import se.user_service_1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Value("${master.key}")
    private String masterKey;

    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername – attempt for username={}", username);
        UserDetails user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("loadUserByUsername – user not found username={}", username);
                    return new UsernameNotFoundException("User not found");
                });
        log.debug("loadUserByUsername – found user username={}", username);
        return user;
    }

    public UserProfileResponse getUserProfile(User user) {
        log.info("getUserProfile – for userId={}", user.getId());
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .preferredLanguage(user.getPreferredLanguage())
                .emailNotifications(user.isEmailNotifications())
                .smsNotifications(user.isSmsNotifications())
                .timeZone(user.getTimeZone())
                .build();
    }

    public UserProfileResponse updateUserProfile(User currentUser, UserProfileRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("updateUserProfile – for userId={}", currentUser.getId());

        // Uppdatera fälten
        currentUser.setFirstName(request.getFirstName());
        currentUser.setLastName(request.getLastName());
        currentUser.setEmail(request.getEmail());
        currentUser.setPhoneNumber(request.getPhoneNumber());
        currentUser.setPreferredLanguage(request.getPreferredLanguage());
        currentUser.setEmailNotifications(request.isEmailNotifications());
        currentUser.setSmsNotifications(request.isSmsNotifications());
        currentUser.setTimeZone(request.getTimeZone());

        // Spara användaren
        User savedUser = userRepository.save(currentUser);

        // Logga aktivitet
        long responseTime = System.currentTimeMillis() - startTime;
        activityLogService.logActivity(savedUser, ActivityLog.ActivityType.PROFILE_UPDATE, responseTime);

        return getUserProfile(savedUser);
    }
}