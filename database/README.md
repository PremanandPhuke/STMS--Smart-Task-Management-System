# database/

**Why this folder exists:** helper scripts for PostgreSQL.

| File | Purpose |
|---|---|
| `00-create-database.sql` | Creates the empty database `stms_db` and the user `stms_user`. Needed only if you installed PostgreSQL yourself (skip it when you use `docker compose up -d`). |

You do **not** create tables by hand. When task-service starts, Flyway runs
`backend/task-service/src/main/resources/db/migration/V1__init_schema.sql` and creates every table.
See `docs/DATABASE.md` for the full structure.
