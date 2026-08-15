-- ============================================================================
-- MeetingOps Platform - Database Migration V3
-- Consolidated Full Schema & Migration for SRS v2.0 Compliance
-- Architecture: PostgreSQL 15/16 + pgvector + Row Level Security (RLS)
-- Features: Multi-Tenancy, Consent Enforcement, Ephemeral Audio Retention,
--           Privacy-Controlled Engagement Analytics, Immutability & Audit Trails,
--           Vector RAG with HNSW, Diarized Utterances, Conversational Q&A
-- ============================================================================

-- 0. Extensions & Types
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DO $$ BEGIN
    BEGIN
        CREATE EXTENSION IF NOT EXISTS vector;
    EXCEPTION WHEN OTHERS THEN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vector') THEN
            CREATE DOMAIN vector AS double precision[];
        END IF;
    END;
END $$;

-- Enums
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'consent_policy_enum') THEN
        CREATE TYPE consent_policy_enum AS ENUM ('notify_only', 'meeting_opt_in', 'participant_opt_in');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'analytics_visibility_policy_enum') THEN
        CREATE TYPE analytics_visibility_policy_enum AS ENUM ('SELF_AND_FACILITATOR', 'SELF_ONLY', 'MANAGER_OVERRIDE_ENABLED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role_enum') THEN
        CREATE TYPE user_role_enum AS ENUM ('ORG_ADMIN', 'FACILITATOR', 'REVIEWER', 'PARTICIPANT', 'PLATFORM_ENGINEER');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'meeting_capture_mode_enum') THEN
        CREATE TYPE meeting_capture_mode_enum AS ENUM ('AUTO_BOT', 'MANUAL_UPLOAD', 'DIRECT_INTEGRATION');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'meeting_platform_enum') THEN
        CREATE TYPE meeting_platform_enum AS ENUM ('ZOOM', 'GOOGLE_MEET', 'MS_TEAMS', 'SLACK_HUDDLE', 'CUSTOM_AUDIO');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'meeting_consent_status_enum') THEN
        CREATE TYPE meeting_consent_status_enum AS ENUM ('PENDING', 'SATISFIED', 'BLOCKED_NO_CONSENT', 'NOT_APPLICABLE');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'meeting_status_enum') THEN
        CREATE TYPE meeting_status_enum AS ENUM (
            'SCHEDULED', 'JOINING', 'RECORDING', 'TRANSCRIBING', 
            'PROCESSING', 'PENDING_REVIEW', 'REVIEWED', 'COMPLETED', 
            'FAILED', 'CONSENT_ABORTED'
        );
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'consent_record_status_enum') THEN
        CREATE TYPE consent_record_status_enum AS ENUM ('PENDING', 'GRANTED', 'DECLINED', 'REVOKED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'consent_scope_enum') THEN
        CREATE TYPE consent_scope_enum AS ENUM ('RECORDING', 'TRANSCRIPTION', 'ANALYTICS', 'VOICE_MATCHING');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'extracted_item_type_enum') THEN
        CREATE TYPE extracted_item_type_enum AS ENUM ('ACTION_ITEM', 'DECISION');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'extracted_item_status_enum') THEN
        CREATE TYPE extracted_item_status_enum AS ENUM ('EXTRACTED', 'NEEDS_CLARIFICATION', 'GROUNDED', 'VALIDATED', 'DRAFTED', 'DISCARDED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'grounding_classification_enum') THEN
        CREATE TYPE grounding_classification_enum AS ENUM ('NEW', 'DUPLICATE', 'CONTINUATION', 'CONFLICT');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'draft_action_type_enum') THEN
        CREATE TYPE draft_action_type_enum AS ENUM ('CREATE_TASK', 'CALENDAR_REMINDER', 'DRAFT_EMAIL');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'draft_action_status_enum') THEN
        CREATE TYPE draft_action_status_enum AS ENUM ('DRAFTED', 'APPROVED', 'EDITED', 'REJECTED', 'EXECUTED', 'FAILED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'review_decision_action_enum') THEN
        CREATE TYPE review_decision_action_enum AS ENUM ('APPROVE', 'EDIT', 'REJECT', 'BULK_APPROVE');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'qa_scope_enum') THEN
        CREATE TYPE qa_scope_enum AS ENUM ('SINGLE_MEETING', 'TEAM_MEETINGS', 'ORG_HISTORY');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'engagement_access_type_enum') THEN
        CREATE TYPE engagement_access_type_enum AS ENUM ('VIEW_OWN', 'VIEW_FACILITATOR', 'VIEW_ADMIN_AUDIT');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'embedding_content_type_enum') THEN
        CREATE TYPE embedding_content_type_enum AS ENUM ('UTTERANCE', 'ACTION_ITEM', 'DECISION', 'TRANSCRIPT_CHUNK');
    END IF;
