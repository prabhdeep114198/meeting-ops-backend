package com.meetingops.application.ai.service;

import com.meetingops.domain.model.ExtractedItem;

import java.util.List;
import java.util.UUID;

/**
 * AI service interface for the extraction agent.
 *
 * <p>Responsible for parsing meeting transcripts and producing a structured
 * list of candidate Action Items and Decisions. Per FR-2.3, the agent
 * must NOT fabricate owners or deadlines not evidenced in the transcript.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface ExtractionAgent {

    /**
     * Extracts structured items from a meeting transcript.
     *
     * @param meetingId      the meeting identifier
     * @param transcriptText the raw transcript text
     * @return list of extracted items with confidence scores
     */
    List<ExtractedItem> extract(UUID meetingId, String transcriptText);
}
