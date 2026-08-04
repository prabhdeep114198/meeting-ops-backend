package com.meetingops.domain.enumeration;

/**
 * Enumeration of audit trail action types.
 *
 * <p>Records the type of human or system action taken on a draft action
 * for compliance and traceability purposes.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum AuditAction {

    /** The draft action was approved for execution. */
    APPROVE,

    /** The draft action was edited and then approved. */
    EDIT_AND_APPROVE,

    /** The draft action was rejected with a reason. */
    REJECT,

    /** The draft action was automatically executed after approval. */
    EXECUTE
}
