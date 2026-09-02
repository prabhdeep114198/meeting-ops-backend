import pytest
from models import (
    ConsentPolicy,
    ConsentStatus,
    AttendeeInfo,
    MeetingPlatform,
    ManualBotJoinRequest,
    BotStatus
)
from services.consent_engine import ConsentVerificationEngine
from services.bot_manager import BotOrchestrationManager

def test_consent_notify_only():
    """FR-1.3, PRIV-1: NOTIFY_ONLY permits join with in-meeting notice"""
    engine = ConsentVerificationEngine()
    result = engine.evaluate_pre_join_consent(
        policy=ConsentPolicy.NOTIFY_ONLY,
        organizer_email="organizer@acme.com",
        organizer_opt_in=False, # even if false, policy allows
        attendees=[AttendeeInfo(email="dev1@acme.com", consentStatus=ConsentStatus.PENDING)]
    )
    assert result.allowed is True
    assert result.consentPolicy == ConsentPolicy.NOTIFY_ONLY
    assert "NOTIFY_ONLY" in result.reason

def test_consent_meeting_opt_in_success():
    """FR-1.3, PRIV-1: MEETING_OPT_IN allows join when organizer opted in"""
    engine = ConsentVerificationEngine()
    result = engine.evaluate_pre_join_consent(
        policy=ConsentPolicy.MEETING_OPT_IN,
        organizer_email="organizer@acme.com",
        organizer_opt_in=True,
        attendees=[]
    )
    assert result.allowed is True
    assert result.consentPolicy == ConsentPolicy.MEETING_OPT_IN

def test_consent_meeting_opt_in_aborts():
    """FR-1.3, PRIV-1, PRIV-2: MEETING_OPT_IN aborts if organizer did not opt in"""
    engine = ConsentVerificationEngine()
    result = engine.evaluate_pre_join_consent(
        policy=ConsentPolicy.MEETING_OPT_IN,
        organizer_email="organizer@acme.com",
        organizer_opt_in=False,
        attendees=[]
    )
    assert result.allowed is False
    assert "did not opt in" in result.reason
    assert "organizer@acme.com" in result.missingConsentEmails

def test_consent_participant_opt_in_success():
    """FR-1.3, PRIV-1: PARTICIPANT_OPT_IN succeeds when ALL attendees granted consent"""
    engine = ConsentVerificationEngine()
    attendees = [
        AttendeeInfo(email="alex@acme.com", consentStatus=ConsentStatus.GRANTED),
        AttendeeInfo(email="priya@acme.com", consentStatus=ConsentStatus.GRANTED)
    ]
    result = engine.evaluate_pre_join_consent(
        policy=ConsentPolicy.PARTICIPANT_OPT_IN,
        organizer_email="organizer@acme.com",
        organizer_opt_in=True,
        attendees=attendees
    )
    assert result.allowed is True
    assert len(result.missingConsentEmails) == 0

def test_consent_participant_opt_in_hard_abort_priv2():
    """
    CRITICAL TEST (PRIV-2 & FR-1.3):
    If any attendee is missing consent or declined, join must HARD ABORT.
    Never degrade to stealth or partial recording!
    """
    engine = ConsentVerificationEngine()
    attendees = [
        AttendeeInfo(email="alex@acme.com", consentStatus=ConsentStatus.GRANTED),
        AttendeeInfo(email="declined_user@acme.com", consentStatus=ConsentStatus.DECLINED),
        AttendeeInfo(email="pending_user@acme.com", consentStatus=ConsentStatus.PENDING)
    ]
    result = engine.evaluate_pre_join_consent(
        policy=ConsentPolicy.PARTICIPANT_OPT_IN,
        organizer_email="organizer@acme.com",
        organizer_opt_in=True,
        attendees=attendees
    )
    assert result.allowed is False
    assert "declined_user@acme.com" in result.missingConsentEmails
    assert "pending_user@acme.com" in result.missingConsentEmails
    assert "Bot join aborted" in result.reason

def test_bot_orchestration_dispatch_and_abort():
    """Test full orchestrator handling allowed and aborted joins"""
    mgr = BotOrchestrationManager()
    mgr.set_org_policy("org-test", ConsentPolicy.PARTICIPANT_OPT_IN)

    # 1. Request with missing consent -> should ABORT
    abort_req = ManualBotJoinRequest(
        meetingUrl="https://meet.google.com/abc-defg-hij",
        platform=MeetingPlatform.MEET,
        organizerEmail="org@test.com",
        organizationId="org-test",
        attendees=[AttendeeInfo(email="user1@test.com", consentStatus=ConsentStatus.PENDING)]
    )
    res_abort = mgr.dispatch_bot_join(abort_req)
    assert res_abort.status == BotStatus.ABORTED_NO_CONSENT
    assert res_abort.consentEvaluation.allowed is False

    # 2. Change policy to NOTIFY_ONLY -> should dispatch bot successfully
    mgr.set_org_policy("org-test", ConsentPolicy.NOTIFY_ONLY)
    success_req = ManualBotJoinRequest(
        meetingUrl="https://zoom.us/j/1234567890",
        platform=MeetingPlatform.ZOOM,
        organizerEmail="org@test.com",
        organizationId="org-test",
        attendees=[AttendeeInfo(email="user1@test.com", consentStatus=ConsentStatus.PENDING)]
    )
    res_success = mgr.dispatch_bot_join(success_req)
    assert res_success.status == BotStatus.JOINING
    assert res_success.consentEvaluation.allowed is True
    assert "Zoom" in res_success.meetingUrl or "zoom" in res_success.meetingUrl

def test_participant_opt_out_analytics():
    """FR-1.5: Participant can opt out of analytics for a specific meeting"""
    mgr = BotOrchestrationManager()
    success_req = ManualBotJoinRequest(
        meetingUrl="https://teams.microsoft.com/l/meetup-join/19%3a",
        platform=MeetingPlatform.TEAMS,
        organizerEmail="org@test.com",
        organizationId="org-default",
        attendees=[]
    )
    res = mgr.dispatch_bot_join(success_req)
    meeting_id = res.meetingId

    # Mark user as opted out
    opt_out_success = mgr.record_opt_out(meeting_id, "optout_user@acme.com")
    assert opt_out_success is True

    job = mgr.get_job_status(meeting_id)
    assert "optout_user@acme.com" in job.excluded_attendees

if __name__ == "__main__":
    pytest.main(["-v", __file__])
