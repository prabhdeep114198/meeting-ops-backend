from enum import Enum
from typing import List, Optional, Dict, Any
from datetime import datetime
from pydantic import BaseModel, Field
import uuid

class ConsentPolicy(str, Enum):
    NOTIFY_ONLY = "NOTIFY_ONLY"
    MEETING_OPT_IN = "MEETING_OPT_IN"
    PARTICIPANT_OPT_IN = "PARTICIPANT_OPT_IN"

class ConsentStatus(str, Enum):
    PENDING = "PENDING"
    GRANTED = "GRANTED"
    DECLINED = "DECLINED"

class MeetingPlatform(str, Enum):
    ZOOM = "ZOOM"
    TEAMS = "TEAMS"
    MEET = "MEET"
    MANUAL = "MANUAL"

class MeetingCaptureMode(str, Enum):
    AUTOMATIC_BOT = "AUTOMATIC_BOT"
    MANUAL_UPLOAD = "MANUAL_UPLOAD"

class BotStatus(str, Enum):
    IDLE = "IDLE"
    CHECKING_CONSENT = "CHECKING_CONSENT"
    JOINING = "JOINING"
    RECORDING = "RECORDING"
    COMPLETED = "COMPLETED"
    ABORTED_NO_CONSENT = "ABORTED_NO_CONSENT"
    FAILED = "FAILED"

class AttendeeInfo(BaseModel):
    email: str
    name: Optional[str] = None
    consentStatus: Optional[ConsentStatus] = ConsentStatus.PENDING

class ManualBotJoinRequest(BaseModel):
    meetingUrl: str = Field(..., description="Conference call URL (Zoom, Teams, Google Meet)")
    platform: MeetingPlatform = Field(default=MeetingPlatform.MEET)
    title: Optional[str] = "Live Capture Meeting"
    organizerEmail: str = "organizer@example.com"
    organizerOptIn: bool = True
    attendees: List[AttendeeInfo] = Field(default_factory=list)
    organizationId: Optional[str] = None

class CalendarWebhookPayload(BaseModel):
    provider: str = Field(..., description="google or microsoft")
    calendarEventId: str
    title: str
    meetingUrl: str
    platform: MeetingPlatform
    startTime: datetime
    endTime: datetime
    organizerEmail: str
    organizerOptIn: bool = True
    attendees: List[AttendeeInfo]
    organizationId: Optional[str] = None

class ConsentEvaluationResult(BaseModel):
    allowed: bool
    consentPolicy: ConsentPolicy
    reason: str
    missingConsentEmails: List[str] = Field(default_factory=list)
    evaluatedAt: datetime = Field(default_factory=datetime.utcnow)

class BotJoinResponse(BaseModel):
    captureJobId: str
    meetingId: str
    status: BotStatus
    platform: MeetingPlatform
    meetingUrl: str
    consentEvaluation: ConsentEvaluationResult
    message: str
    joinedAt: Optional[datetime] = None

class OptOutRequest(BaseModel):
    meetingId: str
    participantEmail: str
    organizationId: Optional[str] = None
    excludeAnalytics: bool = True

class MeetingCapturedKafkaPayload(BaseModel):
    eventId: str = Field(default_factory=lambda: str(uuid.uuid4()))
    meetingId: str
    organizationId: str
    audioS3Uri: str
    captureMode: MeetingCaptureMode = MeetingCaptureMode.AUTOMATIC_BOT
    platform: MeetingPlatform
    publishedAt: str = Field(default_factory=lambda: datetime.utcnow().isoformat())
    audioDurationSeconds: int = 0
    attendeeCount: int = 0

class CaptureAbortedKafkaPayload(BaseModel):
    eventId: str = Field(default_factory=lambda: str(uuid.uuid4()))
    meetingId: str
    organizationId: str
    reason: str
    missingConsentCount: int
    abortedAt: str = Field(default_factory=lambda: datetime.utcnow().isoformat())
