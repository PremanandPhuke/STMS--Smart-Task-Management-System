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