END $$;

-- ============================================================================
-- 1. UPGRADE EXISTING V1 / V2 TABLES
-- ============================================================================

-- Upgrade: organizations
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'organizations' AND column_name = 'plan_type'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'organizations' AND column_name = 'plan_tier'
    ) THEN
        ALTER TABLE organizations RENAME COLUMN plan_type TO plan_tier;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'organizations' AND column_name = 'plan_tier'
    ) THEN
        ALTER TABLE organizations ADD COLUMN plan_tier VARCHAR(50) NOT NULL DEFAULT 'community';
    END IF;
END $$;

ALTER TABLE organizations ADD COLUMN IF NOT EXISTS domain VARCHAR(255);
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS consent_policy consent_policy_enum NOT NULL DEFAULT 'notify_only';
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS analytics_visibility_policy analytics_visibility_policy_enum NOT NULL DEFAULT 'SELF_AND_FACILITATOR';
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS audio_retention_days INTEGER NOT NULL DEFAULT 0;
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS analytics_retention_days INTEGER NOT NULL DEFAULT 365;
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS encryption_key_arn TEXT;
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Upgrade: users
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);
UPDATE users SET full_name = COALESCE(name, 'User') WHERE full_name IS NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- Upgrade: meetings
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS organizer_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS facilitator_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS scheduled_start TIMESTAMPTZ;
UPDATE meetings SET scheduled_start = meeting_date WHERE scheduled_start IS NULL;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS scheduled_end TIMESTAMPTZ;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS actual_start TIMESTAMPTZ;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS actual_end TIMESTAMPTZ;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS duration_seconds INTEGER;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS capture_mode meeting_capture_mode_enum NOT NULL DEFAULT 'MANUAL_UPLOAD';
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS capture_platform meeting_platform_enum NOT NULL DEFAULT 'CUSTOM_AUDIO';
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS external_meeting_id VARCHAR(255);
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS consent_status meeting_consent_status_enum NOT NULL DEFAULT 'PENDING';
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS audio_s3_uri TEXT;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS audio_retention_until TIMESTAMPTZ;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS audio_deleted_at TIMESTAMPTZ;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS quality_confidence DOUBLE PRECISION;

-- Upgrade: extracted_items
ALTER TABLE extracted_items ADD COLUMN IF NOT EXISTS organization_id UUID;
UPDATE extracted_items ei SET organization_id = m.organization_id FROM meetings m WHERE ei.meeting_id = m.id AND ei.organization_id IS NULL;
ALTER TABLE extracted_items ADD COLUMN IF NOT EXISTS owner_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE extracted_items ADD COLUMN IF NOT EXISTS raw_owner_name VARCHAR(255);
ALTER TABLE extracted_items ADD COLUMN IF NOT EXISTS deadline_date DATE;
ALTER TABLE extracted_items ADD COLUMN IF NOT EXISTS raw_deadline_text VARCHAR(255);
ALTER TABLE extracted_items ADD COLUMN IF NOT EXISTS utterance_ref_ids UUID[] DEFAULT '{}';
ALTER TABLE extracted_items ADD COLUMN IF NOT EXISTS is_vague BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE extracted_items ADD COLUMN IF NOT EXISTS model_version VARCHAR(100);

