package com.meetingops.application.ai.service;

import com.meetingops.domain.model.ExtractedItem;
import com.meetingops.domain.model.GroundingResult;

import java.util.List;
import java.util.UUID;

/**
 * AI service interface for the grounding (context) agent.
 *
 * <p>Responsible for querying a vector store of the organization's prior
 * meeting history (RAG) to detect duplicate, conflicting, or continuing
 * action items. Retrieval is restricted to the tenant's namespace.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface GroundingAgent {

    /**
     * Grounds a batch of extracted items against historical meeting data.
     *
     * @param items            the extracted items to ground
     * @param organizationId   the tenant identifier for namespace isolation
     * @return list of grounding results with classification and rationale
     */
    List<GroundingResult> ground(List<ExtractedItem> items, UUID organizationId);
}
