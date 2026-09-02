import logging
from typing import List, Dict, Optional, Tuple
from models import ConsentPolicy, ConsentStatus, AttendeeInfo, ConsentEvaluationResult
from config import settings

logger = logging.getLogger("capture-service.consent")

class ConsentVerificationEngine:
    """
    Pre-Join Consent Verification State Machine.
    Conforms to SRS v2.0 Section 8.2 & FR-1.3, PRIV-1, PRIV-2.

    Core Rule (PRIV-2): The system shall not silently degrade a declined-consent scenario
    into partial capture; if required consent is not obtained, capture shall abort completely.
    """

    def __init__(self, db_conn=None):
        self.db_conn = db_conn

    def evaluate_pre_join_consent(
        self,
        policy: ConsentPolicy,
        organizer_email: str,
        organizer_opt_in: bool,
        attendees: List[AttendeeInfo],
        consent_records_override: Optional[Dict[str, ConsentStatus]] = None
    ) -> ConsentEvaluationResult:
        """
        Executes pre-join consent validation before any capture bot joins the call.
        """
        logger.info(f"Evaluating pre-join consent under policy: {policy} for organizer: {organizer_email}")

        # Mode 1: NOTIFY_ONLY (FR-1.3, PRIV-1)
        # Permitted to join, but in-meeting audible/visible notice is strictly mandatory.
        if policy == ConsentPolicy.NOTIFY_ONLY:
            return ConsentEvaluationResult(
                allowed=True,
                consentPolicy=policy,
                reason="Policy is NOTIFY_ONLY: Bot permitted to join. Visible badge and chat broadcast required.",
                missingConsentEmails=[]
            )

        # Mode 2: MEETING_OPT_IN (FR-1.3, PRIV-1)
        # Requires explicit organizer opt-in flag.
        if policy == ConsentPolicy.MEETING_OPT_IN:
            if organizer_opt_in:
                return ConsentEvaluationResult(
                    allowed=True,
                    consentPolicy=policy,
                    reason="Policy is MEETING_OPT_IN: Organizer has explicitly opted in.",
                    missingConsentEmails=[]
                )
            else:
                logger.warning("MEETING_OPT_IN check failed: Organizer has not opted in.")
                return ConsentEvaluationResult(
                    allowed=False,
                    consentPolicy=policy,
                    reason="Policy is MEETING_OPT_IN: Meeting organizer did not opt in. Aborting bot join.",
                    missingConsentEmails=[organizer_email]
                )

        # Mode 3: PARTICIPANT_OPT_IN (FR-1.3, PRIV-1, PRIV-2)
        # Every attendee must have explicit GRANTED consent.
        # If even one attendee has DECLINED or PENDING consent, join MUST ABORT (PRIV-2).
        if policy == ConsentPolicy.PARTICIPANT_OPT_IN:
            missing_or_declined: List[str] = []

            for attendee in attendees:
                # Check status from override map or attendee record
                status = None
                if consent_records_override and attendee.email in consent_records_override:
                    status = consent_records_override[attendee.email]
                else:
                    status = attendee.consentStatus

                if status != ConsentStatus.GRANTED:
                    missing_or_declined.append(attendee.email)

            if len(missing_or_declined) == 0:
                return ConsentEvaluationResult(
                    allowed=True,
                    consentPolicy=policy,
                    reason="Policy is PARTICIPANT_OPT_IN: All attendees have verified GRANTED consent records.",
                    missingConsentEmails=[]
                )
            else:
                logger.error(
                    f"PARTICIPANT_OPT_IN failed (PRIV-2): {len(missing_or_declined)} attendees lack verified consent: {missing_or_declined}. "
                    "Aborting bot join. Never degrading to partial capture."
                )
                return ConsentEvaluationResult(
                    allowed=False,
                    consentPolicy=policy,
                    reason=f"Policy is PARTICIPANT_OPT_IN: Missing or declined consent for: {', '.join(missing_or_declined)}. Bot join aborted.",
                    missingConsentEmails=missing_or_declined
                )

        # Default safe fallback
        return ConsentEvaluationResult(
            allowed=False,
            consentPolicy=policy,
            reason=f"Unknown consent policy '{policy}'. Safe abort enforced.",
            missingConsentEmails=[]
        )
