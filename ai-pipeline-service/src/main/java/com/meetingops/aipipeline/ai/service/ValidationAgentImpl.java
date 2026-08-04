package com.meetingops.aipipeline.ai.service;

import com.meetingops.aipipeline.ai.prompt.PromptManager;
import com.meetingops.domain.enumeration.ItemStatus;
import com.meetingops.domain.model.ExtractedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the validation agent using LangChain4j AI Service.
 *
 * <p>Reviews each grounded item and rejects/flags items that are too vague
 * to act on (FR-4.1). Duplicate items are flagged to prevent creating
 * duplicate task-tracker entries (FR-4.2). Items requiring clarification
 * are routed to the human review queue (FR-4.3).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationAgentImpl implements ValidationAgent {

    private final PromptManager promptManager;
    // TODO: Inject LangChain4j ChatModel or AI Service interface

    @Override
    public List<ExtractedItem> validate(final List<ExtractedItem> items) {
        log.info("Validating {} extracted items", items.size());

        // TODO: Implement LLM-based validation using LangChain4j AI Service
        // 1. Build validation prompt for each item
        // 2. Call LLM to determine if item is actionable
        // 3. Flag vague items (e.g., 'someone should look into this')
        // 4. Flag items missing owner/deadline that should have them
        // 5. Update item status accordingly

        return items.stream().map(item -> {
            // Placeholder: In production, this calls the LLM
            log.debug("Validating item: {}", item.id());

            if (item.description() == null || item.description().isBlank()) {
                log.warn("Item {} has empty description, flagging as INVALID", item.id());
                return item.withStatus(ItemStatus.INVALID);
            }

            return item;
        }).collect(Collectors.toList());
    }
}
