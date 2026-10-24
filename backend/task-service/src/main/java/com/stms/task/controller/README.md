# controller/

REST endpoints (`@RestController`). They only: read the request, call a service, return a DTO.

| Controller | Base path |
|---|---|
| `AuthController` | `/api/auth` |
| `ProjectController` | `/api/projects` |
| `TaskController` | `/api/tasks` |
| `CommentController` | `/api/tasks/{id}/comments`, `/api/comments/{id}` |
| `LabelController` | `/api/labels` |
| `DashboardController` | `/api/dashboard` |
| `UserController` | `/api/users`, `/api/admin/users` |
