package com.meetingops.domain.enumeration;

/**
 * Enumeration of draft action types that the drafting agent can produce.
 *
 * <p>Each type corresponds to a specific MCP tool used for generating
 * follow-through actions from validated meeting items.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum DraftActionType {

    /** A task-tracker entry to be created via the create-task MCP tool. */
    TASK,

    /** A calendar reminder to be set via the calendar-reminder MCP tool. */
    CALENDAR_REMINDER,

    /** A follow-up email to be drafted via the draft-email MCP tool. */
    EMAIL
}
