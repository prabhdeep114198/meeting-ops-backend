package com.meetingops.domain.model;

import com.meetingops.domain.enumeration.AgentRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a trace entry in the agent processing pipeline.
 *
 * <p>AgentTrace entries form a distributed trace spanning the entire
 * ingestion → extraction → grounding → validation → drafting pipeline,
 * providing observability into each step's tool calls and reasoning.</p>
 *
 * @param id             unique identifier
 * @param meetingId      the meeting being processed
 * @param agentRole      which agent produced this trace entry
 * @param stepIndex      sequential index within the agent's processing steps
 * @param toolCalled     name of the MCP tool called (if any)
 * @param toolInput      input provided to the tool
 * @param toolOutput     output received from the tool
 * @param reasoningExcerpt the agent's reasoning excerpt
 * @param timestamp      when this step occurred
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record AgentTrace(
        UUID id,
        UUID meetingId,
        AgentRole agentRole,
        int stepIndex,
        String toolCalled,
        String toolInput,
        String toolOutput,
        String reasoningExcerpt,
        Instant timestamp
) {
    /**
     * Creates a new AgentTrace entry with auto-generated UUID.
     *
     * @param meetingId          the meeting being processed
     * @param agentRole          which agent produced this trace
     * @param stepIndex          sequential step index
     * @param toolCalled         MCP tool name
     * @param toolInput          tool input
     * @param toolOutput         tool output
     * @param reasoningExcerpt   agent reasoning excerpt
     * @return a new AgentTrace
     */
    public static AgentTrace create(final UUID meetingId,
                                    final AgentRole agentRole,
                                    final int stepIndex,
                                    final String toolCalled,
                                    final String toolInput,
                                    final String toolOutput,
                                    final String reasoningExcerpt) {
        return new AgentTrace(
                UUID.randomUUID(),
                meetingId,
                agentRole,
                stepIndex,
                toolCalled,
                toolInput,
                toolOutput,
                reasoningExcerpt,
                Instant.now()
        );
    }
}
