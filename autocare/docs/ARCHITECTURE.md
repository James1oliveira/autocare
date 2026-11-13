# Architecture notes

## Request flow

1. The React app sends every request to the **API gateway** (`:8080`), adding `Authorization: Bearer <token>` once the user is logged in.
2. `JwtAuthenticationFilter` (a Spring Cloud Gateway `GlobalFilter`) runs first:
   - removes `X-User-Id`, `X-User-Role` and `X-User-Name` from the incoming request
   - validates the JWT signature and expiry with the shared secret
   - adds those three headers again from the verified token
   - returns `401` for protected paths without a valid token
3. `AiRateLimitFilter` limits `/api/ai/diagnose` per user (or per IP for visitors).
4. The request is routed to the matching service by path.
5. Services trust the `X-User-*` headers because only the gateway can reach them (inside the Docker network they have no public ports).

## Services

### auth-service
- `User` entity in table `app_users` (`user` is a reserved word in PostgreSQL).
- `JwtService` signs tokens with claims `sub` (user id), `name`, `email`, `role`.
- `LoginAttemptService` keeps failed attempts in memory and locks an email for 5 minutes after 5 failures.
- `DemoUserSeeder` creates the demo customer, mechanic and admin.

### booking-service
- `ServiceItem` (catalog) and `Booking` entities.
- `BookingStatus` is a small state machine: `BOOKED → IN_PROGRESS → READY → COLLECTED`, or `BOOKED → CANCELLED`. Invalid moves return `409 Conflict`.
- `CurrentUserResolver` turns the gateway headers into a `CurrentUser` controller argument, so controllers stay clean.
- `DataSeeder` keeps the price list up to date on every start-up and adds example jobs to an empty database.

### ai-service (Spring AI)
Retrieval-augmented generation in three steps:

1. **Knowledge base:** `src/main/resources/knowledge/workshop-guide.md` is split into sections, each with symptoms, causes, advice and a `Service code`.
2. **Retrieval:**
   - AI mode: each section is embedded once with `text-embedding-3-small` (`EmbeddingModel`). The question is embedded and compared with cosine similarity, and the top 3 sections are used.
   - Offline mode: keyword matching with light stemming (so "braking" matches "brakes"), with title words counting double.
3. **Generation:** `ChatClient` sends a system prompt with strict rules, plus the retrieved notes, the live price list from booking-service, and the customer's message. The model may only use that information, so it can't invent prices.

Suggestions shown to the customer come from the service codes of the retrieved sections, not from the model's text, so the "Book this" buttons always point at real services.

If the model call fails (no key, network error, quota), the service answers in offline mode instead of returning an error.

### api-gateway
- Routes: `/api/auth/**` → auth, `/api/bookings/**` and `/api/catalog/**` → booking, `/api/ai/**` → ai.
- Global CORS configuration for the frontend origin.
- `SlidingWindowRateLimiter` is in memory. For multiple gateway instances it would move to Redis.

## Data

| Database | Owner | Tables |
|---|---|---|
| `auth_db` | auth-service | `app_users` |
| `booking_db` | booking-service | `service_items`, `bookings` |

Locally each service falls back to an in-memory H2 database. With Docker Compose they use PostgreSQL.

## Design decisions

- **Gateway-level authentication:** tokens are checked once at the edge instead of in every service, and services get a simple, trusted identity.
- **Shared HMAC secret** instead of public/private keys: simpler for a small system. The next step would be RS256 with auth-service publishing a JWKS endpoint.
- **Suggestions from retrieval, not from the model:** keeps booking links reliable even if the model phrases things differently.
- **Graceful degradation:** the assistant keeps working without an AI key or without booking-service (it just leaves out prices).
