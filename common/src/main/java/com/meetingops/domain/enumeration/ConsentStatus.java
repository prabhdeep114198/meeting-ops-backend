package com.meetingops.domain.enumeration;

/**
 * Enumeration of meeting-level and participant-level consent verification states.
 *
 * <p>Enforces pre-join capture verification (FR-1.3, PRIV-1, PRIV-2).</p>
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public enum ConsentStatus {

    /** Consent check is pending verification before meeting start. */
    PENDING,

    /** Required consent has been satisfied per organization policy. */
    SATISFIED,

    /** Consent granted explicitly by participant. */
    GRANTED,

    /** Consent declined by participant. */
    DECLINED,

    /** Consent revoked by participant after initial grant. */
    REVOKED,

    /** Bot capture blocked because required consent was not obtained. */
    BLOCKED_NO_CONSENT,

    /** Bot capture was aborted. */
    CONSENT_ABORTED,

    /** Consent not applicable (e.g. manual transcript upload). */
    NOT_APPLICABLE
}
