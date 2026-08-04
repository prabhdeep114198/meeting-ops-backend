package com.meetingops.aipipeline.service;

import java.util.UUID;

/**
 * Application service interface for the AI agent processing pipeline.
 *
 * <p>Orchestrates the end-to-end agent pipeline: extraction → grounding →
 * validation → drafting. Consumes the {@code meeting.transcribed} event
 * and produces draft actions in the review queue.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface AgentPipelineService {

    /**
     * Processes a meeting through the full agent pipeline.
     * Triggered by the {@code meeting.transcribed} event.
     *
     * <p>Pipeline steps:
     * <ol>
     *   <li>Extract structured items from the transcript</li>
     *   <li>Ground items against historical meeting data via RAG</li>
     *   <li>Validate items and flag ambiguous ones</li>
     *   <li>Generate draft follow-through actions via MCP tools</li>
     * </ol>
     * </p>
     *
     * @param meetingId the meeting to process
     */
    void processMeeting(UUID meetingId);

    /**
     * Retrigger processing for a failed meeting.
     * Supports exponential backoff retry (max 3 attempts per NFR-3.2).
     *
     * @param meetingId the meeting to retry
     */
    void retryProcessing(UUID meetingId);
}
