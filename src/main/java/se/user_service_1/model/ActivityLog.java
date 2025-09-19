package se.user_service_1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime activityDate;

    @PrePersist
    protected void onCreate() {
        if (this.activityDate == null) {
            this.activityDate = LocalDateTime.now();
        }
    }

    @Column(nullable = false)
    private ActivityType activityType;

    @Column(nullable = true)
    private Long responseTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //Lagt till ORDER_CREATED och ORDER_COMPLETED gör att göra det möjligt att logga dessa händelser
    public enum ActivityType {
        LOGIN,
        REGISTER,
        PROFILE_UPDATE,
        ORDER_COMPLETED
    }

}