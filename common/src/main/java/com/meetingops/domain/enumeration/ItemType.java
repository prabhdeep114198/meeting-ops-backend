package com.meetingops.domain.enumeration;

/**
 * Enumeration of extracted item types from meeting transcripts.
 *
 * <p>Distinguishes between actionable tasks (Action Items) and
 * recorded conclusions (Decisions) produced by the extraction agent.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum ItemType {

    /** A discrete task identified in a meeting, ideally with an owner and deadline. */
    ACTION_ITEM,

    /** A conclusion or resolution reached during a meeting, distinct from an open task. */
    DECISION
}
