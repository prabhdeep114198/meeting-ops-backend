package com.meetingops.domain.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for grounding result persistence in the domain layer.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface GroundingResultRepository {

    /**
     * Saves a grounding result.
     *
     * @param result the grounding result to save
     * @return the persisted result
     */
    GroundingResult save(GroundingResult result);

    /**
     * Finds a grounding result by its unique identifier.
     *
     * @param id the result identifier
     * @return the result if found
     */
    Optional<GroundingResult> findById(UUID id);

    /**
     * Finds the grounding result for a specific extracted item.
     *
     * @param extractedItemId the extracted item identifier
     * @return the grounding result if found
     */
    Optional<GroundingResult> findByExtractedItemId(UUID extractedItemId);

    /**
     * Saves a batch of grounding results.
     *
     * @param results the results to save
     * @return the persisted results
     */
    List<GroundingResult> saveAll(List<GroundingResult> results);
}
