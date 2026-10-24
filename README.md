# Smart Task Management System

A full stack, production-style task manager built with **Java 17 + Spring Boot 3**, **PostgreSQL**, **gRPC** and **React + TypeScript**.
It is designed as a learning project: every folder has a `README.md` explaining why it exists.

## Features
- Register / login with **JWT** (access + refresh tokens), roles `USER` and `ADMIN`
- Projects, tasks, **subtasks**, labels, comments, assignees
- Kanban board with drag and drop, search, filters, sorting, pagination
- **Smart priority score (0-100)** from priority and due date; refreshed every night
- **Recurring tasks**: completing one creates the next occurrence
- **Reminders**: a scheduled job calls a separate **notification-service over gRPC**
- Dashboard: counts, overdue, due today, top tasks to do next
- Security: Spring Security, BCrypt, rate-limited login, ownership checks, CORS, validation, service-to-service API key
- Swagger UI, Flyway migrations, global error handling, unit tests

## Architecture
```
React (5173) --REST/JSON + JWT--> task-service (8080) --JDBC--> PostgreSQL (5432)
                                       |
                                       +--gRPC (x-api-key)--> notification-service (9090)
```

## Tech stack
| Area | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA (Hibernate), Spring Security, Bean Validation |
| Auth | JWT (jjwt), BCrypt |
| Database | PostgreSQL 16, Flyway |
| Messaging | gRPC + Protocol Buffers (`net.devh` Spring starters) |
| Docs / Tests | springdoc OpenAPI (Swagger), JUnit 5 |
| Frontend | React 19, TypeScript, Vite, React Router, Axios |

## Run it locally
You need: JDK 17+, Maven 3.9+, Node 20.19+ (or 22), and Docker **or** a local PostgreSQL.

**1. Database** (pick one)
```bash
docker compose up -d                          # easiest
# or, with your own PostgreSQL:  psql -U postgres -f database/00-create-database.sql
```
**2. Backend** (three terminals, or build once and run two)
```bash
cd backend
mvn clean install                             # builds all modules, generates gRPC classes
cd notification-service && mvn spring-boot:run     # terminal A  (gRPC 9090)
cd task-service && mvn spring-boot:run             # terminal B  (REST 8080)
```
**3. Frontend**
```bash
cd frontend
npm install
npm run dev                                   # open http://localhost:5173
```
**Demo logins** (created automatically on first start)
| User | Email | Password |
|---|---|---|
| Admin | admin@stms.com | Admin@123 |
| Demo user | demo@stms.com | Demo@1234 |
| Teammate | riya@stms.com | Riya@1234 |

Swagger UI: http://localhost:8080/swagger-ui.html - Health: http://localhost:8080/actuator/health

**Test the reminders quickly:** set `app.reminders.cron: "0 * * * * *"` (every minute) in task-service `application.yml`, restart, and watch the `REMINDER ->` lines in the notification-service console.

## API summary
| Method & path | Description |
|---|---|
| POST `/api/auth/register`, `/login`, `/refresh` | Public auth endpoints |
| GET `/api/auth/me` | Current user |
| GET/POST `/api/projects`, GET/PUT/DELETE `/api/projects/{id}` | Projects (owner or admin) |
| GET `/api/tasks?projectId&status&priority&q&page&size&sort` | Filtered, paged tasks |
| POST/PUT/DELETE `/api/tasks`, PATCH `/api/tasks/{id}/status` | Task commands |
| GET `/api/tasks/{id}/subtasks` | Subtasks |
| GET/POST `/api/tasks/{id}/comments`, DELETE `/api/comments/{id}` | Comments |
| GET/POST/DELETE `/api/labels` | Labels |
| GET `/api/dashboard` | Statistics |
| GET `/api/admin/users` | Admin only |

## Folder map
```
stms/
|-- docker-compose.yml        PostgreSQL for local development
|-- database/                 SQL helper to create the DB and user
|-- docs/                     DATABASE.md (tables + ER diagram)
|-- backend/                  Maven multi-module parent
|   |-- grpc-contract/        notification.proto -> generated gRPC classes
|   |-- notification-service/ gRPC server (reminders)
|   `-- task-service/         REST API (controller, service, repository, entity, dto, security...)
`-- frontend/                 React + TypeScript + Vite
```

## Common interview questions (answered with this project)

**1. What is Spring Boot and why use it?**
Spring Boot is an opinionated layer on top of Spring that removes boilerplate: auto-configuration, starter dependencies, embedded Tomcat and production features such as Actuator. In this project, adding `spring-boot-starter-data-jpa` and a PostgreSQL driver is enough for it to configure a `DataSource`, Hibernate and transactions.

**2. What does `@SpringBootApplication` do?**
It combines `@Configuration`, `@EnableAutoConfiguration` and `@ComponentScan`. It marks the configuration class, turns on auto-configuration based on the classpath, and scans the package for beans.

**3. Explain the layered architecture.**
Controller (HTTP) -> Service (business rules and transactions) -> Repository (data access) -> Database, with DTOs at the edges. It gives separation of concerns and testability. Here `TaskController` only calls `TaskService`, which owns the permission and recurrence rules.

**4. What is dependency injection? Constructor vs field injection?**
Spring creates objects and supplies their dependencies instead of classes calling `new`. Constructor injection (used here via Lombok `@RequiredArgsConstructor`) keeps fields `final`, makes classes easy to unit test and avoids hidden dependencies.

