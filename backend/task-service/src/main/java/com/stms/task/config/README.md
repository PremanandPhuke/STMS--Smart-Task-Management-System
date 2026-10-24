# config/

Spring `@Configuration` classes.

- `SecurityConfig` - filter chain, which URLs are public, CORS, BCrypt encoder, stateless sessions.
- `OpenApiConfig` - Swagger UI with a bearer-token "Authorize" button.
- `GrpcClientConfig` - adds the `x-api-key` header to every outgoing gRPC call.
- `DataSeeder` - creates demo users/projects/tasks on the first start (disable with `app.seed.enabled=false`).
