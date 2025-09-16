package se.user_service_1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.user_service_1.model.ActivityLog;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLogResponse {
    private LocalDateTime activityDate;
    private Long id;
    private Long userId;
    private ActivityLog.ActivityType activityType;
    private Long responseTime;

}
