package se.user_service_1.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.user_service_1.model.User;
import se.user_service_1.repository.UserRepository;
import se.user_service_1.service.OrderService;
import se.user_service_1.service.UserService;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final OrderService orderService;


    @PostMapping("/sync")
    public String syncOrders(@RequestHeader("Authorization") String token, @AuthenticationPrincipal User user) {
//        User user = userRepository.findById(userId).orElseThrow(
//                () -> new RuntimeException("Användaren med id " + userId + " finns inte."));

        orderService.syncOrderHistory(token, user);
        return "Synkronisering klar för user " + user.getId();
    }
}
