package microservicio.user.controller;

import microservicio.user.dto.LoginResponseDTO;
import microservicio.user.model.User;
import microservicio.user.service.JwtService;
import microservicio.user.service.UserService;
import microservicio.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Temporal; restringiremos CORS después
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(
            UserService userService,
            JwtService jwtService,
            UserRepository userRepository) {

        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable Long id) {
        return ResponseEntity.ok(userService.existsById(id));
    }

    @GetMapping("/{id}/is-client")
    public ResponseEntity<Boolean> isClient(@PathVariable Long id) {
        return ResponseEntity.ok(userService.isUserClient(id));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.registerUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al registrar: correo duplicado o datos inválidos.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        try {
            User user = userService.loginUser(
                    loginRequest.getEmail(),
                    loginRequest.getPassword());

            String token = jwtService.generateToken(user);

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    "Bearer",
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body("Credenciales inválidas");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("No autenticado");
        }

        return userRepository
                .findByEmail(authentication.getName())
                .map(user -> ResponseEntity.ok((Object) user))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado"));
    }

    // Listar todos los usuarios: solo ADMIN
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Consultar usuario por ID: ADMIN o propietario
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@userAccessService.isCurrentUser(#id, authentication)")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
