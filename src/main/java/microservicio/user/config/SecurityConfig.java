package microservicio.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${INTERNAL_API_KEY}") String internalApiKey) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Registro y login públicos
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/register",
                                "/api/users/login")
                        .permitAll()

                        // Temporal: comunicación interna con Pets
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/*/exists",
                                "/api/users/*/is-client")
                        .permitAll()

                        // Perfil del usuario autenticado
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/me")
                        .authenticated()

                        // Listado completo: solo administradores
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users")
                        .hasRole("ADMIN")

                        // Consulta individual: la autorización
                        // de propietario se comprobará en el controlador
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/*")
                        .authenticated()

                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated())

                .exceptionHandling(ex -> ex

                        // Usuario sin autenticación válida
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"message\":\"No autenticado\"}");
                        })

                        // Usuario autenticado, pero sin permisos
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"message\":\"Acceso denegado\"}");
                        }))

                // Registrar el filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .addFilterBefore(
                        new InternalApiKeyFilter(internalApiKey),
                        JwtAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
