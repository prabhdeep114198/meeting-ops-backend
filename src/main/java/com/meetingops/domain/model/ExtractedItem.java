package com.meetingops.domain.model;

import com.meetingops.domain.enumeration.ItemStatus;
import com.meetingops.domain.enumeration.ItemType;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing an item extracted from a meeting transcript.
 *
 * <p>An ExtractedItem is produced by the extraction agent and contains a
 * description, optional owner and deadline (if evidenced in the transcript),
 * the supporting transcript excerpt, and a confidence score. Items with
 * ambiguous fields are flagged {@code NEEDS_CLARIFICATION} per FR-2.3.</p>
 *
 * @param id                  unique identifier
 * @param meetingId           the meeting from which this item was extracted
 * @param type                type of item (ACTION_ITEM or DECISION)
 * @param description         extracted description of the item
 * @param owner               identified owner, or null if ambiguous
 * @param deadline            identified deadline, or null if ambiguous
 * @param supportingExcerpt   the transcript excerpt supporting this extraction
 * @param confidence          confidence score (0.0 to 1.0) reflecting extraction certainty
 * @param status              current processing status
 * @param promptVersion       version of the prompt/model used for extraction
 * @param createdAt           timestamp of creation
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record ExtractedItem(
        UUID id,
        UUID meetingId,
        ItemType type,
        String description,
        String owner,
        String deadline,
        String supportingExcerpt,
        Double confidence,
        ItemStatus status,
        String promptVersion,
        Instant createdAt
) {
    /**
     * Creates a new ExtractedItem with auto-generated UUID.
     *
     * @param meetingId         the meeting from which this item was extracted
     * @param type              type of item (ACTION_ITEM or DECISION)
     * @param description       extracted description
     * @param owner             identified owner, or null if ambiguous
     * @param deadline          identified deadline, or null if ambiguous
     * @param supportingExcerpt the supporting transcript excerpt
     * @param confidence        confidence score
     * @param promptVersion     prompt/model version used
     * @return a new ExtractedItem with EXTRACTED status
     */
    public static ExtractedItem create(final UUID meetingId,
                                       final ItemType type,
                                       final String description,
                                       final String owner,
                                       final String deadline,
                                       final String supportingExcerpt,
                                       final Double confidence,
                                       final String promptVersion) {
        return new ExtractedItem(
                UUID.randomUUID(),
                meetingId,
                type,
                description,
                owner,
                deadline,
                supportingExcerpt,
                confidence,
                ItemStatus.EXTRACTED,
                promptVersion,
                Instant.now()
        );
    }

    /**
     * Returns a copy of this item with updated status.
     *
     * @param newStatus the new item status
     * @return a new ExtractedItem with updated status
     */
    public ExtractedItem withStatus(final ItemStatus newStatus) {
        return new ExtractedItem(
                this.id, this.meetingId, this.type, this.description,
                this.owner, this.deadline, this.supportingExcerpt,
                this.confidence, newStatus, this.promptVersion, this.createdAt
        );
    }
}
