# repository/

Spring Data JPA interfaces (no SQL needed for simple queries).

- `TaskRepository` extends `JpaSpecificationExecutor` for dynamic filters.
- `TaskSpecs` holds reusable filter fragments (access rules, status, priority, overdue...).
- One custom `@Query` finds tasks that need a reminder.