-- Upgrade: grounding_results
ALTER TABLE grounding_results ADD COLUMN IF NOT EXISTS organization_id UUID;
UPDATE grounding_results gr SET organization_id = ei.organization_id FROM extracted_items ei WHERE gr.extracted_item_id = ei.id AND gr.organization_id IS NULL;
ALTER TABLE grounding_results ADD COLUMN IF NOT EXISTS similarity_score DOUBLE PRECISION;
ALTER TABLE grounding_results ADD COLUMN IF NOT EXISTS model_version VARCHAR(100);

-- Upgrade: draft_actions
ALTER TABLE draft_actions ADD COLUMN IF NOT EXISTS organization_id UUID;
UPDATE draft_actions da SET organization_id = m.organization_id FROM meetings m WHERE da.meeting_id = m.id AND da.organization_id IS NULL;
ALTER TABLE draft_actions ADD COLUMN IF NOT EXISTS classification grounding_classification_enum NOT NULL DEFAULT 'NEW';
ALTER TABLE draft_actions ADD COLUMN IF NOT EXISTS confidence DOUBLE PRECISION NOT NULL DEFAULT 0.0;
ALTER TABLE draft_actions ADD COLUMN IF NOT EXISTS mcp_tool_name VARCHAR(100) DEFAULT 'create_task';
ALTER TABLE draft_actions ADD COLUMN IF NOT EXISTS external_ref_id VARCHAR(255);
ALTER TABLE draft_actions ADD COLUMN IF NOT EXISTS executed_at TIMESTAMPTZ;
ALTER TABLE draft_actions ADD COLUMN IF NOT EXISTS model_version VARCHAR(100);

-- Upgrade: review_decisions
ALTER TABLE review_decisions ADD COLUMN IF NOT EXISTS organization_id UUID;
UPDATE review_decisions rd SET organization_id = da.organization_id FROM draft_actions da WHERE rd.draft_action_id = da.id AND rd.organization_id IS NULL;
ALTER TABLE review_decisions ADD COLUMN IF NOT EXISTS decision_metadata JSONB DEFAULT '{}'::jsonb;

-- Upgrade: meeting_embeddings
ALTER TABLE meeting_embeddings ADD COLUMN IF NOT EXISTS meeting_id UUID;
ALTER TABLE meeting_embeddings ADD COLUMN IF NOT EXISTS content_type embedding_content_type_enum DEFAULT 'ACTION_ITEM';
ALTER TABLE meeting_embeddings ADD COLUMN IF NOT EXISTS chunk_index INTEGER NOT NULL DEFAULT 0;

-- Upgrade: agent_traces
ALTER TABLE agent_traces ADD COLUMN IF NOT EXISTS organization_id UUID;
UPDATE agent_traces at SET organization_id = m.organization_id FROM meetings m WHERE at.meeting_id = m.id AND at.organization_id IS NULL;
ALTER TABLE agent_traces ADD COLUMN IF NOT EXISTS trace_id VARCHAR(64);
ALTER TABLE agent_traces ADD COLUMN IF NOT EXISTS span_id VARCHAR(32);
ALTER TABLE agent_traces ADD COLUMN IF NOT EXISTS parent_span_id VARCHAR(32);
ALTER TABLE agent_traces ADD COLUMN IF NOT EXISTS latency_ms INTEGER;
ALTER TABLE agent_traces ADD COLUMN IF NOT EXISTS prompt_tokens INTEGER;
ALTER TABLE agent_traces ADD COLUMN IF NOT EXISTS completion_tokens INTEGER;

-- ============================================================================
-- 2. NEW SRS V2.0 TABLES
-- ============================================================================

-- Table: teams
CREATE TABLE IF NOT EXISTS teams (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(organization_id, name)
);

CREATE INDEX IF NOT EXISTS idx_teams_org ON teams(organization_id);

-- Table: user_teams
CREATE TABLE IF NOT EXISTS user_teams (
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id             UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    role_in_team        VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, team_id)
);

