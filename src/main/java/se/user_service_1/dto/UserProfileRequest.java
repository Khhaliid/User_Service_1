package se.user_service_1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String preferredLanguage;
    private boolean emailNotifications;
    private boolean smsNotifications;
    private String timeZone;
}