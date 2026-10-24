# service/

Business logic. Controllers stay thin; the rules live here.

- `AuthService` - register, login, refresh.
- `TaskService` - CRUD, filters, permission checks, subtasks, **recurring tasks**.
- `PriorityScoringService` - the smart 0-100 urgency score.
- `ProjectService`, `LabelService`, `CommentService`, `DashboardService`.

Methods are `@Transactional`; read-only ones use `readOnly = true`.
