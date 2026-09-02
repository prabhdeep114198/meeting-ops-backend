import logging
import uuid
import time
import threading
from datetime import datetime
from typing import Dict, Optional, List
from config import settings
from models import (
    BotStatus,
    MeetingPlatform,
    MeetingCaptureMode,
    ConsentPolicy,
    ConsentStatus,
    ManualBotJoinRequest,
    BotJoinResponse,
    ConsentEvaluationResult,
    MeetingCapturedKafkaPayload,
    CaptureAbortedKafkaPayload,
    AttendeeInfo
)
from services.consent_engine import ConsentVerificationEngine
from services.audio_streamer import EphemeralAudioStreamer
from services.kafka_publisher import KafkaEventPublisher

logger = logging.getLogger("capture-service.bot_manager")

class ActiveCaptureJob:
    def __init__(self, job_id: str, meeting_id: str, org_id: str, platform: MeetingPlatform, meeting_url: str):
        self.job_id = job_id
        self.meeting_id = meeting_id
        self.org_id = org_id
        self.platform = platform
        self.meeting_url = meeting_url
        self.status = BotStatus.IDLE
        self.consent_evaluation: Optional[ConsentEvaluationResult] = None
        self.bot_display_name = f"{settings.BOT_NAME_PREFIX} (AI Analysis)"
        self.chat_notice_sent = False
        self.audio_s3_uri = None
        self.started_at = None
        self.ended_at = None
        self.error_message = None
        self.excluded_attendees = set()

