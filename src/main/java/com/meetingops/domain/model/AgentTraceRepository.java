package com.meetingops.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Port interface for agent trace persistence in the domain layer.
 *
 * <p>Supports distributed trace retrieval per meeting for observability
 * as required by FR-7.3.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface AgentTraceRepository {

    /**
     * Saves an agent trace entry.
     *
     * @param trace the trace entry to save
     * @return the persisted trace
     */
    AgentTrace save(AgentTrace trace);

    /**
     * Finds all trace entries for a given meeting, ordered by timestamp.
     *
     * @param meetingId the meeting identifier
     * @return list of trace entries for the meeting
     */
    List<AgentTrace> findByMeetingId(UUID meetingId);

    /**
     * Saves a batch of trace entries.
     *
     * @param traces the trace entries to save
     * @return the persisted traces
     */
    List<AgentTrace> saveAll(List<AgentTrace> traces);
}
