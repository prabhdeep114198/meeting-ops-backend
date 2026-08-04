package com.meetingops.aipipeline.ai.tool;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * LangChain4j tool definitions for the extraction and grounding agents.
 *
 * <p>These tools are annotated with {@link Tool} and automatically
 * registered by the LangChain4j Spring Boot starter when a
 * {@code ChatModel} is autowired. The extraction agent uses these
 * tools to return structured data about extracted meeting items.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingTools {

    /**
     * Extracts a structured action item from a transcript excerpt.
     *
     * <p>This tool is called by the LLM during the extraction phase
     * to return structured item data. The agent calls this once per
     * identified action item or decision.</p>
     *
     * @param description       the item description
     * @param owner             the identified owner (empty string if ambiguous)
     * @param deadline          the identified deadline (empty string if ambiguous)
     * @param supportingExcerpt the transcript excerpt supporting this extraction
     * @param itemType          "ACTION_ITEM" or "DECISION"
     * @return a JSON-formatted extraction result
     */
    @Tool("""
            Extract a structured action item or decision from a meeting transcript.
            Leave 'owner' and 'deadline' empty if they are not clearly stated in the excerpt.
            Never fabricate an owner or deadline that is not evidenced in the text.
            """)
    public String extractItem(final String description,
                              final String owner,
                              final String deadline,
                              final String supportingExcerpt,
                              final String itemType) {
        // TODO: Return structured extraction result
        log.debug("Tool call: extractItem - description={}, type={}", description, itemType);
        return """
                {
                  "id": "%s",
                  "description": "%s",
                  "owner": "%s",
                  "deadline": "%s",
                  "type": "%s",
                  "needsClarification": %s
                }
                """.formatted(
                UUID.randomUUID(),
                description,
                owner.isEmpty() ? "null" : owner,
                deadline.isEmpty() ? "null" : deadline,
                itemType,
                owner.isEmpty() || deadline.isEmpty()
        );
    }

    /**
     * Classifies an extracted item against historical meeting data.
     *
     * <p>Called by the grounding agent to classify a new item as
     * new, duplicate, continuation, or conflict with respect to
     * the retrieved historical context.</p>
     *
     * @param itemId        the extracted item identifier
     * @param classification the grounding classification
     * @param citedItemId   the cited prior item (empty if none)
     * @param rationale     explanation of the classification
     * @return a JSON-formatted grounding result
     */
    @Tool("""
            Classify an extracted meeting item against historical meeting data.
            Valid classifications: NEW, DUPLICATE, CONTINUATION, CONFLICT.
            Provide a rationale explaining why this classification was chosen.
            """)
    public String classifyGrounding(final String itemId,
                                    final String classification,
                                    final String citedItemId,
                                    final String rationale) {
        // TODO: Return structured grounding result
        log.debug("Tool call: classifyGrounding - item={}, class={}", itemId, classification);
        return """
                {
                  "itemId": "%s",
                  "classification": "%s",
                  "citedItemId": "%s",
                  "rationale": "%s"
                }
                """.formatted(itemId, classification,
                citedItemId.isEmpty() ? "null" : citedItemId,
                rationale);
    }

    /**
     * Validates an extracted item for completeness and actionability.
     *
     * @param itemId       the extracted item identifier
     * @param isValid      whether the item is actionable
     * @param flagReason   reason for flagging (empty if valid)
     * @return a JSON-formatted validation result
     */
    @Tool("""
            Validate whether an extracted meeting item is actionable.
            Flag items that are too vague (e.g., 'someone should look into this').
            Flag items where owner or deadline is missing when they should be present.
            """)
    public String validateItem(final String itemId,
                               final boolean isValid,
                               final String flagReason) {
        // TODO: Return structured validation result
        log.debug("Tool call: validateItem - item={}, valid={}", itemId, isValid);
        return """
                {
                  "itemId": "%s",
                  "valid": %s,
                  "flagReason": "%s"
                }
                """.formatted(itemId, isValid, flagReason);
    }
}
