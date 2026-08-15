-- ============================================================
-- MeetingOps Initial Schema
-- PostgreSQL with pgvector extension
-- ============================================================

-- Enable extensions
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

-- ============================================================
-- Table: organizations
-- ============================================================
CREATE TABLE organizations (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL UNIQUE,
    plan_type   VARCHAR(50) NOT NULL DEFAULT 'community',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- Table: users
-- ============================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    email           VARCHAR(255) NOT NULL,
    role            VARCHAR(50) NOT NULL,
    name            VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(organization_id, email)
);

CREATE INDEX idx_users_org ON users(organization_id);

-- ============================================================
-- Table: meetings
-- ============================================================
CREATE TABLE meetings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    team_id         UUID,
    title           VARCHAR(500) NOT NULL,
    meeting_date    TIMESTAMPTZ NOT NULL,
    attendees       JSONB NOT NULL DEFAULT '[]'::jsonb,
    transcript_ref  TEXT NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'INGESTED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_meetings_org ON meetings(organization_id);
CREATE INDEX idx_meetings_status ON meetings(status);
CREATE INDEX idx_meetings_org_status ON meetings(organization_id, status);

-- ============================================================
-- Table: extracted_items
-- ============================================================
CREATE TABLE extracted_items (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id          UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    type                VARCHAR(50) NOT NULL,
    description         TEXT NOT NULL,
    owner               VARCHAR(255),
    deadline            VARCHAR(255),
    supporting_excerpt  TEXT NOT NULL,
    confidence          DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    status              VARCHAR(50) NOT NULL DEFAULT 'EXTRACTED',
    prompt_version      VARCHAR(50),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_extracted_items_meeting ON extracted_items(meeting_id);
CREATE INDEX idx_extracted_items_type ON extracted_items(type);

-- ============================================================
-- Table: grounding_results
-- ============================================================
CREATE TABLE grounding_results (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    extracted_item_id   UUID NOT NULL REFERENCES extracted_items(id) ON DELETE CASCADE,
    classification      VARCHAR(50) NOT NULL,
    cited_item_id       UUID,
    cited_meeting_id    UUID REFERENCES meetings(id) ON DELETE SET NULL,
    rationale           TEXT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_grounding_item ON grounding_results(extracted_item_id);
CREATE INDEX idx_grounding_cited ON grounding_results(cited_meeting_id);

-- ============================================================
-- Table: draft_actions
-- ============================================================
CREATE TABLE draft_actions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    extracted_item_id   UUID NOT NULL REFERENCES extracted_items(id) ON DELETE CASCADE,
    meeting_id          UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    action_type         VARCHAR(50) NOT NULL,
    payload_json        TEXT NOT NULL,
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFTED',
    original_payload    TEXT NOT NULL,
    final_payload       TEXT,
    prompt_version      VARCHAR(50),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_draft_actions_meeting ON draft_actions(meeting_id);
CREATE INDEX idx_draft_actions_status ON draft_actions(status);
CREATE INDEX idx_draft_actions_item ON draft_actions(extracted_item_id);

-- ============================================================
-- Table: review_decisions (audit trail)
-- ============================================================
CREATE TABLE review_decisions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    draft_action_id     UUID NOT NULL REFERENCES draft_actions(id) ON DELETE CASCADE,
    reviewer_id         UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    action              VARCHAR(50) NOT NULL,
    original_payload    TEXT NOT NULL,
    final_payload       TEXT,
    reason              TEXT NOT NULL,
    timestamp           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_review_decisions_draft ON review_decisions(draft_action_id);
CREATE INDEX idx_review_decisions_reviewer ON review_decisions(reviewer_id);

-- ============================================================
-- Table: agent_traces
-- ============================================================
CREATE TABLE agent_traces (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id          UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    agent_role          VARCHAR(50) NOT NULL,
    step_index          INTEGER NOT NULL,
    tool_called         TEXT,
    tool_input          TEXT,
    tool_output         TEXT,
    reasoning_excerpt   TEXT,
    timestamp           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agent_traces_meeting ON agent_traces(meeting_id);
CREATE INDEX idx_agent_traces_meeting_time ON agent_traces(meeting_id, timestamp);

-- ============================================================
-- Table: meeting_embeddings (pgvector)
-- ============================================================
CREATE TABLE meeting_embeddings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    metadata_id     UUID NOT NULL,
    content         TEXT NOT NULL,
    embedding       vector,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_embeddings_org ON meeting_embeddings(organization_id);
CREATE INDEX idx_embeddings_metadata ON meeting_embeddings(organization_id, metadata_id);
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_am WHERE amname = 'ivfflat') THEN
        CREATE INDEX idx_embeddings_vector ON meeting_embeddings USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
    END IF;
END $$;
