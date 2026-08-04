package com.meetingops.domain.enumeration;

/**
 * Enumeration of draft action lifecycle statuses.
 *
 * <p>Tracks the state of a draft action from creation through human review
 * and final execution against external systems.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum DraftActionStatus {

    /** The draft action has been generated and is pending review. */
    DRAFTED,

    /** The draft action has been approved by a reviewer. */
    APPROVED,

    /** The draft action has been edited by a reviewer before approval. */
    EDITED,

    /** The draft action has been rejected by a reviewer. */
    REJECTED,

    /** The draft action has been executed against the external system. */
    EXECUTED
}
