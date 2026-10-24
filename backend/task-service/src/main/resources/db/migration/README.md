# db/migration/

Flyway versioned migrations. Flyway runs them in order on startup and records them in `flyway_schema_history`.

- `V1__init_schema.sql` - creates all tables and indexes.

**Never edit an applied migration.** To change the schema add `V2__something.sql`.
