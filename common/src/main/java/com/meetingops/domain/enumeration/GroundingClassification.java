package com.meetingops.domain.enumeration;

/**
 * Enumeration of grounding classification results produced by the RAG-based
 * historical grounding agent.
 *
 * <p>Each classification indicates the relationship between a newly extracted
 * item and the organization's prior meeting history.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum GroundingClassification {

    /** The item has no prior equivalent in meeting history. */
    NEW,

    /** The item is a duplicate of an already open, unresolved item. */
    DUPLICATE,

    /** The item relates to a prior item (e.g., a status update or continuation). */
    CONTINUATION,

    /** The item contradicts a previously recorded decision. */
    CONFLICT
}
