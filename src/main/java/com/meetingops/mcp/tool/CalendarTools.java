package com.meetingops.mcp.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP server tool for creating calendar reminders.
 *
 * <p>Implements the {@code calendar-reminder} MCP tool as defined in FR-5.2
 * and FR-5.4. Generates a preview payload for a calendar reminder without
 * executing against the real calendar system until human approval.</p>
 *
 * <p>The interface is designed for a real Google/Outlook Calendar API
 * swap without changing the agent logic (FR-4.3).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class CalendarTools {

    /**
     * Creates a draft calendar reminder for a deadline-bearing action item.
     *
     * @param title       the reminder title
     * @param description the reminder description
     * @param attendee    the attendee to receive the reminder
     * @param reminderDate the reminder date/time (ISO 8601 format)
     * @return a JSON-formatted draft calendar reminder payload
     */
    @Tool(name = "calendar-reminder", description = """
            Create a draft calendar reminder for a meeting action item deadline.
            Returns a preview payload for human review before execution.
            Never execute against the real calendar without approval.
            """)
    public String createCalendarReminder(
            @ToolParam(description = "The reminder title", required = true) final String title,
            @ToolParam(description = "The reminder description", required = true) final String description,
            @ToolParam(description = "The attendee to receive the reminder", required = true) final String attendee,
            @ToolParam(description = "The reminder date/time in ISO 8601 format", required = true) final String reminderDate) {

        // TODO: Implement actual calendar integration (mock provider for now)
        log.info("MCP Tool: calendar-reminder called - title={}, attendee={}, date={}",
                title, attendee, reminderDate);

        return """
                {
                  "status": "draft",
                  "payload": {
                    "title": "%s",
                    "description": "%s",
                    "attendee": "%s",
                    "reminderDate": "%s",
                    "provider": "mock",
                    "requiresApproval": true
                  }
                }
                """.formatted(title, description, attendee, reminderDate);
    }
}
