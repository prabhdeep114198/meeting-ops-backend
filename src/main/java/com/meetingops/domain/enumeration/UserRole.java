package com.meetingops.domain.enumeration;

/**
 * Enumeration of user roles within an organization.
 *
 * <p>Defines the access control boundaries for the review queue,
 * integration management, and meeting submission.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum UserRole {

    /** Can upload transcripts and view extracted items for their meetings. */
    PARTICIPANT,

    /** Can approve, edit, or reject drafted actions before execution. */
    REVIEWER,

    /** Can configure integrations and manage team membership and roles. */
    ADMIN
}
