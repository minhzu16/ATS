# ADR-0001: Stateless JWT Authentication with Refresh Token Rotation

## Status
Accepted

## Context
The ATS frontend (React SPA) communicates with the Spring Boot REST API (`/api/v1/**`). We need stateless horizontal scalability for API requests while still supporting session revocation, account locking, and brute-force protection on login.

## Decision
1. **Stateless Access Tokens**: `SecurityConfig` configures `SessionCreationPolicy.STATELESS` and inserts `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`. Short-lived JWT access tokens (`jwt.access-token-expiration-ms`) carry user identity and role claims.
2. **Database-Backed Refresh Tokens**: `RefreshTokenServiceImpl` persists refresh tokens in MySQL (`refresh_tokens`) so logout and account-lock events can revoke active refresh tokens immediately.
3. **Auth Rate Limiting**: `AuthRateLimitFilter` enforces per-client request limits (`app.security.auth-rate-limit.per-minute`) on authentication endpoints to mitigate credential stuffing.

## Consequences
- API servers remain stateless for standard requests without sticky sessions.
- Compromised or locked accounts can be blocked at refresh time and on subsequent user lookups.
- Security headers (`X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy`, `Content-Security-Policy`) and explicit CORS origins (`CorsConfig`) are enforced centrally.
