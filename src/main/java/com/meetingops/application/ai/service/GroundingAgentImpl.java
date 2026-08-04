package com.meetingops.application.ai.service;

import com.meetingops.application.ai.prompt.PromptManager;
import com.meetingops.domain.enumeration.GroundingClassification;
import com.meetingops.domain.model.ExtractedItem;
import com.meetingops.domain.model.GroundingResult;
import com.meetingops.domain.model.VectorStorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the grounding agent using LangChain4j AI Service and RAG.
 *
 * <p>Queries a vector store of the organization's prior meeting history
 * to retrieve semantically related past items, then classifies each
 * extracted item as new, duplicate, continuation, or conflict.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroundingAgentImpl implements GroundingAgent {

    private final PromptManager promptManager;
    private final VectorStorePort vectorStore;
    // TODO: Inject LangChain4j ChatModel or AI Service interface

    @Override
    public List<GroundingResult> ground(final List<ExtractedItem> items,
                                        final UUID organizationId) {
        log.info("Grounding {} items for organization {}", items.size(), organizationId);

        List<GroundingResult> results = new ArrayList<>();

        for (ExtractedItem item : items) {
            // TODO: Implement RAG-based grounding
            // 1. Query vector store for similar historical items (tenant-isolated)
            // 2. Build grounding prompt with retrieved context
            // 3. Call LLM to classify (NEW/DUPLICATE/CONTINUATION/CONFLICT)
            // 4. Attach relevant historical citation

            List<VectorStorePort.EmbeddingSearchResult> similarItems =
                    vectorStore.searchSimilar(organizationId, item.description(), 5);

            log.debug("Found {} similar items for extraction {}", similarItems.size(), item.id());

            String prompt = promptManager.resolvePrompt("grounding", Map.of(
                    "itemDescription", item.description(),
                    "historicalContext", formatHistoricalContext(similarItems)
            ));

            // Placeholder: In production, this calls the LLM via LangChain4j
            results.add(GroundingResult.create(
                    item.id(),
                    GroundingClassification.NEW, // Placeholder
                    null,
                    null,
                    "Placeholder: LLM classification not yet implemented"
            ));
        }

        return results;
    }

    /**
     * Formats historical search results into a context string for the LLM.
     */
    private String formatHistoricalContext(
            final List<VectorStorePort.EmbeddingSearchResult> results) {
        return results.stream()
                .map(r -> String.format("- [score=%.2f] %s", r.score(), r.text()))
                .reduce(String::join)
                .orElse("No historical context found.");
    }
}
