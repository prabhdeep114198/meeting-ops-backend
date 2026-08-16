package com.meetingops.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Centralized Redis Token Bucket Rate Limiter KeyResolver.
 * Resolves rate limit bucket by tenant Organization ID (X-Org-ID), user ID, or client IP.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Configuration
public class RateLimiterConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterConfig.class);

    @Bean(name = "tenantKeyResolver")
    @Primary
    public KeyResolver tenantKeyResolver() {
        return exchange -> {
            // 1. Check if X-Org-ID header is present (populated by JwtClaimExtractionFilter)
            String orgId = exchange.getRequest().getHeaders().getFirst("X-Org-ID");
            if (orgId != null && !orgId.isBlank()) {
                return Mono.just("org:" + orgId);
            }

            // 2. Check if X-User-ID header is present
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }

            // 3. Fallback to client remote address / IP
            String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                    .map(addr -> addr.getAddress().getHostAddress())
                    .orElse("anonymous");

            log.trace("Resolving rate limit key by IP fallback: {}", clientIp);
            return Mono.just("ip:" + clientIp);
        };
    }
}
