package se.user_service_1.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private long orderId;
    private String orderStatus;
    private LocalDateTime completedAt;

}