package com.meetingops.interfaces.rest;

import com.meetingops.application.dto.request.ReviewDecisionRequest;
import com.meetingops.application.dto.response.DraftActionResponse;
import com.meetingops.application.dto.response.ReviewDecisionResponse;
import com.meetingops.application.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for the human review queue and approval workflow.
 *
 * <p>Implements the API interfaces defined in Section 4.2 of the SRS:
 * <ul>
 *   <li>{@code GET /api/v1/review-queue} — Pending draft actions</li>
 *   <li>{@code POST /api/v1/draft-actions/{id}/decision} — Approve/edit/reject</li>
 * </ul>
 * </p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Retrieves the review queue for the authenticated user's organization.
     *
     * @param status optional status filter (DRAFTED, APPROVED, etc.)
     * @return list of draft action responses pending review
     */
    @GetMapping("/review-queue")
    public ResponseEntity<List<DraftActionResponse>> getReviewQueue(
            @RequestParam(required = false) final String status) {
        // TODO: Extract organizationId from authenticated user context
        UUID organizationId = UUID.randomUUID(); // Placeholder
        // TODO: Implement status filtering
        List<DraftActionResponse> queue = reviewService.getReviewQueue(organizationId);
        return ResponseEntity.ok(queue);
    }

    /**
     * Submits a review decision for a draft action.
     *
     * <p>Requires REVIEWER role per FR-6.1. The draft action transitions
     * to EXECUTED state only after explicit approval (FR-6.3).</p>
     *
     * @param id      the draft action identifier
     * @param request the review decision request
     * @return the persisted review decision response
     */
    @PostMapping("/draft-actions/{id}/decision")
    public ResponseEntity<ReviewDecisionResponse> submitDecision(
            @PathVariable final UUID id,
            @Valid @RequestBody final ReviewDecisionRequest request) {
        // TODO: Extract reviewerId from authenticated user context
        UUID reviewerId = UUID.randomUUID(); // Placeholder
        ReviewDecisionResponse response = reviewService.submitDecision(id, reviewerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the audit trail for a specific draft action.
     *
     * @param id the draft action identifier
     * @return list of review decisions (audit trail)
     */
    @GetMapping("/draft-actions/{id}/audit-trail")
    public ResponseEntity<List<ReviewDecisionResponse>> getAuditTrail(
            @PathVariable final UUID id) {
        List<ReviewDecisionResponse> trail = reviewService.getAuditTrail(id);
        return ResponseEntity.ok(trail);
    }

    /**
     * Bulk-approves low-risk, high-confidence draft actions.
     *
     * <p>Conflict-flagged and needs-clarification items cannot be
     * bulk-approved per FR-6.5.</p>
     *
     * @param request map containing draftActionIds list
     * @return list of resulting review decisions
     */
    @PostMapping("/review-queue/bulk-approve")
    public ResponseEntity<List<ReviewDecisionResponse>> bulkApprove(
            @RequestBody final Map<String, List<UUID>> request) {
        // TODO: Extract organizationId and reviewerId from authenticated user context
        UUID organizationId = UUID.randomUUID(); // Placeholder
        UUID reviewerId = UUID.randomUUID(); // Placeholder
        List<UUID> draftActionIds = request.get("draftActionIds");
        List<ReviewDecisionResponse> decisions =
                reviewService.bulkApprove(organizationId, draftActionIds, reviewerId);
        return ResponseEntity.ok(decisions);
    }
}
