# notification-service/

**Why this module exists:** a tiny second service that shows how microservices talk over gRPC.
It receives "send reminder" calls from task-service and either logs them or sends a real email.

- gRPC port **9090**, HTTP health port **8081**
- Every call must carry the `x-api-key` header (service-to-service authentication)
- Real e-mail: uncomment the `spring.mail` block in `src/main/resources/application.yml`

Run: `mvn spring-boot:run` inside this folder.
