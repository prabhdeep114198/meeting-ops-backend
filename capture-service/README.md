# MeetingOps Capture Service (Phase 2 — Week 3)

**FastAPI (Python 3.11) + Bot SDKs + S3/MinIO + Kafka** — Standalone microservice managing autonomous video conferencing bot lifecycles (Zoom, MS Teams, Google Meet), calendar webhook synchronization, pre-join consent verification state machine, and ephemeral encrypted audio capture.

---

## 🎯 SRS v2.0 Requirements Compliance

| Requirement ID | Specification Scope | Implementation Details |
| :--- | :--- | :--- |
| **FR-1.1** | Automatic Meeting Capture | Dispatches autonomous bot to join Zoom, Teams, or Meet calls. |
| **FR-1.3** | Pre-Join Consent Verification | State machine evaluates `NOTIFY_ONLY`, `MEETING_OPT_IN`, and `PARTICIPANT_OPT_IN` prior to call join. |
| **FR-1.4** | Visible/Audible Recording Notice | In-call bot badge (`"MeetingOps Recording Bot (AI Analysis)"`) & automated chat announcement. |
| **FR-1.5** | Participant Analytics Opt-Out | Allows participants to request exclusion from analytics without deleting transcript (`POST /api/v1/capture/opt-out`). |
| **FR-1.6** | Kafka Capture Decoupling | Publishes `meeting.captured` event to Kafka topic `meeting.captured` upon call completion. |
| **PRIV-1** | 3 Consent Modes | Full support for `NOTIFY_ONLY`, `MEETING_OPT_IN`, and `PARTICIPANT_OPT_IN`. |
| **PRIV-2** | Hard Abort (No Stealth Mode) | If consent is missing/declined, bot **aborts join immediately** and emits `capture.aborted`. Never degrades to partial/stealth recording. |
| **NFR-4.2** | Ephemeral Audio Retention | Audio streamed into S3/MinIO bucket with AES-256 encryption and a strict 24-hour auto-purge TTL. |

---

## 🚀 Quick Start

### 1. Run via Docker Compose (Recommended)
From `meeting-ops-backend/`:
```bash
docker-compose up --build capture-service minio
```

### 2. Run Locally (Python 3.11+)
```bash
cd capture-service
pip install -r requirements.txt
python main.py
```
Service runs on port `8085` (`http://localhost:8085`). Interactive Swagger UI available at `http://localhost:8085/docs`.

---

## 🔌 API Endpoints

### Capture & Consent Endpoints (`/api/v1`)
* `GET /api/v1/capture/health`: Health status and registered capabilities.
* `POST /api/v1/capture/manual-bot-join`: Dispatches a recording bot with pre-join consent checks.
* `GET /api/v1/capture/status/{meeting_id}`: Real-time bot status (`JOINING`, `RECORDING`, `COMPLETED`, `ABORTED_NO_CONSENT`).
* `POST /api/v1/capture/opt-out`: Records participant analytics opt-out (`FR-1.5`).
* `POST /api/v1/consent/evaluate`: Dry-run consent verification test.
* `PUT /api/v1/consent/policy`: Update organization consent mode.

### Calendar Integration Endpoints (`/api/v1/integrations`)
* `POST /api/v1/integrations/calendar`: Ingests Google Calendar push notifications and Microsoft Graph webhooks.

---

## 🧪 Automated Tests
Run unit and state machine tests:
```bash
pytest -v test_capture_service.py
```
Tests verify:
1. `NOTIFY_ONLY` policy allows join with mandatory in-call disclosure notice.
2. `MEETING_OPT_IN` allows join only when organizer opted in.
3. `PARTICIPANT_OPT_IN` enforces the **PRIV-2 Hard Abort**: if even one attendee declined or lacks verified consent, the bot aborts joining and emits `capture.aborted`.
