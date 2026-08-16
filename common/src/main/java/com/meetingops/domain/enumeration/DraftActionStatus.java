package com.meetingops.domain.enumeration;

/**
 * Enumeration of draft action lifecycle statuses.
 *
 * <p>Enforces the Human-in-the-Loop review state machine.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum DraftActionStatus {

    /** Action has been drafted by the agent and awaits human review. */
    DRAFTED,

    /** Human reviewer approved the action as drafted. */
    APPROVED,

    /** Human reviewer edited the action before approval. */
    EDITED,

    /** Human reviewer rejected the draft action. */
    REJECTED,

    /** Approved action has been successfully dispatched to the external system (via MCP). */
    EXECUTED,

    /** Dispatch to external system failed. */
    FAILED
}
