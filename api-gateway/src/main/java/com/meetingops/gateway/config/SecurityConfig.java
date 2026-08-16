package com.meetingops.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive WebFlux Security configuration for the API Gateway.
 *
 * <p>Allows unhindered routing while delegating authentication inspection
 * to the {@code JwtClaimExtractionFilter} and downstream microservices.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable) // Managed via CorsWebFilter
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/api/v1/**").permitAll()
                        .pathMatchers("/mcp/**").permitAll()
                        .anyExchange().permitAll()
                );

        return http.build();
    }
}
