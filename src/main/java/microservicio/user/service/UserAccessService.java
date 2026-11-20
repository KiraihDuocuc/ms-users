package microservicio.user.service;

import microservicio.user.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("userAccessService")
public class UserAccessService {

    private final UserRepository userRepository;

    public UserAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isCurrentUser(
            Long userId,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            return false;
        }

        return userRepository.findByEmail(
                authentication.getName()
        ).map(user -> user.getId().equals(userId))
         .orElse(false);
    }
}

