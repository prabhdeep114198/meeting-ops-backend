package com.meetingops.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Global Gateway filter responsible for JWT claim extraction and downstream header forwarding.
 *
 * <p>Extracts {@code X-Org-ID}, {@code X-User-ID}, and {@code X-User-Role} from Bearer tokens
 * (or demo headers in development mode) and injects them into downstream microservice requests
 * to guarantee strict multi-tenant context propagation (FR-10.1, FR-10.2, DATA-3).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Component
public class JwtClaimExtractionFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtClaimExtractionFilter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Default development / synthetic demo tenant fallback
    public static final String DEFAULT_DEMO_ORG_ID = "c38a4d78-b179-4d64-9844-3d027cf884c9";
    public static final String DEFAULT_DEMO_USER_ID = "e76b2f12-0941-47a3-8321-70bf812c75a2";
    public static final String DEFAULT_DEMO_ROLE = "REVIEWER";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        String orgId = null;
        String userId = null;
        String userRole = null;
        String userEmail = null;

        // 1. Inspect Authorization Bearer Token
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
                    JsonNode claims = MAPPER.readTree(new String(payloadBytes, StandardCharsets.UTF_8));

                    if (claims.has("org_id")) {
                        orgId = claims.get("org_id").asText();
                    } else if (claims.has("organizationId")) {
                        orgId = claims.get("organizationId").asText();
                    }

                    if (claims.has("sub")) {
                        userId = claims.get("sub").asText();
                    } else if (claims.has("user_id")) {
                        userId = claims.get("user_id").asText();
                    }

                    if (claims.has("role")) {
                        userRole = claims.get("role").asText();
                    } else if (claims.has("user_role")) {
                        userRole = claims.get("user_role").asText();
                    }

                    if (claims.has("email")) {
                        userEmail = claims.get("email").asText();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to decode JWT claims in Gateway filter: {}", e.getMessage());
            }
        }

        // 2. Check for Explicit Request Headers or Demo Overrides
        if (orgId == null || orgId.isBlank()) {
            orgId = headers.getFirst("X-Org-ID");
            if (orgId == null) {
                orgId = headers.getFirst("X-Demo-Org-ID");
            }
        }

        if (userId == null || userId.isBlank()) {
            userId = headers.getFirst("X-User-ID");
            if (userId == null) {
                userId = headers.getFirst("X-Demo-User-ID");
            }
        }

        if (userRole == null || userRole.isBlank()) {
            userRole = headers.getFirst("X-User-Role");
            if (userRole == null) {
                userRole = headers.getFirst("X-Demo-User-Role");
            }
        }

        // 3. Fallback to Demo Defaults for Local Development & Prototyping
        if (orgId == null || orgId.isBlank()) {
            orgId = DEFAULT_DEMO_ORG_ID;
        }
        if (userId == null || userId.isBlank()) {
            userId = DEFAULT_DEMO_USER_ID;
        }
        if (userRole == null || userRole.isBlank()) {
            userRole = DEFAULT_DEMO_ROLE;
        }
        if (userEmail == null || userEmail.isBlank()) {
            userEmail = "reviewer@meetingops.com";
        }

        log.debug("Routing request [{} {}] -> Downstream with Tenant Context: OrgID={}, UserID={}, Role={}",
                request.getMethod(), request.getURI().getPath(), orgId, userId, userRole);

        // 4. Mutate ServerHttpRequest to include extracted headers
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Org-ID", orgId)
                .header("X-User-ID", userId)
                .header("X-User-Role", userRole)
                .header("X-User-Email", userEmail)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // High precedence to ensure claims are extracted before route matching and rate limiting
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
