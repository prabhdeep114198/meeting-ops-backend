package com.meetingops.domain.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for extracted item persistence in the domain layer.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface ExtractedItemRepository {

    /**
     * Saves an extracted item.
     *
     * @param item the item to save
     * @return the persisted item
     */
    ExtractedItem save(ExtractedItem item);

    /**
     * Finds an extracted item by its unique identifier.
     *
     * @param id the item identifier
     * @return the item if found
     */
    Optional<ExtractedItem> findById(UUID id);

    /**
     * Finds all extracted items for a given meeting.
     *
     * @param meetingId the meeting identifier
     * @return list of items for the meeting
     */
    List<ExtractedItem> findByMeetingId(UUID meetingId);

    /**
     * Saves a batch of extracted items.
     *
     * @param items the items to save
     * @return the persisted items
     */
    List<ExtractedItem> saveAll(List<ExtractedItem> items);
}
