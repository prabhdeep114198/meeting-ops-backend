package com.meetingops.application.ai.prompt;

/**
 * Exception thrown when a prompt template cannot be found on the classpath.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public class PromptNotFoundException extends RuntimeException {

    /**
     * Constructs a new PromptNotFoundException.
     *
     * @param promptName the name of the prompt that was not found
     * @param cause      the underlying I/O exception
     */
    public PromptNotFoundException(final String promptName, final Throwable cause) {
        super("Prompt template not found: " + promptName, cause);
    }
}
