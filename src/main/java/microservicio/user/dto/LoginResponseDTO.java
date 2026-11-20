package microservicio.user.dto;

public record LoginResponseDTO(
        String token,
        String tokenType,
        Long id,
        String name,
        String email,
        String role
) {}
