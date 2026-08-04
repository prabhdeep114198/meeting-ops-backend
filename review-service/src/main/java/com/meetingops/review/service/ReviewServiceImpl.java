package com.meetingops.review.service;

import com.meetingops.application.dto.request.ReviewDecisionRequest;
import com.meetingops.application.dto.response.DraftActionResponse;
import com.meetingops.application.dto.response.ReviewDecisionResponse;
import com.meetingops.domain.enumeration.AuditAction;
import com.meetingops.domain.enumeration.DraftActionStatus;
import com.meetingops.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service implementation for the human review and approval workflow.
 *
 * <p>Enforces the HITL constraint (FR-6.3) that no draft action reaches
 * EXECUTED state without explicit human approval. All review decisions
 * are persisted as immutable audit records (NFR-6.1).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final DraftActionRepository draftActionRepository;
    private final ReviewDecisionRepository reviewDecisionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DraftActionResponse> getReviewQueue(final UUID organizationId) {
        // TODO: Enforce tenant isolation at the data-access layer
        return draftActionRepository.findPendingReviewByOrganizationId(organizationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewDecisionResponse submitDecision(final UUID draftActionId,
                                                  final UUID reviewerId,
                                                  final ReviewDecisionRequest request) {
        DraftAction draftAction = draftActionRepository.findById(draftActionId)
                .orElseThrow(() -> new NoSuchElementException(
                        "DraftAction not found: " + draftActionId));

        // Validate: DRAFTED actions can only be transitioned by an explicit decision
        if (draftAction.status() != DraftActionStatus.DRAFTED) {
            throw new IllegalStateException(
                    "Draft action " + draftActionId + " is already " + draftAction.status());
        }

        DraftActionStatus newStatus = switch (request.action()) {
            case APPROVE -> DraftActionStatus.APPROVED;
            case EDIT_AND_APPROVE -> DraftActionStatus.EDITED;
            case REJECT -> DraftActionStatus.REJECTED;
            default -> draftAction.status();
        };

        String finalPayload = request.finalPayload() != null
                ? request.finalPayload()
                : draftAction.payloadJson();

        // Apply the approval to the draft action
        DraftAction updated = draftAction.withApproval(newStatus, finalPayload);
        draftActionRepository.save(updated);

        // Record the audit decision
        ReviewDecision decision = ReviewDecision.create(
                draftActionId,
                reviewerId,
                request.action(),
                draftAction.originalPayload(),
                finalPayload,
                request.reason() != null ? request.reason() : ""
        );
        reviewDecisionRepository.save(decision);

        // TODO: If approved and all draft actions for the meeting are approved,
        // transition meeting status to RESOLVED

        return mapDecisionToResponse(decision);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDecisionResponse> getAuditTrail(final UUID draftActionId) {
        return reviewDecisionRepository.findByDraftActionId(draftActionId)
                .stream()
                .map(this::mapDecisionToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DraftActionResponse getDraftAction(final UUID id) {
        DraftAction draftAction = draftActionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("DraftAction not found: " + id));
        return mapToResponse(draftAction);
    }

    @Override
    @Transactional
    public List<ReviewDecisionResponse> bulkApprove(final UUID organizationId,
                                                     final List<UUID> draftActionIds,
                                                     final UUID reviewerId) {
        // TODO: Validate that none of the items are conflict-flagged or needs-clarification
        // TODO: Only bulk-approve new-classified, high-confidence items (FR-6.5)

        return draftActionIds.stream()
                .map(id -> {
                    ReviewDecisionRequest request = new ReviewDecisionRequest(
                            AuditAction.APPROVE,
                            null,
                            "Bulk approved"
                    );
                    return submitDecision(id, reviewerId, request);
                })
                .collect(Collectors.toList());
    }

    /**
     * Maps a DraftAction domain model to a response DTO.
     */
    private DraftActionResponse mapToResponse(final DraftAction draftAction) {
        return new DraftActionResponse(
                draftAction.id(),
                draftAction.extractedItemId(),
                draftAction.meetingId(),
                draftAction.actionType(),
                draftAction.payloadJson(),
                draftAction.status(),
                draftAction.status() == DraftActionStatus.DRAFTED,
                draftAction.createdAt()
        );
    }

    /**
     * Maps a ReviewDecision domain model to a response DTO.
     */
    private ReviewDecisionResponse mapDecisionToResponse(final ReviewDecision decision) {
        return new ReviewDecisionResponse(
                decision.id(),
                decision.draftActionId(),
                decision.reviewerId(),
                decision.action(),
                decision.originalPayload(),
                decision.finalPayload(),
                decision.reason(),
                decision.timestamp()
        );
    }
}
