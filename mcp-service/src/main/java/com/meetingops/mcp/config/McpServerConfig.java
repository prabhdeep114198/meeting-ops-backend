package com.meetingops.mcp.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Spring AI MCP Server.
 *
 * <p>The MCP Server is auto-configured by the Spring AI MCP Server
 * Boot Starter. Tools annotated with {@code @McpTool} are automatically
 * scanned and registered. This class provides additional custom
 * configuration if needed.</p>
 *
 * <p>Configuration properties are managed via application.yml:
 * <pre>
 *   spring.ai.mcp.server:
 *     type: SYNC
 *     protocol: STREAMABLE
 *     annotation-scanner.enabled: true
 * </pre>
 * </p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Configuration
public class McpServerConfig {

    // TODO: Add custom MCP server configuration as needed
    // - Custom transport settings
    // - Server capabilities configuration
    // - Security configuration for MCP endpoints
}
