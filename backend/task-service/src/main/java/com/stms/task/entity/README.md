# entity/

JPA entities - Java classes mapped to database tables.

`User`, `Project`, `Task`, `Label`, `Comment` plus the enums `Role`, `TaskStatus`, `Priority`, `Recurrence`.
Relationships are `LAZY` by default and `Task.labels` uses `@BatchSize` to avoid the N+1 query problem.
