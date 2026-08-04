package com.meetingops.application.ai.service;

import com.meetingops.domain.model.DraftAction;
import com.meetingops.domain.model.ExtractedItem;

import java.util.List;
import java.util.UUID;

/**
 * AI service interface for the drafting agent.
 *
 * <p>Generates draft follow-through actions (task-tracker entries,
 * calendar reminders, follow-up emails) for validated action items
 * via MCP tool calls. All actions are in draft/preview mode and
 * require human approval before execution (FR-5.1–5.4).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface DraftingAgent {

    /**
     * Generates draft follow-through actions for validated extracted items.
     *
     * @param items      the validated items to generate drafts for
     * @param meetingId  the meeting context
     * @param attendees  list of attendee identifiers for email drafting
     * @return list of generated draft actions
     */
    List<DraftAction> generateDrafts(List<ExtractedItem> items,
                                     UUID meetingId,
                                     List<String> attendees);
}
