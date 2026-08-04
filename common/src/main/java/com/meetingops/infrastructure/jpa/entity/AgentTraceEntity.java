package com.meetingops.infrastructure.jpa.entity;

import com.meetingops.domain.enumeration.AgentRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the agent_traces table.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Entity
@Table(name = "agent_traces")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentTraceEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "meeting_id", nullable = false, columnDefinition = "uuid")
    private UUID meetingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_role", nullable = false)
    private AgentRole agentRole;

    @Column(name = "step_index", nullable = false)
    private int stepIndex;

    @Column(name = "tool_called", columnDefinition = "text")
    private String toolCalled;

    @Column(name = "tool_input", columnDefinition = "text")
    private String toolInput;

    @Column(name = "tool_output", columnDefinition = "text")
    private String toolOutput;

    @Column(name = "reasoning_excerpt", columnDefinition = "text")
    private String reasoningExcerpt;

    @Column(nullable = false)
    private Instant timestamp;
}
