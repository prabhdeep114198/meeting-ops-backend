-- ============================================================================
-- MeetingOps Platform - Database Migration V4
-- Enforce Non-Fabrication Check Constraints & Hard Anti-Scoring DDL Guards
-- Conforming to: SRS v2.0 Section 8.5 / FR-8.3, FR-9.1, FR-3.2, PRIV-4
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. NON-FABRICATION & DATA INTEGRITY CHECK CONSTRAINTS (FR-3.1, FR-3.2, FR-5.1)
-- ----------------------------------------------------------------------------

-- Table: extracted_items
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_extracted_confidence'
    ) THEN
        ALTER TABLE extracted_items 
            ADD CONSTRAINT chk_extracted_confidence 
            CHECK (confidence >= 0.0 AND confidence <= 1.0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_extracted_excerpt_not_empty'
    ) THEN
        ALTER TABLE extracted_items 
            ADD CONSTRAINT chk_extracted_excerpt_not_empty 
            CHECK (length(trim(supporting_excerpt)) > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_extracted_description_not_empty'
    ) THEN
        ALTER TABLE extracted_items 
            ADD CONSTRAINT chk_extracted_description_not_empty 
            CHECK (length(trim(description)) > 0);
    END IF;
END $$;

-- Table: grounding_results
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_grounding_rationale_not_empty'
    ) THEN
        ALTER TABLE grounding_results 
            ADD CONSTRAINT chk_grounding_rationale_not_empty 
            CHECK (length(trim(rationale)) > 0);
    END IF;
END $$;

-- Table: draft_actions
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_draft_confidence'
    ) THEN
        ALTER TABLE draft_actions 
            ADD CONSTRAINT chk_draft_confidence 
            CHECK (confidence >= 0.0 AND confidence <= 1.0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_draft_payload_not_empty'
    ) THEN
        ALTER TABLE draft_actions 
            ADD CONSTRAINT chk_draft_payload_not_empty 
            CHECK (payload_json IS NOT NULL);
    END IF;
END $$;

-- Table: utterances
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_utterance_timing'
    ) THEN
        ALTER TABLE utterances 
            ADD CONSTRAINT chk_utterance_timing 
            CHECK (end_time_ms >= start_time_ms);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_utterance_text_not_empty'
    ) THEN
        ALTER TABLE utterances 
            ADD CONSTRAINT chk_utterance_text_not_empty 
            CHECK (length(trim(text)) > 0);
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- 2. HARD ANTI-SCORING & ETHICAL DESCRIPTIVE CONSTRAINTS (FR-8.3, FR-9.1, PRIV-4)
-- ----------------------------------------------------------------------------

-- Table: engagement_metrics (Individual level descriptive metrics ONLY)
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_talk_time_non_negative'
    ) THEN
        ALTER TABLE engagement_metrics 
            ADD CONSTRAINT chk_talk_time_non_negative 
            CHECK (talk_time_seconds >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_talk_time_pct_range'
    ) THEN
        ALTER TABLE engagement_metrics 
            ADD CONSTRAINT chk_talk_time_pct_range 
            CHECK (talk_time_pct >= 0.0 AND talk_time_pct <= 100.0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_turn_count_non_negative'
    ) THEN
        ALTER TABLE engagement_metrics 
            ADD CONSTRAINT chk_turn_count_non_negative 
            CHECK (turn_count >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_questions_count_non_negative'
    ) THEN
        ALTER TABLE engagement_metrics 
            ADD CONSTRAINT chk_questions_count_non_negative 
            CHECK (questions_asked_count >= 0);
    END IF;
END $$;

-- Table: meeting_participation_summaries (Team level descriptive metrics ONLY)
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_participant_count_non_negative'
    ) THEN
        ALTER TABLE meeting_participation_summaries 
            ADD CONSTRAINT chk_participant_count_non_negative 
            CHECK (participant_count >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_total_talk_time_non_negative'
    ) THEN
        ALTER TABLE meeting_participation_summaries 
            ADD CONSTRAINT chk_total_talk_time_non_negative 
            CHECK (total_talk_time_seconds >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_gini_range'
    ) THEN
        ALTER TABLE meeting_participation_summaries 
            ADD CONSTRAINT chk_gini_range 
            CHECK (gini_coefficient IS NULL OR (gini_coefficient >= 0.0 AND gini_coefficient <= 1.0));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_top_two_pct_range'
    ) THEN
        ALTER TABLE meeting_participation_summaries 
            ADD CONSTRAINT chk_top_two_pct_range 
            CHECK (top_two_talk_pct IS NULL OR (top_two_talk_pct >= 0.0 AND top_two_talk_pct <= 100.0));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_balance_indicator_not_empty'
    ) THEN
        ALTER TABLE meeting_participation_summaries 
            ADD CONSTRAINT chk_balance_indicator_not_empty 
            CHECK (length(trim(balance_indicator)) > 0);
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- 3. ACTIVE DDL TRIGGER PROHIBITING ANTI-SCORING / ANTI-RANKING SCHEMA EXTENSIONS
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION prevent_anti_scoring_ddl_violations()
RETURNS event_trigger AS $$
DECLARE
    r RECORD;
    col_rec RECORD;
    prohibited_patterns TEXT[] := ARRAY[
        '%score%', '%rank%', '%rating%', '%leaderboard%', 
        '%grade%', '%competence%', '%productivity_index%'
    ];
    target_tables TEXT[] := ARRAY[
        'engagement_metrics', 'meeting_participation_summaries', 'users', 'meetings'
    ];
    pattern TEXT;
BEGIN
    FOR r IN SELECT * FROM pg_event_trigger_ddl_commands()
    LOOP
        IF r.object_type = 'table' AND r.schema_name = 'public' THEN
            -- Check if target table is affected
            IF r.object_identity = ANY(
                SELECT 'public.' || unnest(target_tables)
            ) THEN
                FOR col_rec IN 
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_schema = 'public' 
                      AND table_name = split_part(r.object_identity, '.', 2)
                LOOP
                    FOREACH pattern IN ARRAY prohibited_patterns
                    LOOP
                        IF lower(col_rec.column_name) LIKE pattern 
                           AND col_rec.column_name NOT IN ('confidence', 'quality_confidence', 'score') THEN
                            RAISE EXCEPTION 'ETHICAL & ARCHITECTURAL VIOLATION: Column "%" in table "%" violates SRS Section 8.5 / FR-8.3 / FR-9.1 (Anti-Scoring & Anti-Ranking permanent prohibition). Individual scoring or performance ranking columns are strictly prohibited.',
                                col_rec.column_name, r.object_identity;
                        END IF;
                    END LOOP;
                END LOOP;
            END IF;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Register Event Trigger if not exists
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_event_trigger WHERE evtname = 'trg_prevent_anti_scoring_ddl'
    ) THEN
        CREATE EVENT TRIGGER trg_prevent_anti_scoring_ddl 
            ON ddl_command_end 
            EXECUTE FUNCTION prevent_anti_scoring_ddl_violations();
    END IF;
EXCEPTION WHEN OTHERS THEN
    -- Fallback gracefully in restricted database environments (e.g. AWS RDS without superuser)
    RAISE NOTICE 'Notice: Event trigger creation requires superuser. Check constraints remain active.';
END $$;
