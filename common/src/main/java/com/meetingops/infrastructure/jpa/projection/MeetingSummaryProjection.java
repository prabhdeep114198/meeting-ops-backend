package com.meetingops.infrastructure.jpa.projection;

import com.meetingops.domain.enumeration.MeetingStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight JPA projection for memory-efficient meeting queries.
 * Selects only essential header fields, bypassing heavy JSON payloads.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface MeetingSummaryProjection {

    UUID getId();

    UUID getOrganizationId();

    UUID getTeamId();

    String getTitle();

    Instant getMeetingDate();

    MeetingStatus getStatus();

    Instant getCreatedAt();
}
