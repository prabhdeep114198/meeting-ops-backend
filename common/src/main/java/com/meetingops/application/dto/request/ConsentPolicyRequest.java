package com.meetingops.application.dto.request;

import com.meetingops.domain.enumeration.AnalyticsVisibilityPolicy;
import com.meetingops.domain.enumeration.ConsentPolicy;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for configuring organization consent policy and analytics visibility (PRIV-1, FR-10.3).
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public record ConsentPolicyRequest(
        @NotNull(message = "Consent policy must be specified")
        ConsentPolicy consentPolicy,

        AnalyticsVisibilityPolicy analyticsVisibilityPolicy,

        Integer audioRetentionDays,

        Integer analyticsRetentionDays
) {
}
