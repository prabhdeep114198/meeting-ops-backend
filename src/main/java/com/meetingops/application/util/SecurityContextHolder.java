package com.meetingops.application.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Utility for extracting the current authenticated user's context.
 *
 * <p>Extracts organization ID and user ID from the JWT claims
 * for use in application service methods that require tenant
 * isolation (DATA-3) and user identification.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public final class SecurityContextHolder {

    private SecurityContextHolder() {
    }

    /**
     * Extracts the current user's ID from the JWT token.
     *
     * @return the user's UUID
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return UUID.fromString(jwt.getClaimAsString("sub"));
    }

    /**
     * Extracts the current user's organization ID from the JWT token.
     *
     * @return the organization's UUID
     */
    public static UUID getCurrentOrganizationId() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return UUID.fromString(jwt.getClaimAsString("org_id"));
    }

    /**
     * Extracts the current user's role from the JWT token.
     *
     * @return the user's role string
     */
    public static String getCurrentUserRole() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaimAsString("role");
    }
}
