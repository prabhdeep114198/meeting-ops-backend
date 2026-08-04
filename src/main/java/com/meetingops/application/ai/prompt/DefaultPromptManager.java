package com.meetingops.application.ai.prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Default implementation of {@link PromptManager} that loads prompts
 * from classpath resources and performs template variable substitution.
 *
 * <p>Prompt files are stored in {@code src/main/resources/prompts/} with
 * the naming convention {@code <name>.txt}. Template variables are
 * enclosed in curly braces (e.g., {@code {{transcript}}}).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultPromptManager implements PromptManager {

    private static final String PROMPT_BASE_PATH = "prompts/";
    private static final String PROMPT_EXTENSION = ".txt";

    @Override
    public String resolvePrompt(final String promptName,
                                final Map<String, String> variables) {
        try {
            String template = loadPromptTemplate(promptName);
            return substituteVariables(template, variables);
        } catch (IOException e) {
            log.error("Failed to resolve prompt '{}': {}", promptName, e.getMessage());
            throw new PromptNotFoundException(promptName, e);
        }
    }

    @Override
    public String getPromptVersion(final String promptName) {
        // TODO: Implement version tracking via metadata file or database
        return "v1.0.0";
    }

    /**
     * Loads a prompt template from the classpath.
     */
    private String loadPromptTemplate(final String promptName) throws IOException {
        String resourcePath = PROMPT_BASE_PATH + promptName + PROMPT_EXTENSION;
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Substitutes template variables in the format {@code {{key}}} with values.
     */
    private String substituteVariables(final String template,
                                       final Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
