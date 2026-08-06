"""
FastMCP Server for MeetingOps Platform
Provides high-performance, low-latency MCP tools for task tracking, calendar, and email drafting.
"""

from fastmcp import FastMCP
import json
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("fastmcp-meetingops")

# Initialize FastMCP Server
mcp = FastMCP("MeetingOps FastMCP Tools", port=8084)


@mcp.tool(name="create-task", description="Create a draft task-tracker entry for a meeting action item. Returns preview payload.")
def create_task(title: str, description: str, assignee: str, due_date: str) -> str:
    """Creates a draft task-tracker entry for human review."""
    logger.info(f"FastMCP Tool: create-task called - title={title}, assignee={assignee}, due_date={due_date}")
    return json.dumps({
        "status": "draft",
        "payload": {
            "title": title,
            "description": description,
            "assignee": assignee,
            "dueDate": due_date,
            "provider": "fastmcp-mock",
            "requiresApproval": True
        }
    })


@mcp.tool(name="calendar-reminder", description="Create a draft calendar reminder for an action item deadline.")
def create_calendar_reminder(title: str, description: str, attendee: str, reminder_date: str) -> str:
    """Creates a draft calendar reminder for human review."""
    logger.info(f"FastMCP Tool: calendar-reminder called - title={title}, attendee={attendee}, reminder_date={reminder_date}")
    return json.dumps({
        "status": "draft",
        "payload": {
            "title": title,
            "description": description,
            "attendee": attendee,
            "reminderDate": reminder_date,
            "provider": "fastmcp-mock",
            "requiresApproval": True
        }
    })


@mcp.tool(name="draft-email", description="Draft a follow-up email summarizing meeting decisions and action items.")
def draft_email(subject: str, body: str, recipients: str) -> str:
    """Drafts a follow-up email for human review."""
    logger.info(f"FastMCP Tool: draft-email called - subject={subject}, recipients={recipients}")
    return json.dumps({
        "status": "draft",
        "payload": {
            "subject": subject,
            "body": body,
            "recipients": recipients,
            "provider": "fastmcp-mock",
            "requiresApproval": True,
            "sendCapability": False
        }
    })


if __name__ == "__main__":
    logger.info("Starting FastMCP MeetingOps Tool Server on port 8084...")
    mcp.run()
