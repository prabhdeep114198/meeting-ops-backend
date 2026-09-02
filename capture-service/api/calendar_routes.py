import logging
from fastapi import APIRouter, Header, Request, HTTPException
from typing import Optional, Dict, Any
from datetime import datetime
from models import (
    CalendarWebhookPayload,
    ManualBotJoinRequest,
    BotJoinResponse,
    MeetingPlatform,
    AttendeeInfo
)
from api.capture_routes import bot_manager

logger = logging.getLogger("capture-service.calendar")
router = APIRouter(prefix="/api/v1/integrations", tags=["Calendar Integrations"])

@router.post("/calendar")
async def handle_calendar_webhook(
    request: Request,
    payload: Optional[CalendarWebhookPayload] = None,
    x_goog_channel_id: Optional[str] = Header(None),
    x_ms_client_state: Optional[str] = Header(None)
):
    """
    Calendar Webhook Ingestion Endpoint (FR-1.1).
    Supports Google Calendar Push Notifications and Microsoft Graph Change Notifications.
    """
    logger.info("Received calendar webhook notification")

    # If direct JSON payload provided (e.g. from internal sync or simulated webhook)
    if payload:
        logger.info(f"Processing calendar event: '{payload.title}' from {payload.provider} (Start: {payload.startTime})")
        
        # Convert to BotJoinRequest and trigger pre-join consent & capture
        join_req = ManualBotJoinRequest(
            meetingUrl=payload.meetingUrl,
            platform=payload.platform,
            title=payload.title,
            organizerEmail=payload.organizerEmail,
            organizerOptIn=payload.organizerOptIn,
            attendees=payload.attendees,
            organizationId=payload.organizationId
        )

        response = bot_manager.dispatch_bot_join(join_req)
        return {
            "status": "PROCESSED",
            "calendarEventId": payload.calendarEventId,
            "provider": payload.provider,
            "botResponse": response
        }

    # Handle raw provider webhooks (Google / Microsoft)
    raw_body = await request.body()
    logger.info(f"Raw webhook notification received (bytes={len(raw_body)})")

    # Acknowledge Google channel sync or Microsoft client state handshake
    return {
        "status": "ACKNOWLEDGED",
        "timestamp": datetime.utcnow().isoformat(),
        "provider": "google" if x_goog_channel_id else ("microsoft" if x_ms_client_state else "generic")
    }
