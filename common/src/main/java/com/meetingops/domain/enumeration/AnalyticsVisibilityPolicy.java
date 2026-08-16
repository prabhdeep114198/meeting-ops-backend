package com.meetingops.domain.enumeration;

/**
 * Enumeration of organization policies governing the visibility of individual engagement metrics.
 *
 * <p>Conforming to SRS Section 3.8, FR-8.4, and PRIV-4.</p>
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public enum AnalyticsVisibilityPolicy {

    /** Default: Individual metrics are visible only to the individual themself and the meeting Facilitator. */
    SELF_AND_FACILITATOR,

    /** Strict: Individual metrics visible only to the individual themself. */
    SELF_ONLY,

    /** Individual metrics visible to managers only with explicit documented justification. */
    MANAGER_OVERRIDE_ENABLED
}