**5. What is JPA and Hibernate? How do you map relationships?**
JPA is the specification for object-relational mapping; Hibernate is the implementation. `Task` has `@ManyToOne` to `Project` and `User`, and `@ManyToMany` to `Label` through the `task_labels` table.

**6. What is the N+1 problem and how did you avoid it?**
Loading a list of N entities and then triggering one extra query per entity for a lazy association. We used `@EntityGraph` to fetch `project` and `assignee` with the task query, and `@BatchSize` on `labels` to load them in batches.

**7. LAZY vs EAGER loading?**
LAZY loads an association only when accessed; EAGER loads it immediately. LAZY is the safer default. We set `open-in-view: false`, so lazy data must be read inside a `@Transactional` service method and is mapped to DTOs there.

**8. What does `@Transactional` do? What is `readOnly = true`?**
It wraps a method in a database transaction (commit on success, rollback on a runtime exception) through a proxy. `readOnly = true` is a hint that allows optimisations and prevents accidental writes. Because of dirty checking, changing a managed entity inside a transaction saves it without calling `save()`.

**9. Why use DTOs instead of returning entities?**
To avoid exposing sensitive fields (the password hash), prevent lazy-loading errors and circular JSON, and keep the API contract independent of the database model.

**10. How does Spring Security work internally?**
Requests pass through a chain of servlet filters (`SecurityFilterChain`). Our `JwtAuthFilter` reads the bearer token, validates it, and stores an `Authentication` in the `SecurityContext`. Authorization rules (`authorizeHttpRequests`, `@PreAuthorize`) then decide access.

**11. How does JWT authentication work? Why access and refresh tokens?**
After login the server signs a token containing the user and expiry. The client sends it on every request; the server verifies the signature - no session is stored (stateless). The access token is short-lived (30 min) to limit damage if stolen; the long-lived refresh token (7 days) is used only to get a new access token. The axios interceptor does this silently on a 401.

**12. Why is CSRF disabled here?**
CSRF attacks exploit browsers sending cookies automatically. This API is stateless and uses an `Authorization` header that browsers do not attach on their own, so CSRF protection is not needed. If tokens were stored in cookies, it would have to be enabled.

**13. How do you store passwords?**
Never in plain text. `BCryptPasswordEncoder` adds a random salt and is deliberately slow, so brute-forcing leaked hashes is expensive.

**14. Authentication vs authorization? Where is each done?**
Authentication proves who you are (login + JWT). Authorization decides what you may do: role rules (`/api/admin/**` needs `ADMIN`) and ownership rules in `TaskService` (only the project owner may edit; an assignee may only change status).

**15. How do you handle exceptions in a REST API?**
A `@RestControllerAdvice` class maps exceptions to a consistent JSON body and HTTP status: 400 validation, 401 bad credentials, 403 forbidden, 404 not found, 409 conflict, 500 unexpected (logged, with a safe message).

**16. How do you validate input?**
Bean Validation annotations on DTOs (`@NotBlank`, `@Email`, `@Size`) together with `@Valid` on the controller parameter. Failures raise `MethodArgumentNotValidException`, which our handler turns into a 400 with field errors.

**17. What is gRPC and how does it differ from REST?**
gRPC is an RPC framework using Protocol Buffers (binary) over HTTP/2, with a strict `.proto` contract, code generation and streaming. It is smaller and faster than JSON but not browser-friendly, so React uses REST while services use gRPC. Here task-service calls `SendReminder` on notification-service.

**18. How did you secure the gRPC call?**
A client interceptor adds an `x-api-key` header and a server interceptor rejects calls without it (`UNAUTHENTICATED`). In production add TLS/mTLS as well.

**19. What is Flyway and why not `ddl-auto=update`?**
Flyway applies versioned SQL migrations and records them, giving repeatable, reviewable schema changes across environments. `ddl-auto=update` guesses changes and is unsafe in production. We use `ddl-auto=validate` so Hibernate only checks that entities match the schema.

**20. How does pagination and dynamic filtering work?**
`TaskController` builds a `Pageable` (page, size, whitelisted sort field). `TaskService` composes JPA `Specification`s (project, status, priority, search text, access rules) and calls `findAll(spec, pageable)`. The response is a `PageResponse` DTO.

**21. How do scheduled jobs work?**
`@EnableScheduling` plus `@Scheduled(cron = ...)`. The reminder job finds due tasks, calls gRPC, and sets `reminder_sent` so a task is not reminded twice. For several instances you would add a lock (ShedLock) so only one runs the job.

**22. What is CORS and how did you configure it?**
Browsers block cross-origin calls unless the server allows them. `SecurityConfig` allows the React origin (`http://localhost:5173`, configurable via `CORS_ORIGINS`) and specific methods/headers.

**23. How did you protect against brute force?**
`RateLimitFilter` allows 10 login/register attempts per minute per IP and answers `429`. In production prefer a shared store (Redis) or API gateway.

**24. How do you test this application?**
Unit tests without Spring for pure logic (`PriorityScoringServiceTest`, `JwtServiceTest`). Next steps: `@DataJpaTest` with Testcontainers for repositories, and `@SpringBootTest` + MockMvc with `@WithMockUser` for 401/403 tests.

**25. How would you take this to production?**
Dockerize each service, externalise secrets (env vars/secret manager), run behind HTTPS (Nginx), add CI/CD (GitHub Actions), monitoring (Actuator + Prometheus + Grafana), database backups, and scale the services horizontally behind a load balancer.

> Want any of these as a full interview note (Definition, Key Concepts, How It Works, Interview Answer)? Ask for the topic.
