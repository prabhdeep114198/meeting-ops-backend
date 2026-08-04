package com.meetingops.aipipeline.ai.service;

import com.meetingops.domain.model.ExtractedItem;

import java.util.List;

/**
 * AI service interface for the validation agent.
 *
 * <p>Reviews each grounded item and rejects/flags items that are too vague
 * to act on (FR-4.1). Duplicate items are flagged to prevent creating
 * duplicate task-tracker entries (FR-4.2). Items requiring clarification
 * are routed to the human review queue (FR-4.3).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface ValidationAgent {

    /**
     * Validates a batch of grounded extracted items.
     *
     * @param items the extracted items to validate
     * @return list of items with updated validation status
     */
    List<ExtractedItem> validate(List<ExtractedItem> items);
}
