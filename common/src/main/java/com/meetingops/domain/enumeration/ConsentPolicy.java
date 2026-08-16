package com.meetingops.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of organization-level recording & analysis consent policies.
 *
 * <p>Conforming to SRS Section 8.2 and PRIV-1.</p>
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public enum ConsentPolicy {

    /** Participants are notified that recording/analysis is active; organization asserts legal basis. */
    NOTIFY_ONLY("notify_only"),

    /** The meeting organizer must explicitly opt-in per meeting. */
    MEETING_OPT_IN("meeting_opt_in"),

    /** Every participant must individually grant affirmative consent before capture proceeds. */
    PARTICIPANT_OPT_IN("participant_opt_in");

    private final String value;

    ConsentPolicy(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ConsentPolicy fromValue(String value) {
        if (value == null) {
            return NOTIFY_ONLY;
        }
        for (ConsentPolicy policy : values()) {
            if (policy.value.equalsIgnoreCase(value) || policy.name().equalsIgnoreCase(value)) {
                return policy;
            }
        }
        return NOTIFY_ONLY;
    }
}
