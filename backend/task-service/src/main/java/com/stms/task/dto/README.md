# dto/

Data Transfer Objects (Java `record`s) - the JSON shapes of requests and responses.

Entities are never returned directly: DTOs stop password hashes leaking, avoid lazy-loading errors and let the
API evolve independently of the database. Validation annotations (`@NotBlank`, `@Email`...) live here.
