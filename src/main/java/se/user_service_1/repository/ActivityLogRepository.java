package se.user_service_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.user_service_1.model.ActivityLog;
import se.user_service_1.model.User;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserId(Long id);
}