# MeetingOps Backend

**Spring Boot 3.4 (Java 21) + LangChain4j + MCP Server** — AI-powered meeting action item extraction and follow-through workflow with human-in-the-loop review.

## Architecture

This project follows **Clean/Hexagonal Architecture** with strict dependency rules. The domain layer has zero external dependencies. All dependencies flow inward toward the domain.

```
┌─────────────────────────────────────────────────────────┐
│  Interfaces (Controllers, Handlers)                     │
├─────────────────────────────────────────────────────────┤
│  Application (Services, AI Agents, DTOs, Events)        │
├─────────────────────────────────────────────────────────┤
│  Domain (Models, Enums, Ports)                          │
├─────────────────────────────────────────────────────────┤
│  Infrastructure (JPA, Kafka, MCP, Vector, Config)       │
└─────────────────────────────────────────────────────────┘
```

## Key Technologies

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Framework | Spring Boot 3.4 | Application framework |
| Language | Java 21 | Runtime with records, pattern matching |
| AI | LangChain4j 1.0.0-beta3 | LLM integration and AI Services |
| MCP Server | Spring AI MCP Server 1.0.1 | Tool execution via Model Context Protocol |
| Database | PostgreSQL 16 + pgvector | Relational storage + vector embeddings |
| Messaging | Apache Kafka | Event-driven agent pipeline |
| Security | Spring Security + OAuth2/JWT | API authentication |
| ORM | Spring Data JPA + Hibernate | Persistence layer |

## Agent Pipeline

The processing pipeline runs in four stages after a meeting transcript is ingested:

1. **Extraction Agent** — Parses transcript into structured Action Items and Decisions using LLM tool calls
2. **Grounding Agent** — Classifies items against historical data via RAG (pgvector similarity search)
3. **Validation Agent** — Flags vague, ambiguous, or low-confidence items for human review
4. **Drafting Agent** — Generates draft follow-through actions (tasks, reminders, emails) via MCP tools

All draft actions enter a **human review queue** and require explicit approval before execution.

## Project Structure

```
src/main/java/com/meetingops/
├── MeetingOpsApplication.java          # Entry point
├── domain/
│   ├── enumeration/                    # Domain enums (ItemType, Status, etc.)
│   └── model/                          # Domain models + port interfaces
├── application/
│   ├── dto/                            # Request/Response DTOs
│   ├── event/                          # Domain events
│   ├── service/                        # Application services + interfaces
│   ├── ai/
│   │   ├── service/                    # AI agent interfaces + implementations
│   │   ├── prompt/                     # Prompt management
│   │   ├── tool/                       # LangChain4j tool definitions
│   │   └── util/                       # AI utilities
│   └── util/                           # General utilities
├── interfaces/
│   ├── rest/                           # REST controllers
│   └── handler/                        # Global exception handling
├── infrastructure/
│   ├── config/                         # Configuration classes
│   ├── jpa/                            # JPA entities + repositories
│   ├── persistence/                    # Event publishers/consumers
│   └── vector/                         # pgvector implementation
└── mcp/
    └── tool/                           # MCP server tool definitions
```

## Quick Start

### Prerequisites
- Java 21+ (JDK)
- Maven 3.9+
- Docker + Docker Compose (for PostgreSQL + Kafka)

### Development

```bash
# 1. Start infrastructure services
docker-compose up -d postgres kafka

# 2. Configure environment
export DB_URL="jdbc:postgresql://localhost:5432/meeting_ops"
export DB_USERNAME="meeting_ops"
export DB_PASSWORD="meeting_ops"
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
export OPENAI_API_KEY="your-api-key"

# 3. Run database migrations
# (Schema is auto-initialized by docker-compose mount)

# 4. Start the application
./mvnw spring-boot:run

# 5. (Optional) Start in debug mode
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

### Docker

```bash
# Build and run everything
docker-compose up --build

# Run only infrastructure
docker-compose up -d postgres kafka
```

### Testing

```bash
# Run all tests
./mvnw test

# Run integration tests
./mvnw verify -P integration

# Run with coverage
./mvnw test jacoco:report
```

## Configuration

All configuration is managed via `application.yml` with environment variable overrides. See the configuration file for the full list of configurable properties.

Key properties:

| Property | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/meeting_ops` | Database connection |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker |
| `OPENAI_API_KEY` | (required) | LLM API key |
| `OAUTH2_ISSUER_URI` | (required) | OAuth2 issuer |
| `MCP_TRANSPORT` | `http` | MCP transport type (http/stdio) |

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/meetings` | Create meeting with transcript |
| `GET` | `/api/v1/meetings/{id}` | Get meeting details |
| `GET` | `/api/v1/meetings` | List meetings (tenant-scoped) |
| `GET` | `/api/v1/meetings/{id}/items` | Get extracted items |
| `GET` | `/api/v1/review-queue` | Get pending draft actions |
| `POST` | `/api/v1/draft-actions/{id}/decision` | Submit review decision |
| `GET` | `/api/v1/draft-actions/{id}/audit-trail` | Get audit trail |
| `POST` | `/api/v1/review-queue/bulk-approve` | Bulk approve drafts |
| `GET` | `/actuator/health` | Health check |

## Adding New MCP Tools

To add a new MCP server tool (e.g., for a new external system integration):

1. Create a new class in `com.meetingops.mcp.tool` with `@Component` annotation
2. Annotate methods with `@Tool(name = "...", description = "...")`
3. Define parameters with `@ToolParam`
4. The tool is automatically registered by the Spring AI MCP Server

## Extending the Agent Pipeline

To add a new agent step:

1. Define an interface in `application.ai.service`
2. Implement it in the same package using LangChain4j AI Service
3. Add the step to `AgentPipelineServiceImpl.processMeeting()`
4. Create a corresponding prompt template in `src/main/resources/prompts/`

## License

Proprietary. All rights reserved.
