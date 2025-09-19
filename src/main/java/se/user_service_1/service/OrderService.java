package se.user_service_1.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateRequestCustomizer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.user_service_1.dto.OrderHistoryRequest;
import se.user_service_1.dto.OrderResponse;
import se.user_service_1.model.ActivityLog;
import se.user_service_1.model.User;
import se.user_service_1.repository.ActivityLogRepository;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalTime.MIN;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);
    private final RestTemplate restTemplate;
    private final ActivityLogService activityLogService;

    //TODO skapa activitetslogs med hjälp av redan satta DateTimes från orderhistoriken
    //TODO hämta alla ordrar efter den senaste loggen om att ordrar skapats (För att minska på mängden data som behöver skickas och gå igenom)
    public void syncOrderHistory (String token, User user) {

        // Hämta senaste loggdatum
        ActivityLog lastOrderLog = activityLogService.findLastOrderCompletedLog(user.getId());
        LocalDateTime lastLogDate = lastOrderLog != null ? lastOrderLog.getActivityDate() : LocalDateTime.MIN;

        // Hämta alla ordrar från order-service
        String orderServiceAddress = "http://localhost:8081";
//        String url = orderServiceAddress + "/order/orderHistory/" + user.getId();
        String url = orderServiceAddress + "/order/orderHistory";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.set("Content-Type", "application/json");

        HttpEntity<OrderHistoryRequest> entity = new HttpEntity<>(new OrderHistoryRequest(), headers);

        ResponseEntity<List<OrderResponse>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        List<OrderResponse> orders = responseEntity.getBody();
        if (orders == null || orders.isEmpty()) {
            log.info("Inga ordrar hittades för user {}", user.getId());
            return;
        }

        // Filtrera och skapa logs
        orders.stream()
                .filter(order -> order.getCompletedAt() != null && order.getCompletedAt().isAfter(lastLogDate))
                .forEach(order -> activityLogService.logActivityWithDate(
                        user,
                        ActivityLog.ActivityType.ORDER_COMPLETED,
                        order.getCompletedAt()
                ));

    }
}
