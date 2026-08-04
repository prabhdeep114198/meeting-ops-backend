package com.meetingops.domain.enumeration;

/**
 * Enumeration of agent roles in the processing pipeline.
 *
 * <p>Each agent performs a specific step in the end-to-end meeting processing
 * pipeline, from transcript ingestion to draft action generation.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public enum AgentRole {

    /** Agent responsible for extracting structured items from transcripts. */
    EXTRACTION,

    /** Agent responsible for grounding items against historical meeting data via RAG. */
    GROUNDING,

    /** Agent responsible for validating extracted items and flagging ambiguous ones. */
    VALIDATION,

    /** Agent responsible for generating draft follow-through actions via MCP tools. */
    DRAFTING
}
