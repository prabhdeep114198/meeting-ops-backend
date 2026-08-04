package com.meetingops.application.service;

import com.meetingops.application.dto.request.ReviewDecisionRequest;
import com.meetingops.application.dto.response.DraftActionResponse;
import com.meetingops.application.dto.response.ReviewDecisionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Application service interface for the human review and approval workflow.
 *
 * <p>Manages the review queue, draft action decisions, and audit trail
 * persistence. Enforces the HITL constraint (FR-6.3) that no draft action
 * reaches EXECUTED state without explicit human approval.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface ReviewService {

    /**
     * Retrieves the review queue for an organization (draft actions pending review).
     *
     * @param organizationId the organization identifier
     * @return list of draft action responses pending review
     */
    List<DraftActionResponse> getReviewQueue(UUID organizationId);

    /**
     * Submits a review decision for a draft action.
     *
     * @param draftActionId the draft action identifier
     * @param reviewerId    the reviewer's user identifier
     * @param request       the review decision request
     * @return the persisted review decision response
     */
    ReviewDecisionResponse submitDecision(UUID draftActionId, UUID reviewerId, ReviewDecisionRequest request);

    /**
     * Retrieves the audit trail for a specific draft action.
     *
     * @param draftActionId the draft action identifier
     * @return list of review decisions (audit trail)
     */
    List<ReviewDecisionResponse> getAuditTrail(UUID draftActionId);

    /**
     * Retrieves a single draft action by its identifier.
     *
     * @param id the draft action identifier
     * @return the draft action response
     */
    DraftActionResponse getDraftAction(UUID id);

    /**
     * Bulk-approves low-risk, high-confidence draft actions.
     * Conflict-flagged and needs-clarification items cannot be bulk-approved.
     *
     * @param organizationId the organization identifier
     * @param draftActionIds list of draft action identifiers to approve
     * @param reviewerId     the reviewer performing the bulk approval
     * @return list of resulting review decisions
     */
    List<ReviewDecisionResponse> bulkApprove(UUID organizationId,
                                             List<UUID> draftActionIds,
                                             UUID reviewerId);
}
