package com.meetingops.aipipeline.config;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.mcp.McpToolProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for LangChain4j MCP client and tool provider integration.
 *
 * <p>Sets up the MCP client transport, client instance, and tool provider
 * that bridges LangChain4j AI services with the MCP server tools defined
 * in this application. Supports both STDIO and HTTP transports.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

    @Value("${meetingops.mcp.transport:http}")
    private String transportType;

    @Value("${meetingops.mcp.server.url:http://localhost:8081/mcp}")
    private String mcpServerUrl;

    @Value("${meetingops.mcp.transport.stdio.command:}")
    private String stdioCommand;

    /**
     * Creates the MCP transport based on configuration.
     *
     * @return the configured MCP transport
     */
    @Bean
    public McpTransport mcpTransport() {
        return switch (transportType) {
            case "stdio" -> {
                log.info("Configuring STDIO MCP transport");
                // TODO: Parse stdioCommand into List.of()
                yield new StdioMcpTransport.Builder()
                        .command(List.of(stdioCommand))
                        .build();
            }
            default -> {
                log.info("Configuring HTTP MCP transport: {}", mcpServerUrl);
                yield new HttpMcpTransport.Builder()
                        .sseUrl(mcpServerUrl)
                        .build();
            }
        };
    }

    /**
     * Creates the MCP client for tool discovery and invocation.
     *
     * @param transport the MCP transport
     * @return the configured MCP client
     */
    @Bean
    public McpClient mcpClient(final McpTransport transport) {
        return new DefaultMcpClient.Builder()
                .clientName("meeting-ops-mcp")
                .transport(transport)
                .build();
    }

    /**
     * Creates the MCP tool provider for AI service integration.
     *
     * <p>The tool provider bridges MCP server tools to LangChain4j
     * AI services. Tool names can be filtered to restrict which
     * tools are available to specific agents.</p>
     *
     * @param mcpClient the MCP client
     * @return the configured tool provider
     */
    @Bean
    public McpToolProvider mcpToolProvider(final McpClient mcpClient) {
        return McpToolProvider.builder()
                .mcpClients(mcpClient)
                // TODO: Add tool name filtering per agent if needed
                // .filterToolNames("create-task", "calendar-reminder", "draft-email")
                .build();
    }
}
