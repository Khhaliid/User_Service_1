package se.user_service_1.service;

import se.user_service_1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Value("${master.key}")
    private String masterKey;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername – attempt for username={}", username);
        UserDetails user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("loadUserByUsername – user not found username={}", username);
                    return new UsernameNotFoundException("User not found");
                });
        log.debug("loadUserByUsername – found user username={}", username);
        return user;
    }

}
