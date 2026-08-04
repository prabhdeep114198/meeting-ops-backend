package com.meetingops.mcp.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP server tool for drafting follow-up emails.
 *
 * <p>Implements the {@code draft-email} MCP tool as defined in FR-5.3
 * and FR-5.4. Generates a draft email summarizing the meeting's decisions
 * and action items. Per NFR-4.5, this tool shall never be configured with
 * send-capability that bypasses the human approval gate.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class EmailTools {

    /**
     * Creates a draft follow-up email summarizing meeting decisions and action items.
     *
     * <p>The email is addressed to the meeting's attendees and includes
     * a summary of decisions made and action items assigned. The tool
     * returns a preview payload only; it never sends the email without
     * explicit human approval per FR-6.3.</p>
     *
     * @param subject   the email subject line
     * @param body      the email body content
     * @param recipients comma-separated list of recipient email addresses
     * @return a JSON-formatted draft email payload
     */
    @Tool(name = "draft-email", description = """
            Draft a follow-up email summarizing meeting decisions and action items.
            Returns a preview payload for human review before execution.
            NEVER send this email without explicit human approval.
            This tool only creates drafts; it has no send capability.
            """)
    public String draftEmail(
            @ToolParam(description = "The email subject line", required = true) final String subject,
            @ToolParam(description = "The email body content", required = true) final String body,
            @ToolParam(description = "Comma-separated list of recipient email addresses", required = true) final String recipients) {

        // TODO: Implement actual email integration (mock provider for now)
        log.info("MCP Tool: draft-email called - subject={}, recipients={}",
                subject, recipients);

        return """
                {
                  "status": "draft",
                  "payload": {
                    "subject": "%s",
                    "body": "%s",
                    "recipients": "%s",
                    "provider": "mock",
                    "requiresApproval": true,
                    "sendCapability": false
                  }
                }
                """.formatted(subject, body, recipients);
    }
}
