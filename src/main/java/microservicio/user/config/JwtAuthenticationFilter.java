package microservicio.user.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import microservicio.user.model.User;
import microservicio.user.repository.UserRepository;
import microservicio.user.service.JwtService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null &&
                authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            try {
                Claims claims = jwtService.validateAndGetClaims(token);
                String email = claims.getSubject();

                if (email != null &&
                        SecurityContextHolder.getContext()
                                .getAuthentication() == null) {

                    User user = userRepository.findByEmail(email)
                            .orElse(null);

                    Object userIdClaim = claims.get("userId");

                    if (user != null &&
                            userIdClaim instanceof Number &&
                            user.getId().longValue() == ((Number) userIdClaim).longValue()) {

                        var authorities = List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().toUpperCase()));

                        var authentication = new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                authorities);

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                }

            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
