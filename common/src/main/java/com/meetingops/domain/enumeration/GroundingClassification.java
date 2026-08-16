package com.meetingops.domain.enumeration;

/**
 * Enumeration of grounding classification results produced by the RAG-based
 * historical grounding agent.
 *
 * <p>Each classification indicates the relationship between a newly extracted
 * item and the organization's prior meeting history (pgvector RAG).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum GroundingClassification {

    /** The item has no prior equivalent in meeting history. */
    NEW,

    /** Semantic match (>0.88 similarity) with consistent historical context. */
    NO_CONFLICT,

    /** The item is a duplicate of an already open, unresolved item. */
    DUPLICATE,

    /** The item relates to a prior item (e.g., a status update or continuation). */
    CONTINUATION,

    /** Matches recurring meeting cadence (e.g. weekly sprint notices). */
    RECURRING_UPDATED,

    /** The item contradicts a previously recorded decision. */
    CONFLICT,

    /** Detects conflicting decision or schedule override compared to historical baseline. */
    CONFLICT_DETECTED,

    /** Low confidence score (<0.85) or owner/deadline ambiguity requiring human clarification. */
    NEEDS_CLARIFICATION
}
