package com.meetingops.domain.model;

import com.meetingops.domain.enumeration.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a user within an organization.
 *
 * <p>Users are scoped to a single organization and assigned a role that
 * determines their access to the review queue, integration management,
 * and meeting submission capabilities.</p>
 *
 * @param id           unique identifier
 * @param organizationId the owning organization's identifier
 * @param email        user's email address
 * @param role         user's role within the organization
 * @param createdAt    timestamp of creation
 * @param isActive     whether the user account is active
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record User(
        UUID id,
        UUID organizationId,
        String email,
        UserRole role,
        Instant createdAt,
        boolean isActive
) {
    /**
     * Creates a new User with auto-generated UUID and current timestamp.
     *
     * @param organizationId the owning organization's identifier
     * @param email          user's email address
     * @param role           user's role within the organization
     * @return a new User instance
     */
    public static User create(final UUID organizationId,
                              final String email,
                              final UserRole role) {
        return new User(
                UUID.randomUUID(),
                organizationId,
                email,
                role,
                Instant.now(),
                true
        );
    }
}
