package se.user_service_1.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.user_service_1.dto.ActivityLogResponse;
import se.user_service_1.model.ActivityLog;
import se.user_service_1.model.User;
import se.user_service_1.service.ActivityLogService;


import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/activityLog")
@RequiredArgsConstructor
public class ActivityLogController {
    private static Logger log  = LoggerFactory.getLogger(ActivityLogController.class);
    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLogResponse>> findByUserId(@AuthenticationPrincipal User currentUser) {
        List<ActivityLog> activityLogList = activityLogService.findByUserId(currentUser.getId());
        List<ActivityLogResponse> activityLogResponseList = new ArrayList<>();
        for ( ActivityLog activityLog : activityLogList ) {
            ActivityLogResponse activityLogResponse = ActivityLogResponse.builder()
                    .activityDate(activityLog.getActivityDate())
                    .activityType(activityLog.getActivityType())
                    .id(activityLog.getId())
                    .responseTime(activityLog.getResponseTime())
                    .userId(activityLog.getUser().getId())
                    .build();
            activityLogResponseList.add(activityLogResponse);
        }
        return  ResponseEntity.ok(activityLogResponseList);
    }
}
