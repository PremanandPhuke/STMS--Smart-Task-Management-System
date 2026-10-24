# api/

All communication with the backend.

- `types.ts` - TypeScript types that mirror the backend DTOs.
- `client.ts` - the axios instance: adds the JWT to requests and silently refreshes it on a 401.
- `services.ts` - one small function per endpoint (`tasksApi.list`, `projectsApi.create`, ...).
