package com.meetingops.domain.enumeration;

/**
 * Enumeration of statuses for extracted items throughout the processing pipeline.
 *
 * <p>Tracks the lifecycle of an extracted item from initial extraction through
 * grounding, validation, and final resolution.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum ItemStatus {

    /** Item has been extracted and is awaiting grounding. */
    EXTRACTED,

    /** Item has been grounded and requires human clarification. */
    NEEDS_CLARIFICATION,

    /** Item has been validated and is ready for draft action generation. */
    VALIDATED,

    /** Item has been flagged as a duplicate of a prior item. */
    DUPLICATE,

    /** Item has been flagged as conflicting with a prior decision. */
    CONFLICT,

    /** Item has been flagged as invalid and unactionable. */
    INVALID
}
