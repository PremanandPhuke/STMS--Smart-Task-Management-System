# security/

Everything JWT-related.

- `JwtService` - creates and validates access (30 min) and refresh (7 days) tokens.
- `JwtAuthFilter` - runs on every request, reads `Authorization: Bearer ...`, sets the logged-in user.
- `RateLimitFilter` - max 10 login/register attempts per minute per IP (brute-force protection).
- `AppUserDetails` / `CustomUserDetailsService` - bridge between our `User` entity and Spring Security.
