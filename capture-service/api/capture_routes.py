import logging
from fastapi import APIRouter, HTTPException, Depends
from typing import Dict, Any, Optional
from models import (
    ManualBotJoinRequest,
    BotJoinResponse,
    ConsentPolicy,
    ConsentEvaluationResult,
    OptOutRequest,
    AttendeeInfo
)
from services.bot_manager import BotOrchestrationManager

logger = logging.getLogger("capture-service.routes")
router = APIRouter(prefix="/api/v1", tags=["Meeting Capture & Consent"])

# Global singleton orchestrator
bot_manager = BotOrchestrationManager()

@router.get("/capture/health")
def health_check():
    return {
        "status": "UP",
        "service": "capture-service",
        "port": 8085,
        "capabilities": ["calendar-sync", "consent-state-machine", "bot-orchestration", "s3-ephemeral-audio", "kafka-events"]
    }

@router.post("/capture/manual-bot-join", response_model=BotJoinResponse)
def manual_bot_join(request: ManualBotJoinRequest):
    """
    Dispatches an autonomous bot to join Zoom, Teams, or Google Meet.
    Evaluates pre-join consent (FR-1.3, PRIV-1, PRIV-2) before joining.
    """
    logger.info(f"Received manual bot join request for URL: {request.meetingUrl} on platform: {request.platform}")
    response = bot_manager.dispatch_bot_join(request)
    return response

@router.get("/capture/status/{meeting_id}")
def get_capture_status(meeting_id: str):
    """
    Polls the real-time bot join & audio capture status for a meeting.
    """
    job = bot_manager.get_job_status(meeting_id)
    if not job:
        raise HTTPException(status_code=404, detail=f"Capture job for meeting {meeting_id} not found")
    
    return {
        "meetingId": job.meeting_id,
        "jobId": job.job_id,
        "status": job.status,
        "platform": job.platform,
        "meetingUrl": job.meeting_url,
        "botDisplayName": job.bot_display_name,
        "chatNoticeSent": job.chat_notice_sent,
        "audioS3Uri": job.audio_s3_uri,
        "consentEvaluation": job.consent_evaluation,
        "startedAt": job.started_at,
        "endedAt": job.ended_at,
        "errorMessage": job.error_message,
        "excludedAttendees": list(job.excluded_attendees)
    }

@router.post("/capture/opt-out")
def participant_opt_out(request: OptOutRequest):
    """
    Participant Analytics Opt-Out (FR-1.5).
    Marks participant's utterances to be excluded from analytics calculations.
    """
    success = bot_manager.record_opt_out(request.meetingId, request.participantEmail)
    return {
        "meetingId": request.meetingId,
        "participantEmail": request.participantEmail,
        "excludeAnalytics": request.excludeAnalytics,
        "status": "OPTED_OUT" if success else "RECORDED_PENDING"
    }

@router.post("/consent/evaluate", response_model=ConsentEvaluationResult)
def evaluate_consent(
    policy: ConsentPolicy,
    organizer_email: str,
    organizer_opt_in: bool = True,
    attendees: list[AttendeeInfo] = None
):
    """
    Dry-run pre-join consent evaluation endpoint.
    """
    attendees_list = attendees or []
    return bot_manager.consent_engine.evaluate_pre_join_consent(
        policy=policy,
        organizer_email=organizer_email,
        organizer_opt_in=organizer_opt_in,
        attendees=attendees_list
    )

@router.put("/consent/policy")
def set_organization_consent_policy(org_id: str, policy: ConsentPolicy):
    """
    Updates the organization consent policy mode (NOTIFY_ONLY, MEETING_OPT_IN, PARTICIPANT_OPT_IN).
    """
    bot_manager.set_org_policy(org_id, policy)
    logger.info(f"Updated consent policy for org {org_id} to: {policy}")
    return {
        "organizationId": org_id,
        "consentPolicy": policy,
        "message": f"Organization consent policy updated to {policy}"
    }

@router.get("/consent/policy/{org_id}")
def get_organization_consent_policy(org_id: str):
    """
    Gets the organization consent policy mode.
    """
    policy = bot_manager.get_org_policy(org_id)
    return {
        "organizationId": org_id,
        "consentPolicy": policy
    }
