package com.meetingops.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing an organization (tenant) in the multi-tenant platform.
 *
 * <p>Each organization has isolated meeting data, extracted items, and
 * historical embeddings. Integration settings are stored per-organization
 * and referenced by the drafting agent's tool configuration.</p>
 *
 * @param id           unique identifier
 * @param name         organization display name
 * @param planTier     subscription tier (FREE, PRO, ENTERPRISE)
 * @param createdAt    timestamp of creation
 * @param updatedAt    timestamp of last update
 * @param isActive     whether the organization is active
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record Organization(
        UUID id,
        String name,
        String planTier,
        Instant createdAt,
        Instant updatedAt,
        boolean isActive
) {
    /**
     * Creates a new Organization with auto-generated UUID and current timestamp.
     *
     * @param name     organization display name
     * @param planTier subscription tier
     * @return a new Organization instance
     */
    public static Organization create(final String name, final String planTier) {
        return new Organization(
                UUID.randomUUID(),
                name,
                planTier,
                Instant.now(),
                Instant.now(),
                true
        );
    }
}
