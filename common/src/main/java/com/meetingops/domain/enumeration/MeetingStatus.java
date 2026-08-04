package com.meetingops.domain.enumeration;

/**
 * Enumeration of meeting processing statuses in the pipeline.
 *
 * <p>Tracks the lifecycle of a meeting from transcript ingestion through
 * extraction, grounding, drafting, and human review.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum MeetingStatus {

    /** Meeting transcript has been ingested. */
    INGESTED,

    /** Meeting is being processed by the agent pipeline. */
    PROCESSING,

    /** Processing completed; draft actions are pending human review. */
    PENDING_REVIEW,

    /** All draft actions have been reviewed and resolved. */
    RESOLVED,

    /** Processing failed and requires manual intervention. */
    PROCESSING_FAILED
}
