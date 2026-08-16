package com.meetingops.domain.enumeration;

/**
 * Enumeration of meeting processing statuses in the pipeline.
 *
 * <p>Tracks the lifecycle of a meeting from scheduling, auto-capture / ingestion through
 * transcription, extraction, grounding, drafting, and human review.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum MeetingStatus {

    /** Meeting is scheduled for future capture. */
    SCHEDULED,

    /** Bot is actively joining the video call. */
    JOINING,

    /** Meeting audio is being recorded in ephemeral buffer. */
    RECORDING,

    /** Speech-to-text transcription and diarization in progress. */
    TRANSCRIBING,

    /** Meeting transcript has been ingested. */
    INGESTED,

    /** Meeting is being processed by the multi-agent AI pipeline. */
    PROCESSING,

    /** Processing completed; draft actions are pending human review. */
    PENDING_REVIEW,

    /** All draft actions have been reviewed and resolved. */
    REVIEWED,

    /** Alias for reviewed status. */
    RESOLVED,

    /** Meeting pipeline and follow-through actions completed. */
    COMPLETED,

    /** Processing failed and requires manual intervention. */
    FAILED,

    /** Alias for processing failure. */
    PROCESSING_FAILED,

    /** Bot capture was aborted due to missing consent. */
    CONSENT_ABORTED
}