class BotOrchestrationManager:
    """
    Manages Call-Join Lifecycles across Zoom, MS Teams, and Google Meet.
    Conforms to SRS v2.0 Section 3.1 & FR-1.1, FR-1.3, FR-1.4, FR-1.5, FR-1.6.
    """

    def __init__(self):
        self.consent_engine = ConsentVerificationEngine()
        self.audio_streamer = EphemeralAudioStreamer()
        self.kafka_publisher = KafkaEventPublisher()
        self.active_jobs: Dict[str, ActiveCaptureJob] = {}
        # In-memory mock store for organization consent policies (default to NOTIFY_ONLY)
        self.org_policies: Dict[str, ConsentPolicy] = {}

    def set_org_policy(self, org_id: str, policy: ConsentPolicy):
        self.org_policies[org_id] = policy

    def get_org_policy(self, org_id: str) -> ConsentPolicy:
        return self.org_policies.get(org_id, ConsentPolicy.NOTIFY_ONLY)

    def dispatch_bot_join(self, request: ManualBotJoinRequest) -> BotJoinResponse:
        """
        Dispatches a capture bot to join a video conference.
        Enforces pre-join consent check (FR-1.3, PRIV-1, PRIV-2) BEFORE connecting.
        """
        job_id = f"cap-{uuid.uuid4().hex[:8]}"
        meeting_id = f"m-{uuid.uuid4().hex[:8]}"
        org_id = request.organizationId or "org-default-001"

        job = ActiveCaptureJob(
            job_id=job_id,
            meeting_id=meeting_id,
            org_id=org_id,
            platform=request.platform,
            meeting_url=request.meetingUrl
        )
        self.active_jobs[meeting_id] = job

        policy = self.get_org_policy(org_id)
        job.status = BotStatus.CHECKING_CONSENT

        # Step 1: Pre-Join Consent State Machine Check (FR-1.3, PRIV-1, PRIV-2)
        consent_result = self.consent_engine.evaluate_pre_join_consent(
            policy=policy,
            organizer_email=request.organizerEmail,
            organizer_opt_in=request.organizerOptIn,
            attendees=request.attendees
        )
        job.consent_evaluation = consent_result

        # If consent check failed: HARD ABORT (PRIV-2)
        if not consent_result.allowed:
            job.status = BotStatus.ABORTED_NO_CONSENT
            job.error_message = consent_result.reason
            logger.error(f"Bot join aborted for meeting {meeting_id}: {consent_result.reason}")

            # Emit capture.aborted event to Kafka
            aborted_event = CaptureAbortedKafkaPayload(
                meetingId=meeting_id,
                organizationId=org_id,
                reason=consent_result.reason,
                missingConsentCount=len(consent_result.missingConsentEmails)
            )
            self.kafka_publisher.publish_capture_aborted(aborted_event)

            return BotJoinResponse(
                captureJobId=job_id,
                meetingId=meeting_id,
                status=job.status,
                platform=request.platform,
                meetingUrl=request.meetingUrl,
                consentEvaluation=consent_result,
                message=f"ABORTED: {consent_result.reason}"
            )

        # Step 2: Consent is valid! Launch Bot in background
        job.status = BotStatus.JOINING
        job.started_at = datetime.utcnow()
        logger.info(f"Consent verified. Launching bot '{job.bot_display_name}' into {request.platform} call: {request.meetingUrl}")

        # Start asynchronous call lifecycle runner
        threading.Thread(target=self._run_call_lifecycle, args=(job, request.attendees), daemon=True).start()

        return BotJoinResponse(
            captureJobId=job_id,
            meetingId=meeting_id,
            status=job.status,
            platform=request.platform,
            meetingUrl=request.meetingUrl,
            consentEvaluation=consent_result,
            message="Consent verified. Bot dispatched to conference call.",
            joinedAt=job.started_at
        )

    def _run_call_lifecycle(self, job: ActiveCaptureJob, attendees: List[AttendeeInfo]):
        """
        Simulates / executes the in-meeting bot join lifecycle:
        1. Joins call with visible badge (FR-1.4)
        2. Broadcasts chat notice (FR-1.4)
        3. Captures & streams audio to S3/MinIO (NFR-4.2)
        4. Finishes call & publishes meeting.captured (FR-1.6)
        """
        try:
            # Simulate platform join handshake
            time.sleep(1.0)
            job.status = BotStatus.RECORDING

            # FR-1.4: Visible / Audible notice broadcast
            job.chat_notice_sent = True
            logger.info(f"[In-Meeting Notice Broadcast] {job.bot_display_name}: {settings.BOT_CHAT_DISCLAIMER}")

            # NFR-4.2: Stream ephemeral audio chunks into S3/MinIO
            for chunk_idx in range(3):
                # 1 second simulated chunk
                fake_pcm = b"\x00\x01\x00\x02" * 4000
                self.audio_streamer.stream_audio_chunk(
                    meeting_id=job.meeting_id,
                    organization_id=job.org_id,
                    chunk_bytes=fake_pcm,
                    chunk_index=chunk_idx
                )
                time.sleep(0.5)

            # Finalize audio object in S3
            s3_uri = self.audio_streamer.finalize_and_assemble_audio(
                meeting_id=job.meeting_id,
                organization_id=job.org_id
            )
            job.audio_s3_uri = s3_uri
            job.ended_at = datetime.utcnow()
            job.status = BotStatus.COMPLETED

            # FR-1.6: Publish meeting.captured event to Kafka
            event_payload = MeetingCapturedKafkaPayload(
                meetingId=job.meeting_id,
                organizationId=job.org_id,
                audioS3Uri=s3_uri,
                captureMode=MeetingCaptureMode.AUTOMATIC_BOT,
                platform=job.platform,
                audioDurationSeconds=120,
                attendeeCount=len(attendees)
            )
            self.kafka_publisher.publish_meeting_captured(event_payload)
            logger.info(f"Capture completed for meeting {job.meeting_id}. Event published to Kafka.")

        except Exception as e:
            job.status = BotStatus.FAILED
            job.error_message = str(e)
            logger.error(f"Error during bot call lifecycle for {job.meeting_id}: {e}")

    def get_job_status(self, meeting_id: str) -> Optional[ActiveCaptureJob]:
        return self.active_jobs.get(meeting_id)

    def record_opt_out(self, meeting_id: str, participant_email: str) -> bool:
        """
        Participant Analytics Opt-Out (FR-1.5).
        """
        job = self.active_jobs.get(meeting_id)
        if job:
            job.excluded_attendees.add(participant_email)
            logger.info(f"Participant {participant_email} opted out of analytics for meeting {meeting_id}")
            return True
        return False
