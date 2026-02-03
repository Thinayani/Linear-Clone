## Linear Clone — Spring Boot Backend

A production-grade project management API inspired by [Linear.app](https://linear.app), built with Java 21 + Spring Boot 3.2.

---

## Architecture Overview

```
src/main/java/com/linearclone/
├── LinearCloneApplication.java
├── config/
│   ├── SecurityConfig.java         # Spring Security + JWT + CORS
│   └── OpenApiConfig.java          # Swagger/OpenAPI setup
├── controller/
│   ├── AuthController.java         # POST /auth/register, /login, /refresh, /logout
│   ├── WorkspaceController.java    # Workspace CRUD + member management
│   ├── IssueController.java        # Full issue CRUD + filters + search
│   └── CycleController.java        # Sprint/cycle lifecycle
├── service/impl/
│   ├── AuthService.java            # JWT auth + refresh token rotation
│   ├── WorkspaceService.java       # Multi-tenant workspace management
│   ├── IssueService.java           # Core business logic + activity tracking
│   └── CycleService.java           # Sprint management + overlap validation
├── entity/
│   ├── User.java
│   ├── Workspace.java + WorkspaceMember.java
│   ├── Team.java + TeamMember.java
│   ├── Project.java
│   ├── Cycle.java
│   ├── Issue.java                  # Core domain entity
│   ├── Label.java + Comment.java
│   ├── IssueActivity.java          # Audit trail
│   ├── RefreshToken.java
│   └── TeamIssueSequence.java      # Auto-increment per team (ENG-001)
├── repository/                     # Spring Data JPA repos with custom JPQL
├── dto/
│   ├── request/                    # Validated request DTOs
│   └── response/                   # Structured API response DTOs
├── security/
│   ├── JwtUtil.java                # JWT generation & validation
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
└── exception/
    ├── GlobalExceptionHandler.java  # Centralized error handling
    └── *.java                       # Custom exception types
```

---

## Core Features

| Feature | Details |
|---|---|
| **Auth** | JWT access tokens + rotating refresh tokens, BCrypt password hashing |
| **Multi-tenancy** | Workspace → Teams → Projects hierarchy |
| **Issues** | Full CRUD, status/priority/assignee, sub-issues, labels, search |
| **Cycles (Sprints)** | Draft → Started → Completed lifecycle, overlap validation |
| **Projects** | Group issues under time-bounded initiatives |
| **Activity Log** | Auto-recorded audit trail on every field change |
| **Role-based Access** | Workspace roles (Owner/Admin/Member/Guest), Team roles (Lead/Member/Viewer) |

---

## Data Model

```
Workspace
  └── Teams (identifier: ENG, MKT, etc.)
        ├── Issues (ENG-001, ENG-002...)
        │     ├── Sub-Issues
        │     ├── Comments
        │     ├── Labels
        │     └── Activity Log
        ├── Projects
        └── Cycles (Sprints)
```

---

## Getting Started

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven 3.9+

### Run with Docker (recommended)

```bash
# Clone and start everything
docker-compose up -d

# API is live at:
http://localhost:8080/api
http://localhost:8080/api/swagger-ui.html   # Interactive API docs
```

### Run locally

```bash
# 1. Start Postgres + Redis
docker-compose up postgres redis -d

# 2. Run the app
./mvnw spring-boot:run

# Or build and run jar
./mvnw package -DskipTests
java -jar target/linear-clone-1.0.0.jar
```

---

## API Endpoints

### Auth
```
POST   /api/auth/register     Register new user
POST   /api/auth/login        Login → access + refresh tokens
POST   /api/auth/refresh      Rotate refresh token
POST   /api/auth/logout       Revoke refresh token
```

### Workspaces
```
POST   /api/workspaces                          Create workspace
GET    /api/workspaces                          My workspaces
GET    /api/workspaces/{slug}                   Get by slug
PATCH  /api/workspaces/{id}                     Update
POST   /api/workspaces/{id}/members             Invite member
DELETE /api/workspaces/{id}/members/{userId}    Remove member
```

### Issues
```
POST   /api/issues                              Create issue
GET    /api/issues/team/{teamId}                Paginated list
GET    /api/issues/team/{teamId}/filter         Filter by status/priority/assignee
GET    /api/issues/team/{teamId}/search?q=      Full-text search
GET    /api/issues/{id}                         Get single issue
PATCH  /api/issues/{id}                         Partial update
DELETE /api/issues/{id}                         Delete
GET    /api/issues/{id}/sub-issues              Get child issues
```

### Cycles (Sprints)
```
POST   /api/cycles/team/{teamId}                Create cycle
GET    /api/cycles/team/{teamId}                All cycles
GET    /api/cycles/team/{teamId}/active         Active cycle
POST   /api/cycles/{id}/start                   Start cycle
POST   /api/cycles/{id}/complete                Complete cycle
POST   /api/cycles/{id}/issues/{issueId}        Add issue to cycle
DELETE /api/cycles/issues/{issueId}             Remove issue from cycle
```

---

## Configuration

Key environment variables:

| Variable | Default | Description |
|---|---|---|
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `REDIS_HOST` | `localhost` | Redis hostname |
| `JWT_SECRET` | (set in yaml) | HS256 signing key (min 256-bit) |
| `CORS_ORIGINS` | `localhost:3000` | Allowed frontend origins |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2, Java 21 |
| Database | PostgreSQL 16, Spring Data JPA, Hibernate |
| Cache | Redis (Spring Cache) |
| Auth | Spring Security, JWT (jjwt), BCrypt |
| Migrations | Flyway |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Build | Maven, Docker, Docker Compose |
| Code Gen | Lombok, MapStruct |

---

## Next Steps (to extend this project)

- [ ] **WebSocket** — real-time issue updates with Spring WebSocket + STOMP
- [ ] **Email notifications** — Spring Mail + async events
- [ ] **File attachments** — AWS S3 integration
- [ ] **Webhooks** — outbound events on issue changes
- [ ] **Analytics endpoint** — velocity, burndown chart data
- [ ] **Rate limiting** — Bucket4j or Redis-based
- [ ] **Unit & Integration tests** — JUnit 5, Testcontainers
