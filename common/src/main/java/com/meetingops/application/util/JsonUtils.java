package com.meetingops.application.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility for JSON serialization/deserialization within the application layer.
 *
 * <p>Provides a facade over Jackson ObjectMapper for consistent JSON
 * handling across agent payloads and MCP tool inputs/outputs.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper objectMapper;

    /**
     * Serializes an object to a JSON string.
     *
     * @param object the object to serialize
     * @return the JSON string
     * @throws RuntimeException if serialization fails
     */
    public String toJson(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Deserializes a JSON string to the specified type.
     *
     * @param json  the JSON string
     * @param clazz the target class
     * @param <T>   the target type
     * @return the deserialized object
     * @throws RuntimeException if deserialization fails
     */
    public <T> T fromJson(final String json, final Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to {}", clazz.getSimpleName(), e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
}
