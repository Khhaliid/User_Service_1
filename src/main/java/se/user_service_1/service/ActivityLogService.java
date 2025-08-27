package se.user_service_1.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.user_service_1.model.ActivityLog;
import se.user_service_1.model.User;
import se.user_service_1.repository.ActivityLogRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);

    public void logActivity(User user, ActivityLog.ActivityType activityType, long responseTime) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .activityType(activityType)
                .responseTime(responseTime)
                .build();
        activityLogRepository.save(activityLog);
    }

    public List<ActivityLog> findByUserId(Long userId) {
        return activityLogRepository.findByUserId(userId);
    }
}