CREATE INDEX IF NOT EXISTS idx_user_teams_team ON user_teams(team_id);

-- Table: meeting_attendees
CREATE TABLE IF NOT EXISTS meeting_attendees (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    meeting_id          UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    user_id             UUID REFERENCES users(id) ON DELETE SET NULL,
    email               VARCHAR(255) NOT NULL,
    display_name        VARCHAR(255) NOT NULL,
    platform_user_id    VARCHAR(255),
    is_facilitator      BOOLEAN NOT NULL DEFAULT FALSE,
    is_external         BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at           TIMESTAMPTZ,
    left_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(meeting_id, email)
);

CREATE INDEX IF NOT EXISTS idx_attendees_meeting ON meeting_attendees(meeting_id);
CREATE INDEX IF NOT EXISTS idx_attendees_user ON meeting_attendees(user_id);

-- Table: consent_records
CREATE TABLE IF NOT EXISTS consent_records (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    meeting_id          UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    participant_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    participant_email   VARCHAR(255) NOT NULL,
    consent_mode        consent_policy_enum NOT NULL,
    consent_scope       consent_scope_enum NOT NULL,
    status              consent_record_status_enum NOT NULL,
    evidence_token      TEXT,
    ip_address          INET,
    user_agent          TEXT,
    timestamp           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_consent_meeting ON consent_records(meeting_id);
CREATE INDEX IF NOT EXISTS idx_consent_participant ON consent_records(organization_id, participant_email);
CREATE INDEX IF NOT EXISTS idx_consent_status ON consent_records(meeting_id, status);

-- Table: participant_exclusions (FR-1.5)
CREATE TABLE IF NOT EXISTS participant_exclusions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    meeting_id          UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    participant_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    participant_email   VARCHAR(255) NOT NULL,
    exclude_analytics   BOOLEAN NOT NULL DEFAULT TRUE,
    requested_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(meeting_id, participant_email)
);

CREATE INDEX IF NOT EXISTS idx_exclusions_meeting ON participant_exclusions(meeting_id);

