package microservicio.user.service;

import microservicio.user.model.User;
import microservicio.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public boolean isUserClient(Long id) {
        return userRepository.findById(id)
                .map(user -> "CLIENT".equalsIgnoreCase(user.getRole()))
                .orElse(false);
    }

    public User registerUser(User user) {
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("CLIENT");
        }
        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
    }
}