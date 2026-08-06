-- ============================================================
-- MeetingOps V2 Schema Optimization
-- Optimized Indexing, Partitioning, Soft Delete & Optimistic Locking
-- ============================================================

-- 1. Add version (optimistic locking) and deleted_at (soft delete) to meetings
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ NULL;

-- Rename transcript_ref to transcript_s3_uri if needed for offloading clarity
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'meetings' AND column_name = 'transcript_ref'
    ) THEN
        ALTER TABLE meetings RENAME COLUMN transcript_ref TO transcript_s3_uri;
    END IF;
END $$;

-- 2. Add version (optimistic locking) to draft_actions
ALTER TABLE draft_actions ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1;

-- 3. Optimization: Partial Index for Active Review Queue (Filters out approved/rejected drafts)
CREATE INDEX IF NOT EXISTS idx_draft_actions_pending_review 
ON draft_actions (meeting_id, created_at DESC) 
WHERE status = 'DRAFTED';

-- 4. Optimization: Partial Index for Active Meetings (Filters out soft-deleted meetings)
CREATE INDEX IF NOT EXISTS idx_meetings_active_org 
ON meetings (organization_id, created_at DESC) 
WHERE deleted_at IS NULL;

-- 5. Optimization: Covering Index for Status Lookups (Avoids Heap Scans)
CREATE INDEX IF NOT EXISTS idx_meetings_status_lookup 
ON meetings (organization_id, status) 
INCLUDE (title, meeting_date);

-- 6. Upgrade pgvector embedding index to HNSW (Hierarchical Navigable Small World)
DROP INDEX IF EXISTS idx_embeddings_vector;

CREATE INDEX IF NOT EXISTS idx_embeddings_hnsw_cosine 
ON meeting_embeddings 
USING hnsw (embedding vector_cosine_ops) 
WITH (m = 16, ef_construction = 64);

-- 7. Range Partitioning Setup for high-volume agent_traces (Monthly Partitions)
-- Create partition table if agent_traces partition strategy is enabled
CREATE TABLE IF NOT EXISTS agent_traces_y2026m08 PARTITION OF agent_traces
    FOR VALUES FROM ('2026-08-01 00:00:00+00') TO ('2026-09-01 00:00:00+00');

CREATE TABLE IF NOT EXISTS agent_traces_y2026m09 PARTITION OF agent_traces
    FOR VALUES FROM ('2026-09-01 00:00:00+00') TO ('2026-10-01 00:00:00+00');
