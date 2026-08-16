package com.meetingops.application.dto.response;

import com.meetingops.domain.enumeration.AnalyticsVisibilityPolicy;
import com.meetingops.domain.enumeration.ConsentPolicy;

import java.util.UUID;

/**
 * Response payload representing organization consent policy configuration.
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public record ConsentPolicyResponse(
        UUID organizationId,
        String organizationName,
        ConsentPolicy consentPolicy,
        AnalyticsVisibilityPolicy analyticsVisibilityPolicy,
        int audioRetentionDays,
        int analyticsRetentionDays
) {
}
