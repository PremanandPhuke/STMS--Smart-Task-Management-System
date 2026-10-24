# backend/

**Why this folder exists:** it is a *Maven multi-module* project. The parent `pom.xml` fixes the Spring Boot,
gRPC and Java versions once, and the three modules below inherit them.

| Module | Role |
|---|---|
| `grpc-contract/` | The `.proto` file shared by both services. Generates the gRPC Java classes. |
| `task-service/` | The main REST API (port 8080). Talks to PostgreSQL. Acts as gRPC **client**. |
| `notification-service/` | Small gRPC **server** (port 9090, HTTP health on 8081) that delivers reminders. |

Build everything: `mvn clean install` (run inside this folder).
