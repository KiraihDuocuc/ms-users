
package microservicio.user.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final byte[] expectedKey;

    public InternalApiKeyFilter(String internalApiKey) {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new IllegalArgumentException(
                "INTERNAL_API_KEY no puede estar vacía"
            );
        }

        this.expectedKey = internalApiKey.getBytes(
            StandardCharsets.UTF_8
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return !(
            "GET".equals(request.getMethod()) &&
            path.matches("/api/users/[^/]+/(exists|is-client)")
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String receivedKey =
                request.getHeader("X-Internal-Api-Key");

        boolean valid = receivedKey != null &&
                MessageDigest.isEqual(
                    expectedKey,
                    receivedKey.getBytes(StandardCharsets.UTF_8)
                );

        if (!valid) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"message\":\"Clave interna inválida o ausente\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
