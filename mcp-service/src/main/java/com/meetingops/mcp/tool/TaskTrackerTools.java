package com.meetingops.mcp.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP server tool for creating task-tracker entries.
 *
 * <p>Implements the {@code create-task} MCP tool as defined in FR-5.1
 * and FR-5.4. This tool generates a preview payload for a task-tracker
 * entry without executing against the real task tracker until human approval.</p>
 *
 * <p>The interface is designed to accept a real Jira/Linear/Asana client
 * without changing the agent logic (FR-4.3).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class TaskTrackerTools {

    /**
     * Creates a draft task-tracker entry.
     *
     * <p>Returns a preview payload that can be reviewed by a human
     * before being executed against the actual task-tracking system.</p>
     *
     * @param title       the task title
     * @param description the task description
     * @param assignee    the task assignee
     * @param dueDate     the task due date (ISO 8601 format)
     * @return a JSON-formatted draft task payload
     */
    @Tool(name = "create-task", description = """
            Create a draft task-tracker entry for a meeting action item.
            Returns a preview payload for human review before execution.
            Never execute against the real task tracker without approval.
            """)
    public String createTask(
            @ToolParam(description = "The task title", required = true) final String title,
            @ToolParam(description = "The task description", required = true) final String description,
            @ToolParam(description = "The task assignee", required = true) final String assignee,
            @ToolParam(description = "The task due date in ISO 8601 format", required = true) final String dueDate) {

        // TODO: Implement actual task tracker integration (mock provider for now)
        log.info("MCP Tool: create-task called - title={}, assignee={}, dueDate={}",
                title, assignee, dueDate);

        return """
                {
                  "status": "draft",
                  "payload": {
                    "title": "%s",
                    "description": "%s",
                    "assignee": "%s",
                    "dueDate": "%s",
                    "provider": "mock",
                    "requiresApproval": true
                  }
                }
                """.formatted(title, description, assignee, dueDate);
    }
}
