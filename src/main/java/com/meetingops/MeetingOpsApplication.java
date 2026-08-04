package com.meetingops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * MeetingOps Application Entry Point.
 *
 * <p>AI Meeting Intelligence &amp; Follow-Through Platform that extracts action items,
 * decisions, and commitments from meeting transcripts, grounds them against
 * organizational meeting history, and routes drafted follow-through actions
 * through human approval before execution.</p>
 *
 * <p>Architecture: Clean / Hexagonal Architecture
 * <ul>
 *   <li>Domain — Core business logic, entities, and interfaces</li>
 *   <li>Application — Application services, AI services, DTOs, and events</li>
 *   <li>Infrastructure — Persistence, configuration, and external integrations</li>
 *   <li>Interfaces — REST controllers, event handlers, and adapters</li>
 *   <li>MCP — Model Context Protocol tools and server configuration</li>
 * </ul>
 * </p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class MeetingOpsApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(MeetingOpsApplication.class, args);
    }
}
