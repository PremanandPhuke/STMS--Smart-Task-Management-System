# task-service/

**Why this module exists:** it is the heart of the system - the REST API used by the React app.

Layers (request flows top to bottom):

`controller` -> `service` -> `repository` -> PostgreSQL, with `dto` objects at the edges and `security`
protecting every request.

Run: `mvn spring-boot:run` (PostgreSQL must be running). Swagger UI: http://localhost:8080/swagger-ui.html
