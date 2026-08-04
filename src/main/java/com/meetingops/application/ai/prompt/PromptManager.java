package com.meetingops.application.ai.prompt;

/**
 * Interface for managing AI prompts and template resolution.
 *
 * <p>Centralizes prompt storage, versioning, and template variable
 * substitution for all agents in the pipeline. Prompts are loaded
 * from classpath resources and may be overridden via configuration.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface PromptManager {

    /**
     * Resolves a prompt by name, optionally substituting template variables.
     *
     * @param promptName the prompt identifier (e.g., "extraction", "grounding")
     * @param variables  key-value pairs for template substitution
     * @return the resolved prompt text
     */
    String resolvePrompt(String promptName, java.util.Map<String, String> variables);

    /**
     * Gets the current version of a prompt for audit trail purposes.
     *
     * @param promptName the prompt identifier
     * @return the version string (e.g., "v1.2.0")
     */
    String getPromptVersion(String promptName);
}
