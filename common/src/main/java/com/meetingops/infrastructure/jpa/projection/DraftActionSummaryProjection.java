package com.meetingops.infrastructure.jpa.projection;

import com.meetingops.domain.enumeration.DraftActionStatus;
import com.meetingops.domain.enumeration.DraftActionType;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight JPA projection for review queue draft actions.
 * Reduces memory overhead when streaming large pending review queues.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface DraftActionSummaryProjection {

    UUID getId();

    UUID getExtractedItemId();

    UUID getMeetingId();

    DraftActionType getActionType();

    DraftActionStatus getStatus();

    String getPromptVersion();

    Integer getVersion();

    Instant getCreatedAt();
}
