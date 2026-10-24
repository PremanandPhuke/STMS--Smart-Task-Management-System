# auth/

- `AuthContext.tsx` - holds the logged-in user; exposes `login`, `register`, `logout` through `useAuth()`.
- `ProtectedRoute.tsx` - redirects to `/login` when not signed in (and blocks non-admins from admin pages).
