# MeetingOps Backend (Microservices Architecture)

**Spring Boot 3.4 (Java 21) + LangChain4j + MCP Server** — Multi-module Microservices Architecture for AI-powered meeting action item extraction, follow-through workflow, and human-in-the-loop review.

## Microservices Architecture

The application is decomposed into decoupled, specialized microservices managed under a Maven Multi-Module project:

```
                     ┌────────────────────────┐
                     │   API Gateway (8080)   │
                     └───────────┬────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌────────▼─────────┐    ┌────────▼─────────┐    ┌────────▼─────────┐
│ Meeting Service  │    │  Review Service  │    │   MCP Service    │
│     (8081)       │    │     (8083)       │    │     (8084)       │
└────────┬─────────┘    └──────────────────┘    └────────▲─────────┘
         │                                               │
         │ (Kafka: meeting.transcribed)                  │ (HTTP MCP Tools)
         ▼                                               │
┌────────────────────────────────────────────────────────┴─┐
│                   AI Pipeline Service (8082)            │
└──────────────────────────────────────────────────────────┘
```

### Microservice Modules

| Microservice Module | Port | Responsibility | Key Dependencies |
|---------------------|------|----------------|------------------|
| **`common`** | N/A | Shared domain models, DTOs, events, JPA entities, repositories, and exception handling | Spring Data JPA, Security, Lombok |
| **`api-gateway`** | `8080` | Entry point & reverse proxy routing incoming traffic to backend microservices | Spring Cloud Gateway |
| **`meeting-service`** | `8081` | Ingests meeting transcripts, manages meeting lifecycle, publishes Kafka events | Spring Web, Spring Kafka, Flyway |
| **`ai-pipeline-service`** | `8082` | Listens to Kafka meeting events, executes AI extraction, grounding (pgvector), validation & drafting agents | LangChain4j, pgvector, Kafka |
| **`review-service`** | `8083` | Manages human review queue, audit decisions, and bulk approvals | Spring Web, Spring Data JPA |
| **`mcp-service`** | `8084` | FastMCP Python Server hosting Calendar, Email, and TaskTracker execution tools | FastMCP, Uvicorn, Pydantic |

## Key Technologies

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Framework | Spring Boot 3.4 & Spring Cloud Gateway | Microservices application framework & gateway |
| Language | Java 21 | Runtime with records, pattern matching |
| AI | LangChain4j 1.0.0-beta3 | LLM integration and AI Services |
| MCP Server | Spring AI MCP Server 1.0.1 | Tool execution via Model Context Protocol |
| Database | PostgreSQL 16 + pgvector | Relational storage + vector embeddings |
| Messaging | Apache Kafka | Event-driven agent pipeline decoupling |
| Security | Spring Security + OAuth2/JWT | API authentication |

## Agent Pipeline Workflow

1. **Meeting Service Ingestion** — Transcript ingested via API Gateway (`POST /api/v1/meetings`), saved to PostgreSQL, emitting `meeting.transcribed` event to Kafka.
2. **Extraction Agent** — Parses transcript into structured Action Items and Decisions using LLM tool calls.
3. **Grounding Agent** — Classifies items against historical data via RAG (pgvector similarity search).
4. **Validation Agent** — Flags vague, ambiguous, or low-confidence items for human review.
5. **Drafting Agent** — Generates draft follow-through actions (tasks, reminders, emails) via MCP tools on MCP Service (`http://localhost:8084/mcp`).
6. **Human Review Queue** — Draft actions enter review queue in `review-service` requiring explicit human approval (`POST /api/v1/draft-actions/{id}/decision`).

## Project Directory Structure

```
meeting-ops-backend/
├── pom.xml                               # Parent POM (multi-module aggregator)
├── docker-compose.yml                    # Multi-container microservices compose config
├── Dockerfile                            # Parameterized multi-stage Docker build
├── common/                               # Shared module
│   └── src/main/java/com/meetingops/
│       ├── domain/                       # Domain models, enums & ports
│       ├── application/dto/              # Request & Response DTOs
│       ├── application/event/            # Kafka Domain Events
│       ├── infrastructure/jpa/           # JPA entities & Spring Data repositories
│       └── interfaces/handler/           # Global Exception Handling
├── api-gateway/                          # API Gateway microservice (8080)
│   └── src/main/java/com/meetingops/gateway/
├── meeting-service/                      # Meeting Ingestion & CRUD microservice (8081)
│   └── src/main/java/com/meetingops/meeting/
├── ai-pipeline-service/                  # AI Pipeline Agent microservice (8082)
│   └── src/main/java/com/meetingops/aipipeline/
├── review-service/                       # Human Review Queue microservice (8083)
│   └── src/main/java/com/meetingops/review/
└── mcp-service/                          # Spring AI MCP Server microservice (8084)
    └── src/main/java/com/meetingops/mcp/
```

## Quick Start

### Prerequisites
- Java 21+ (JDK)
- Maven 3.9+
- Docker + Docker Compose

### Running with Docker Compose

To build and launch all 5 microservices, API Gateway, PostgreSQL (pgvector), and Kafka:

```bash
docker-compose up --build
```

### Running Individual Microservices locally

```bash
# 1. Start infrastructure (PostgreSQL & Kafka)
docker-compose up -d postgres kafka

# 2. Build all modules
./mvnw clean package -DskipTests

# 3. Run individual microservices
./mvnw spring-boot:run -pl api-gateway
./mvnw spring-boot:run -pl meeting-service
./mvnw spring-boot:run -pl ai-pipeline-service
./mvnw spring-boot:run -pl review-service
./mvnw spring-boot:run -pl mcp-service
```

## API Endpoints (Routed via API Gateway on port 8080)

| Method | Path | Target Microservice | Description |
|--------|------|--------------------|-------------|
| `POST` | `/api/v1/meetings` | `meeting-service:8081` | Create meeting with transcript |
| `GET` | `/api/v1/meetings/{id}` | `meeting-service:8081` | Get meeting details |
| `GET` | `/api/v1/meetings` | `meeting-service:8081` | List meetings (tenant-scoped) |
| `GET` | `/api/v1/meetings/{id}/items` | `meeting-service:8081` | Get extracted items |
| `GET` | `/api/v1/review-queue` | `review-service:8083` | Get pending draft actions |
| `POST` | `/api/v1/draft-actions/{id}/decision` | `review-service:8083` | Submit review decision |
| `GET` | `/api/v1/draft-actions/{id}/audit-trail` | `review-service:8083` | Get audit trail |
| `POST` | `/api/v1/review-queue/bulk-approve` | `review-service:8083` | Bulk approve drafts |
| `GET` | `/mcp/**` | `mcp-service:8084` | Spring AI MCP tool protocol |

## License

Proprietary. All rights reserved.
