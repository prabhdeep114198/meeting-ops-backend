package com.meetingops.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for OAuth2/JWT-based authentication.
 *
 * <p>Configures stateless security with JWT token validation for all
 * API endpoints. Authorization is handled by role-based access control
 * (Participant, Reviewer, Admin) per FR-7.1.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public health check endpoints
                .requestMatchers("/actuator/health").permitAll()
                // API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                // MCP server endpoints (internal, consumed by agent-service only)
                .requestMatchers("/mcp/**").authenticated()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            );

        // TODO: Configure JWT issuer, audience, and authority converter
        // TODO: Add method-level security with @PreAuthorize for role-based access
        // TODO: Configure CORS for web client

        return http.build();
    }
}
