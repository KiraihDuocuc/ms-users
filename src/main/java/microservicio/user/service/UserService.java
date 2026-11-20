package microservicio.user.service;

import microservicio.user.model.User;
import microservicio.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        // Nunca aceptar roles enviados desde el frontend
        user.setRole("CLIENT");

        // Evitar registrar correos duplicados
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "El correo ya está registrado"
            );
        }

        // Guardar un hash BCrypt, no la contraseña original
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Credenciales inválidas")
                );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return user;
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}
