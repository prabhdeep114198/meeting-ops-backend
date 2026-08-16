package com.meetingops.infrastructure.web;

import com.meetingops.application.util.TenantContext;
import com.meetingops.application.util.TenantContextHolder;
import com.meetingops.domain.enumeration.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter for downstream Spring Boot microservices.
 *
 * <p>Reads {@code X-Org-ID}, {@code X-User-ID}, and {@code X-User-Role} forwarded
 * by the Spring Cloud API Gateway and populates {@link TenantContextHolder}
 * for the duration of the request.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String orgIdStr = request.getHeader("X-Org-ID");
            String userIdStr = request.getHeader("X-User-ID");
            String userRoleStr = request.getHeader("X-User-Role");
            String email = request.getHeader("X-User-Email");

            UUID orgId = parseUuid(orgIdStr);
            UUID userId = parseUuid(userIdStr);
            UserRole userRole = parseRole(userRoleStr);

            if (orgId != null) {
                TenantContext context = TenantContext.builder()
                        .organizationId(orgId)
                        .userId(userId)
                        .userRole(userRole)
                        .email(email)
                        .build();

                TenantContextHolder.setContext(context);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private UUID parseUuid(String uuidStr) {
        if (uuidStr == null || uuidStr.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(uuidStr.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private UserRole parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return UserRole.PARTICIPANT;
        }
        try {
            return UserRole.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UserRole.PARTICIPANT;
        }
    }
}
