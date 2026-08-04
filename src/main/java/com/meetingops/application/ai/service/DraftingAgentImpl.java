package com.meetingops.application.ai.service;

import com.meetingops.application.ai.prompt.PromptManager;
import com.meetingops.domain.enumeration.DraftActionType;
import com.meetingops.domain.enumeration.ItemStatus;
import com.meetingops.domain.enumeration.ItemType;
import com.meetingops.domain.model.DraftAction;
import com.meetingops.domain.model.ExtractedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the drafting agent using LangChain4j AI Service and MCP tools.
 *
 * <p>Generates draft follow-through actions for validated action items:
 * <ul>
 *   <li>Action items with assigned owners → task-tracker entries</li>
 *   <li>Action items with deadlines → calendar reminders</li>
 *   <li>Decisions affecting multiple people → follow-up emails</li>
 * </ul>
 * </p>
 *
 * <p>All actions are in DRAFTED state and require human approval
 * before execution (FR-5.1–5.4, FR-6.3).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DraftingAgentImpl implements DraftingAgent {

    private final PromptManager promptManager;
    // TODO: Inject LangChain4j ChatModel with MCP tools (TaskTrackerTools, CalendarTools, EmailTools)

    @Override
    public List<DraftAction> generateDrafts(final List<ExtractedItem> items,
                                            final UUID meetingId,
                                            final List<String> attendees) {
        log.info("Generating draft actions for {} validated items in meeting {}",
                items.size(), meetingId);

        // Filter to only actionable, validated items
        List<ExtractedItem> actionableItems = items.stream()
                .filter(item -> item.status() == ItemStatus.VALIDATED)
                .collect(Collectors.toList());

        List<DraftAction> drafts = new ArrayList<>();

        for (ExtractedItem item : actionableItems) {
            if (item.type() == ItemType.ACTION_ITEM) {
                // Generate task-tracker entry if owner is assigned
                if (item.owner() != null && !item.owner().isBlank()) {
                    drafts.add(generateTaskDraft(item, meetingId));
                }
                // Generate calendar reminder if deadline exists
                if (item.deadline() != null && !item.deadline().isBlank()) {
                    drafts.add(generateCalendarDraft(item, meetingId));
                }
            } else if (item.type() == ItemType.DECISION) {
                // Generate follow-up email for decisions
                drafts.add(generateEmailDraft(item, meetingId, attendees));
            }
        }

        log.info("Generated {} draft actions for meeting {}", drafts.size(), meetingId);
        return drafts;
    }

    /**
     * Generates a task-tracker draft action.
     */
    private DraftAction generateTaskDraft(final ExtractedItem item, final UUID meetingId) {
        // TODO: Call MCP create-task tool via LangChain4j AI Service
        String payload = """
                {
                  "title": "%s",
                  "description": "%s",
                  "assignee": "%s",
                  "dueDate": "%s",
                  "provider": "mock"
                }
                """.formatted(item.description(), item.description(),
                item.owner() != null ? item.owner() : "",
                item.deadline() != null ? item.deadline() : "");

        return DraftAction.create(
                item.id(),
                meetingId,
                DraftActionType.TASK,
                payload,
                payload
        );
    }

    /**
     * Generates a calendar reminder draft action.
     */
    private DraftAction generateCalendarDraft(final ExtractedItem item, final UUID meetingId) {
        // TODO: Call MCP calendar-reminder tool via LangChain4j AI Service
        String payload = """
                {
                  "title": "Reminder: %s",
                  "description": "%s",
                  "attendee": "%s",
                  "reminderDate": "%s",
                  "provider": "mock"
                }
                """.formatted(item.description(), item.description(),
                item.owner() != null ? item.owner() : "",
                item.deadline() != null ? item.deadline() : "");

        return DraftAction.create(
                item.id(),
                meetingId,
                DraftActionType.CALENDAR_REMINDER,
                payload,
                payload
        );
    }

    /**
     * Generates an email draft action.
     */
    private DraftAction generateEmailDraft(final ExtractedItem item,
                                           final UUID meetingId,
                                           final List<String> attendees) {
        // TODO: Call MCP draft-email tool via LangChain4j AI Service
        String recipients = attendees != null
                ? String.join(",", attendees)
                : "";

        String payload = """
                {
                  "subject": "Meeting Decision: %s",
                  "body": "The following decision was made: %s",
                  "recipients": "%s",
                  "provider": "mock"
                }
                """.formatted(item.description(), item.description(), recipients);

        return DraftAction.create(
                item.id(),
                meetingId,
                DraftActionType.EMAIL,
                payload,
                payload
        );
    }
}
