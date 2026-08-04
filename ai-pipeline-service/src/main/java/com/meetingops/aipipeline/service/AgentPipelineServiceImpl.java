package com.meetingops.aipipeline.service;

import com.meetingops.aipipeline.ai.service.*;
import com.meetingops.domain.enumeration.MeetingStatus;
import com.meetingops.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service implementation for the AI agent processing pipeline.
 *
 * <p>Orchestrates the end-to-end agent pipeline:
 * <ol>
 *   <li>Extraction agent parses the transcript into structured items</li>
 *   <li>Grounding agent classifies items against historical data via RAG</li>
 *   <li>Validation agent flags ambiguous items</li>
 *   <li>Drafting agent generates follow-through actions via MCP tools</li>
 * </ol>
 * </p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentPipelineServiceImpl implements AgentPipelineService {

    private final MeetingRepository meetingRepository;
    private final ExtractedItemRepository extractedItemRepository;
    private final GroundingResultRepository groundingResultRepository;
    private final DraftActionRepository draftActionRepository;
    private final ExtractionAgent extractionAgent;
    private final GroundingAgent groundingAgent;
    private final ValidationAgent validationAgent;
    private final DraftingAgent draftingAgent;

    @Override
    @Transactional
    public void processMeeting(final UUID meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalStateException("Meeting not found: " + meetingId));

        log.info("Starting agent pipeline for meeting: {}", meetingId);

        // Update status to PROCESSING
        meeting = meetingRepository.save(meeting.withStatus(MeetingStatus.PROCESSING));

        // Step 1: Extraction
        log.info("Step 1: Extracting items from transcript");
        List<ExtractedItem> extractedItems = extractionAgent.extract(meetingId, meeting.transcriptRef());
        extractedItems = extractedItemRepository.saveAll(extractedItems);

        // Step 2: Grounding (RAG against historical data)
        log.info("Step 2: Grounding items against historical data");
        List<GroundingResult> groundingResults = groundingAgent.ground(
                extractedItems, meeting.organizationId());
        groundingResults = groundingResultRepository.saveAll(groundingResults);

        // Step 3: Validation
        log.info("Step 3: Validating extracted items");
        List<ExtractedItem> validatedItems = validationAgent.validate(extractedItems);
        extractedItemRepository.saveAll(validatedItems);

        // Step 4: Drafting (generate follow-through actions via MCP tools)
        log.info("Step 4: Generating draft follow-through actions");
        List<DraftAction> draftActions = draftingAgent.generateDrafts(
                validatedItems, meetingId, meeting.attendees());
        draftActions = draftActionRepository.saveAll(draftActions);

        // Update meeting status to PENDING_REVIEW
        meetingRepository.save(meeting.withStatus(MeetingStatus.PENDING_REVIEW));

        log.info("Agent pipeline completed for meeting: {} ({} items, {} drafts)",
                meetingId, validatedItems.size(), draftActions.size());
    }

    @Override
    @Transactional
    public void retryProcessing(final UUID meetingId) {
        log.info("Retrying agent pipeline for meeting: {}", meetingId);
        // TODO: Implement exponential backoff retry logic (max 3 attempts per NFR-3.2)
        processMeeting(meetingId);
    }
}
