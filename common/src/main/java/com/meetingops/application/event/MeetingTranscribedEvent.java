package com.meetingops.application.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a meeting transcript has been successfully ingested.
 *
 * <p>This event triggers the downstream agent pipeline (extraction → grounding
 * → validation → drafting). Per FR-1.3, it is published to Kafka to decouple
 * ingestion from agent processing.</p>
 *
 * @param meetingId      the meeting identifier
 * @param organizationId the organization identifier
 * @param transcriptRef  reference to the stored transcript
 * @param publishedAt    timestamp of event publication
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record MeetingTranscribedEvent(
        UUID meetingId,
        UUID organizationId,
        String transcriptRef,
        Instant publishedAt
) {
    /**
     * Creates a new MeetingTranscribedEvent.
     *
     * @param meetingId      the meeting identifier
     * @param organizationId the organization identifier
     * @param transcriptRef  reference to the stored transcript
     * @return a new event with current timestamp
     */
    public static MeetingTranscribedEvent of(final UUID meetingId,
                                             final UUID organizationId,
                                             final String transcriptRef) {
        return new MeetingTranscribedEvent(meetingId, organizationId, transcriptRef, Instant.now());
    }
}
