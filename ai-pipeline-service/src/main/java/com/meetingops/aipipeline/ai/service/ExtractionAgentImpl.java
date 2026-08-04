package com.meetingops.aipipeline.ai.service;

import com.meetingops.aipipeline.ai.prompt.PromptManager;
import com.meetingops.domain.enumeration.ItemType;
import com.meetingops.domain.model.ExtractedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the extraction agent using LangChain4j AI Service.
 *
 * <p>Delegates to the LLM (via LangChain4j) to parse the transcript and
 * produce structured action items and decisions. Per FR-2.3, the agent
 * must NOT fabricate owners or deadlines not evidenced in the transcript.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionAgentImpl implements ExtractionAgent {

    private final PromptManager promptManager;
    // TODO: Inject LangChain4j ChatModel or AI Service interface
    // private final ChatModel chatModel;

    @Override
    public List<ExtractedItem> extract(final UUID meetingId,
                                       final String transcriptText) {
        log.info("Extracting items from meeting {} transcript ({} chars)",
                meetingId, transcriptText.length());

        // TODO: Implement LLM-based extraction using LangChain4j AI Service
        // 1. Load extraction prompt template
        // 2. Build chat request with system message + user message (transcript)
        // 3. Configure tools (MeetingTools.extractItem) for structured output
        // 4. Call LLM and parse structured extraction results
        // 5. Return list of ExtractedItem domain models

        String prompt = promptManager.resolvePrompt("extraction", Map.of(
                "transcript", transcriptText
        ));

        // Placeholder: In production, this calls the LLM via LangChain4j
        log.debug("Extraction prompt length: {} chars", prompt.length());

        return List.of();
    }
}
