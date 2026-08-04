package com.meetingops.domain.model;

import com.meetingops.domain.enumeration.GroundingClassification;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing the result of grounding an extracted item
 * against the organization's historical meeting data.
 *
 * <p>Grounding uses RAG (Retrieval-Augmented Generation) to classify each
 * extracted item as new, duplicate, continuation, or conflict with respect
 * to prior meeting history. Cross-tenant retrieval is prohibited.</p>
 *
 * @param id              unique identifier
 * @param extractedItemId the extracted item that was grounded
 * @param classification  grounding classification result
 * @param citedItemId     the prior item cited as reference, or null if NEW
 * @param citedMeetingId  the prior meeting cited as reference, or null if NEW
 * @param rationale       plain-language explanation of the classification
 * @param createdAt       timestamp of creation
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record GroundingResult(
        UUID id,
        UUID extractedItemId,
        GroundingClassification classification,
        UUID citedItemId,
        UUID citedMeetingId,
        String rationale,
        Instant createdAt
) {
    /**
     * Creates a new GroundingResult with auto-generated UUID.
     *
     * @param extractedItemId the extracted item that was grounded
     * @param classification  grounding classification result
     * @param citedItemId     the prior item cited as reference
     * @param citedMeetingId  the prior meeting cited as reference
     * @param rationale       explanation of the classification
     * @return a new GroundingResult
     */
    public static GroundingResult create(final UUID extractedItemId,
                                         final GroundingClassification classification,
                                         final UUID citedItemId,
                                         final UUID citedMeetingId,
                                         final String rationale) {
        return new GroundingResult(
                UUID.randomUUID(),
                extractedItemId,
                classification,
                citedItemId,
                citedMeetingId,
                rationale,
                Instant.now()
        );
    }
}
