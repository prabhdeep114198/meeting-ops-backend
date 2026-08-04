package com.meetingops.interfaces.handler;

/**
 * Exception thrown when a requested resource is not found.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a ResourceNotFoundException.
     *
     * @param message the error message
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }

    /**
     * Constructs a ResourceNotFoundException with resource type and identifier.
     *
     * @param resourceType the type of resource (e.g., "Meeting", "DraftAction")
     * @param id           the resource identifier
     */
    public ResourceNotFoundException(final String resourceType, final String id) {
        super(resourceType + " not found with id: " + id);
    }
}
