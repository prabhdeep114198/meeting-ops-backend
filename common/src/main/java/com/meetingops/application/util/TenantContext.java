package com.meetingops.application.util;

import com.meetingops.domain.enumeration.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Thread-local context carrying the current tenant and user identity.
 * Populated by gateway header filters (X-Org-ID, X-User-ID, X-User-Role) or JWT claims.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Data
@Builder
public class TenantContext {
    private UUID organizationId;
    private UUID userId;
    private UserRole userRole;
    private String email;
    private boolean auditModeEnabled;
}
