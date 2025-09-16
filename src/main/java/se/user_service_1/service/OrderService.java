package se.user_service_1.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.user_service_1.dto.OrderResponse;
import se.user_service_1.model.User;
import se.user_service_1.repository.ActivityLogRepository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);
    private final RestTemplate restTemplate;
    private final ActivityLogService activityLogService;

    //TODO skapa activitetslogs med hjälp av redan satta DateTimes från orderhistoriken
    //TODO hämta alla ordrar efter den senaste loggen om att ordrar skapats (För att minska på mängden data som behöver skickas och gå igenom)
    public List<OrderResponse> getOrderHistory(User user){
        String orderServiceAddress = "http://localhost:8081";
        String url = orderServiceAddress + "/order/getOrderHistory/" + user.getId();
        ResponseEntity<List<OrderResponse>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });

        return responseEntity.getBody();
    }
}