-- Table: transcripts
CREATE TABLE IF NOT EXISTS transcripts (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    meeting_id          UUID NOT NULL UNIQUE REFERENCES meetings(id) ON DELETE CASCADE,
    raw_text            TEXT NOT NULL,
    diarization_data    JSONB NOT NULL DEFAULT '{}'::jsonb,
    quality_confidence  DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    language_code       VARCHAR(10) DEFAULT 'en',
    word_count          INTEGER NOT NULL DEFAULT 0,
    model_version       VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transcripts_org ON transcripts(organization_id);

-- Table: utterances
CREATE TABLE IF NOT EXISTS utterances (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    transcript_id       UUID NOT NULL REFERENCES transcripts(id) ON DELETE CASCADE,
    meeting_id          UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    speaker_id          UUID REFERENCES users(id) ON DELETE SET NULL,
    speaker_label       VARCHAR(100) NOT NULL,
    sequence_number     INTEGER NOT NULL,
    start_time_ms       BIGINT NOT NULL,
    end_time_ms         BIGINT NOT NULL,
    text                TEXT NOT NULL,
    confidence          DOUBLE PRECISION DEFAULT 1.0,
    is_question         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_utterance_timing CHECK (end_time_ms >= start_time_ms)
);

CREATE INDEX IF NOT EXISTS idx_utterances_transcript_seq ON utterances(transcript_id, sequence_number ASC);
CREATE INDEX IF NOT EXISTS idx_utterances_meeting ON utterances(meeting_id);
CREATE INDEX IF NOT EXISTS idx_utterances_speaker ON utterances(speaker_id);
CREATE INDEX IF NOT EXISTS idx_utterances_timing ON utterances(meeting_id, start_time_ms, end_time_ms);

-- Table: qa_sessions
CREATE TABLE IF NOT EXISTS qa_sessions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    meeting_scope       qa_scope_enum NOT NULL DEFAULT 'SINGLE_MEETING',
    target_meeting_id   UUID REFERENCES meetings(id) ON DELETE CASCADE,
    target_team_id      UUID REFERENCES teams(id) ON DELETE CASCADE,
    title               VARCHAR(255) NOT NULL DEFAULT 'New Conversation',
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qa_sessions_user ON qa_sessions(user_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_qa_sessions_org ON qa_sessions(organization_id);

-- Table: qa_queries
CREATE TABLE IF NOT EXISTS qa_queries (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    session_id          UUID NOT NULL REFERENCES qa_sessions(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_text       TEXT NOT NULL,
    answer_text         TEXT NOT NULL,
    cited_excerpts      JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_grounded         BOOLEAN NOT NULL DEFAULT TRUE,
    latency_ms          INTEGER,
    prompt_tokens       INTEGER,
    completion_tokens   INTEGER,
    prompt_version      VARCHAR(100),
    model_version       VARCHAR(100),
    timestamp           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qa_queries_session ON qa_queries(session_id, timestamp ASC);
CREATE INDEX IF NOT EXISTS idx_qa_queries_org_audit ON qa_queries(organization_id, timestamp DESC);

-- Table: meeting_participation_summaries (Team/Meeting-level descriptive aggregate)
CREATE TABLE IF NOT EXISTS meeting_participation_summaries (
    id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id             UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    meeting_id                  UUID NOT NULL UNIQUE REFERENCES meetings(id) ON DELETE CASCADE,
    participant_count           INTEGER NOT NULL,
    total_talk_time_seconds     INTEGER NOT NULL,
    balance_indicator           VARCHAR(255) NOT NULL,
    gini_coefficient            DOUBLE PRECISION,
    top_two_talk_pct            DOUBLE PRECISION,
    descriptive_notes           TEXT,
    computed_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_part_summary_org ON meeting_participation_summaries(organization_id);

-- Table: engagement_metrics (Individual-level, STRICT ETHICAL CONSTRAINTS)
CREATE TABLE IF NOT EXISTS engagement_metrics (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    meeting_id              UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    speaker_id              UUID REFERENCES users(id) ON DELETE CASCADE,
    speaker_label           VARCHAR(100) NOT NULL,
    talk_time_seconds       INTEGER NOT NULL DEFAULT 0,
    talk_time_pct           DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    turn_count              INTEGER NOT NULL DEFAULT 0,
    questions_asked_count   INTEGER NOT NULL DEFAULT 0,
    model_version           VARCHAR(100),
    computed_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_talk_time_positive CHECK (talk_time_seconds >= 0),
    CONSTRAINT chk_talk_time_pct_range CHECK (talk_time_pct >= 0.0 AND talk_time_pct <= 100.0),
    CONSTRAINT chk_turns_positive CHECK (turn_count >= 0),
    CONSTRAINT chk_questions_positive CHECK (questions_asked_count >= 0),
    UNIQUE(meeting_id, speaker_label)
);

CREATE INDEX IF NOT EXISTS idx_engagement_meeting ON engagement_metrics(meeting_id);
CREATE INDEX IF NOT EXISTS idx_engagement_speaker ON engagement_metrics(speaker_id);
CREATE INDEX IF NOT EXISTS idx_engagement_org ON engagement_metrics(organization_id);

-- Table: engagement_access_logs (IMMUTABLE AUDIT LOG)
CREATE TABLE IF NOT EXISTS engagement_access_logs (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    meeting_id              UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    engagement_metric_id    UUID REFERENCES engagement_metrics(id) ON DELETE CASCADE,
    target_speaker_id       UUID REFERENCES users(id) ON DELETE SET NULL,
    viewer_id               UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_type             engagement_access_type_enum NOT NULL,
    ip_address              INET,
    user_agent              TEXT,
    timestamp               TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_eng_access_org_time ON engagement_access_logs(organization_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_eng_access_viewer ON engagement_access_logs(viewer_id);
CREATE INDEX IF NOT EXISTS idx_eng_access_target ON engagement_access_logs(target_speaker_id);

-- Table: voice_biometric_profiles (Heightened Protection Store for Diarization Voiceprints)
CREATE TABLE IF NOT EXISTS voice_biometric_profiles (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    voiceprint_hash     TEXT NOT NULL,
    voice_embedding     vector,
    consent_verified    BOOLEAN NOT NULL DEFAULT FALSE,
    retention_until     TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_voice_profiles_org ON voice_biometric_profiles(organization_id);

-- ============================================================================
-- 3. INDEXES & PERFORMANCE OPTIMIZATIONS
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_draft_actions_org_status ON draft_actions(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_extracted_items_org_type ON extracted_items(organization_id, type);
CREATE INDEX IF NOT EXISTS idx_extracted_items_status ON extracted_items(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_grounding_org_classification ON grounding_results(organization_id, classification);
CREATE INDEX IF NOT EXISTS idx_review_decisions_org_time ON review_decisions(organization_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_embeddings_org_type ON meeting_embeddings(organization_id, content_type);
CREATE INDEX IF NOT EXISTS idx_agent_traces_org_time ON agent_traces(organization_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_agent_traces_agent_role ON agent_traces(organization_id, agent_role);

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_am WHERE amname = 'hnsw') THEN
        CREATE INDEX IF NOT EXISTS idx_embeddings_hnsw_cosine 
        ON meeting_embeddings 
        USING hnsw (embedding vector_cosine_ops) 
        WITH (m = 16, ef_construction = 64);
    END IF;
END $$;

-- ============================================================================
-- 4. IMMUTABILITY RULES & AUDIT PROTECTION TRIGGERS
-- ============================================================================

CREATE OR REPLACE FUNCTION prevent_audit_log_tampering()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'SECURITY VIOLATION: Records in table % are immutable and cannot be updated or deleted.', TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_immutable_review_decisions ON review_decisions;
CREATE TRIGGER trg_immutable_review_decisions
    BEFORE UPDATE OR DELETE ON review_decisions
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_tampering();

DROP TRIGGER IF EXISTS trg_immutable_consent_records ON consent_records;
CREATE TRIGGER trg_immutable_consent_records
    BEFORE UPDATE OR DELETE ON consent_records
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_tampering();

DROP TRIGGER IF EXISTS trg_immutable_engagement_access_logs ON engagement_access_logs;
CREATE TRIGGER trg_immutable_engagement_access_logs
    BEFORE UPDATE OR DELETE ON engagement_access_logs
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_tampering();

DROP TRIGGER IF EXISTS trg_immutable_agent_traces ON agent_traces;
CREATE TRIGGER trg_immutable_agent_traces
    BEFORE UPDATE OR DELETE ON agent_traces
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_tampering();

-- ============================================================================
-- 5. DATA PRIVACY ACCESS LAYER: SECURE VIEWS (FR-8.4, PRIV-4)
-- ============================================================================

CREATE OR REPLACE FUNCTION is_meeting_facilitator(p_meeting_id UUID, p_user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM meetings m
        WHERE m.id = p_meeting_id AND m.facilitator_id = p_user_id
    ) OR EXISTS (
        SELECT 1 FROM meeting_attendees a
        WHERE a.meeting_id = p_meeting_id AND a.user_id = p_user_id AND a.is_facilitator = TRUE
    );
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

CREATE OR REPLACE VIEW v_secure_individual_engagement AS
SELECT 
    em.id,
    em.organization_id,
    em.meeting_id,
    em.speaker_id,
    em.speaker_label,
    em.talk_time_seconds,
    em.talk_time_pct,
    em.turn_count,
    em.questions_asked_count,
    em.computed_at
FROM engagement_metrics em
JOIN meetings m ON em.meeting_id = m.id
WHERE (
    em.speaker_id = NULLIF(current_setting('app.current_user_id', true), '')::uuid
    OR
    is_meeting_facilitator(em.meeting_id, NULLIF(current_setting('app.current_user_id', true), '')::uuid) = TRUE
    OR
    (current_setting('app.current_user_role', true) = 'ORG_ADMIN' AND current_setting('app.audit_mode_enabled', true) = 'true')
);

-- ============================================================================
-- 6. ROW LEVEL SECURITY (RLS) FOR MULTI-TENANCY ISOLATION (PRIV-6, FR-10.2)
-- ============================================================================

ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
ALTER TABLE teams ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE meetings ENABLE ROW LEVEL SECURITY;
ALTER TABLE meeting_attendees ENABLE ROW LEVEL SECURITY;
ALTER TABLE consent_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE participant_exclusions ENABLE ROW LEVEL SECURITY;
ALTER TABLE transcripts ENABLE ROW LEVEL SECURITY;
ALTER TABLE utterances ENABLE ROW LEVEL SECURITY;
ALTER TABLE extracted_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE grounding_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE draft_actions ENABLE ROW LEVEL SECURITY;
ALTER TABLE review_decisions ENABLE ROW LEVEL SECURITY;
ALTER TABLE qa_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE qa_queries ENABLE ROW LEVEL SECURITY;
ALTER TABLE meeting_participation_summaries ENABLE ROW LEVEL SECURITY;
ALTER TABLE engagement_metrics ENABLE ROW LEVEL SECURITY;
ALTER TABLE engagement_access_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE meeting_embeddings ENABLE ROW LEVEL SECURITY;
ALTER TABLE voice_biometric_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE agent_traces ENABLE ROW LEVEL SECURITY;

DO $$ 
DECLARE
    tbl text;
    tenant_tables text[] := ARRAY[
        'teams', 'users', 'meetings', 'meeting_attendees', 'consent_records',
        'participant_exclusions', 'transcripts', 'utterances', 'extracted_items',
        'grounding_results', 'draft_actions', 'review_decisions', 'qa_sessions',
        'qa_queries', 'meeting_participation_summaries', 'engagement_metrics',
        'engagement_access_logs', 'meeting_embeddings', 'voice_biometric_profiles',
        'agent_traces'
    ];
BEGIN
    FOREACH tbl IN ARRAY tenant_tables LOOP
        EXECUTE format('DROP POLICY IF EXISTS %I_tenant_isolation_policy ON %I', tbl, tbl);
        EXECUTE format('
            CREATE POLICY %I_tenant_isolation_policy ON %I
            FOR ALL
            USING (
                organization_id = NULLIF(current_setting(''app.current_org_id'', true), '''')::uuid
                OR current_setting(''app.bypass_rls'', true) = ''true''
            )
            WITH CHECK (
                organization_id = NULLIF(current_setting(''app.current_org_id'', true), '''')::uuid
                OR current_setting(''app.bypass_rls'', true) = ''true''
            )
        ', tbl, tbl);
    END LOOP;
END $$;

DROP POLICY IF EXISTS organizations_tenant_isolation_policy ON organizations;
CREATE POLICY organizations_tenant_isolation_policy ON organizations
FOR ALL
USING (
    id = NULLIF(current_setting('app.current_org_id', true), '')::uuid
    OR current_setting('app.bypass_rls', true) = 'true'
);

-- ============================================================================
-- 7. RETENTION & PURGE PROCEDURES
-- ============================================================================

CREATE OR REPLACE FUNCTION purge_expired_raw_audio()
RETURNS INTEGER AS $$
DECLARE
    purged_count INTEGER;
BEGIN
    UPDATE meetings
    SET 
        audio_s3_uri = NULL,
        audio_deleted_at = NOW()
    WHERE 
        audio_s3_uri IS NOT NULL 
        AND audio_deleted_at IS NULL
        AND audio_retention_until IS NOT NULL 
        AND audio_retention_until <= NOW();
        
    GET DIAGNOSTICS purged_count = ROW_COUNT;
    RETURN purged_count;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION purge_expired_voice_biometrics()
RETURNS INTEGER AS $$
DECLARE
    purged_count INTEGER;
BEGIN
    DELETE FROM voice_biometric_profiles
    WHERE retention_until <= NOW();
    
    GET DIAGNOSTICS purged_count = ROW_COUNT;
    RETURN purged_count;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
