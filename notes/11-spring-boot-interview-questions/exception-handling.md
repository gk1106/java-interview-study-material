# Exception Handling — @ControllerAdvice, ProblemDetail, Error Response Design

Good exception handling questions separate candidates who've only ever thrown a generic 500 from
those who've designed a real error-response contract that frontend/mobile/partner teams could
build against reliably. This file covers `@ControllerAdvice`/`@ExceptionHandler`,
`ResponseEntityExceptionHandler`, Spring 6's `ProblemDetail` (RFC 7807), consistent
error-response design, and exception translation from the persistence layer.

## @ControllerAdvice & @ExceptionHandler

- [Basic] What does `@ExceptionHandler` do, and what's the difference between putting it on a
  single controller versus inside a `@ControllerAdvice` class? → `@ExceptionHandler(SomeException.class)`
  on a method marks it as the handler for that exception type when thrown from a request-handling
  method. Defined **inside a single controller**, it only catches exceptions thrown by methods in
  that same controller. Defined inside a class annotated `@ControllerAdvice` (or
  `@RestControllerAdvice` for REST APIs, which adds `@ResponseBody`), it applies **globally**
  across every controller in the application (or a scoped subset via
  `@ControllerAdvice(basePackages = ...)` / `assignableTypes`), centralizing all exception-to-
  response translation logic in one place instead of duplicating it per controller. →
  Follow-up: *What's the difference between `@ControllerAdvice` and `@RestControllerAdvice`?*
  `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`, exactly parallel to how
  `@RestController` relates to `@Controller` — every handler method's return value is written
  directly to the response body rather than resolved as a view name.
- [Intermediate] If you have `@ExceptionHandler(RuntimeException.class)` and
  `@ExceptionHandler(IllegalArgumentException.class)` in the same `@ControllerAdvice`, and an
  `IllegalArgumentException` is thrown, which handler runs? → The **most specific** matching
  handler — Spring resolves exception handlers by walking the exception's class hierarchy and
  picking the handler registered for the closest matching type, so
  `IllegalArgumentException` (a subclass of `RuntimeException`) is handled by the
  `IllegalArgumentException` handler, not the broader `RuntimeException` one, even though both
  technically match. → Follow-up: *What happens if two `@ExceptionHandler` methods in the same
  class are registered for the exact same exception type?* Ambiguous mapping — Spring throws an
  `IllegalStateException` at startup (`Ambiguous @ExceptionHandler method mapped for
  [ExceptionType]`), since it can't deterministically choose between two equally specific handlers.
- [Intermediate] How do you return different HTTP status codes for different exception types from
  `@ExceptionHandler` methods? → Either annotate the handler method with
  `@ResponseStatus(HttpStatus.NOT_FOUND)` (status is fixed per handler, simple but inflexible), or
  — the more common and more flexible approach — have the handler method return
  `ResponseEntity<ErrorResponse>` and set the status explicitly per invocation
  (`ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)`), which lets the same handler
  method derive the status dynamically from the caught exception's own fields (e.g. a custom
  `BusinessException` carrying its own `HttpStatus`). → Follow-up: *Why might you design a base
  custom exception class (`ApiException`) that carries its own `HttpStatus` and error code field?*
  It centralizes the exception-to-status mapping at the point the exception is thrown/defined
  (self-documenting — `new ResourceNotFoundException(id)` inherently means 404) rather than
  scattering `if (e instanceof X) return 404; else if instanceof Y return 409...` logic inside a
  generic handler, and it scales cleanly as new domain exceptions are added without touching the
  central handler at all.

## ResponseEntityExceptionHandler & ProblemDetail

- [Intermediate] What is `ResponseEntityExceptionHandler`, and why would you extend it instead of
  writing exception handling from scratch? → It's Spring MVC's base class providing default
  `@ExceptionHandler` implementations for the framework's own common exceptions
  (`MethodArgumentNotValidException`, `HttpMessageNotReadableException`,
  `HttpRequestMethodNotSupportedException`, `NoHandlerFoundException`, etc.), each already mapped
  to a sensible default HTTP status. Extending it in your `@ControllerAdvice` lets you override
  just the specific `handleXxx` protected methods you need to customize (e.g. reshape the
  validation error response body) while inheriting correct default behavior for everything else,
  rather than reinventing handling for every framework exception type yourself. → Follow-up:
  *What method would you override to customize the response body for `@Valid` validation
  failures?* `handleMethodArgumentNotValid(...)` — override it to build your own error response
  shape (e.g. listing each field + message) instead of Spring's default body format.
- [Advanced] What is `ProblemDetail` (Spring Framework 6 / Boot 3+), and how does it relate to
  RFC 7807? → RFC 7807 defines a standard JSON media type (`application/problem+json`) for HTTP
  API error responses, with standard fields: `type` (a URI identifying the problem type),
  `title` (short human-readable summary), `status` (HTTP status code), `detail`
  (human-readable explanation specific to this occurrence), and `instance` (a URI identifying this
  specific occurrence) — plus extension members for domain-specific fields. Spring 6's
  `ProblemDetail` class is a built-in implementation of this standard, and
  `ResponseEntityExceptionHandler`'s default handlers were updated to return `ProblemDetail`
  responses out of the box, so you get RFC-7807-shaped errors without writing custom DTOs, and can
  extend it with `setProperty("errorCode", "...")` for additional fields. → Follow-up: *Why does
  standardizing on RFC 7807 matter for API consumers, beyond just "it's a standard"?* It gives
  every error response a **predictable, self-describing shape** regardless of which service or
  team produced it — client tooling, error-logging middleware, and API gateways can parse/handle
  errors generically across an entire microservice fleet instead of each service inventing its own
  ad-hoc error JSON shape that every consuming client has to special-case.
- [Intermediate] Why would a team choose to build its own custom `ErrorResponse` DTO instead of
  adopting `ProblemDetail` directly? → A few real reasons: (1) an existing API contract predates
  Spring 6/`ProblemDetail` and changing the error shape would be a breaking change for existing
  clients; (2) the team wants fields `ProblemDetail`'s standard shape doesn't naturally accommodate
  well without extension properties (e.g. a structured list of per-field validation errors as a
  first-class array, not a generic property bag); (3) organizational API standards mandate a
  specific shared error contract across services in a different format (common in larger orgs with
  a pre-existing API gateway/error-handling convention). In a greenfield Spring Boot 3+ service
  with no existing contract to preserve, `ProblemDetail` is usually the pragmatic default rather
  than reinventing the same fields. → Follow-up: *If you do build a custom error DTO, what fields
  should it always include at minimum for a production API?* Timestamp, HTTP status code, a stable
  machine-readable error code (not just the message — messages change wording, codes shouldn't), a
  human-readable message, the request path, and (critically for support/debugging) a
  **correlation/trace ID** so a reported error can be tied back to specific log lines/traces.

## Consistent Error-Response Design

- [Basic] Why is a consistent error-response shape across all endpoints important for a REST API? →
  Client code (frontend, mobile, partner integrations) needs to parse errors generically — if
  `POST /users` returns `{"error": "..."}`\ and `POST /orders` returns `{"message": "...",
  "code": 123}`, every client integration needs special-case parsing per endpoint, which is
  fragile and breaks silently when a new endpoint doesn't follow the (unwritten) convention. A
  single global `@RestControllerAdvice` enforcing one shape for every exception, across every
  controller, is what actually guarantees this consistency — relying on each controller author to
  remember the convention doesn't. → Follow-up: *Where's the best place to define/document this
  contract so frontend and backend teams stay in sync?* The OpenAPI/Swagger spec (schema for the
  standard error response, referenced from every endpoint's error responses) — generated from
  code via springdoc-openapi so it can't drift out of sync with the actual `@RestControllerAdvice`
  implementation.
- [Intermediate] What should a well-designed error response body include, and what should it
  deliberately **not** include? → Include: HTTP status, a stable error code, a clear message, the
  request path/timestamp, and a trace/correlation ID. Deliberately exclude: **stack traces** (leak
  internal class/package structure and library versions to a potential attacker — information
  disclosure), raw exception messages from lower layers (a raw `SQLException` message can leak
  table/column names, or database vendor and version), and any internal implementation detail an
  external client has no business seeing. This is exactly why blindly returning
  `exception.getMessage()` to the client for *every* exception type is a common security/hygiene
  mistake — it should be deliberately filtered to a small allow-list of exception types whose
  messages are already client-safe (typically your own custom domain exceptions with hand-written,
  reviewed messages), while unexpected/unknown exceptions get a generic "internal error, ref: <trace
  id>" message instead. → Follow-up: *How do you still get the full stack trace for debugging if
  you're not returning it to the client?* Log it server-side (with the same trace/correlation ID
  included in the response), at ERROR level, so it's fully available in your logging/observability
  stack (correlated by trace ID) without ever leaving the server boundary.
- [Advanced] How would you design error handling so that a client can reliably distinguish "retry
  this request" from "don't retry, this will never succeed" failures? → Map exceptions to HTTP
  status codes that already carry this semantic distinction, and document/enforce it consistently:
  5xx (500, 502, 503, 504) and 429 (Too Many Requests) signal transient/retryable conditions — a
  client with retry logic (ideally with exponential backoff + jitter) should retry these; 4xx
  (400, 404, 409, 422) other than 429 signal the request itself is invalid or will never succeed as
  sent — retrying identically will just fail again, the client needs to fix the request first. For
  429/503 specifically, include a `Retry-After` header telling the client how long to wait before
  retrying, rather than leaving it to guess a backoff strategy. Internally, this also means your
  exception hierarchy should distinguish "transient infrastructure failure" (map to 503/502, log
  as a warning, alert if sustained) from "genuine business rule violation" (map to 409/422, expected
  traffic, don't page anyone) — conflating the two (e.g. a downstream timeout and a validation
  failure both becoming a generic 500) breaks both the client's retry logic and your own alerting
  signal-to-noise ratio. → Follow-up: *Why is it dangerous to blindly retry a `POST` that failed
  with a timeout (client never got a response, but the server might have completed the operation)?*
  The server may have actually processed the request successfully before the response was lost
  (network blip after the write committed) — a naive retry can create a duplicate side effect
  (double-charge, duplicate order); this is exactly why idempotency keys (see
  `rest-and-validation.md`) matter specifically for retry-safety on non-idempotent operations.

## Exception Translation from the Persistence Layer

- [Basic] What is `DataAccessException`, and why does Spring wrap JDBC/JPA-specific exceptions in
  it? → `DataAccessException` (and its subclasses like `DataIntegrityViolationException`,
  `EmptyResultDataAccessException`, `OptimisticLockingFailureException`) is Spring's **unchecked**,
  technology-agnostic exception hierarchy that every Spring Data/JDBC access method throws instead
  of leaking the raw driver-specific checked exception (`SQLException`) or JPA-specific exception
  (`PersistenceException`, `jakarta.persistence.EntityNotFoundException`). This means your service
  layer can catch/handle data-access failures **without depending on which persistence technology
  is underneath** — swap JDBC for JPA, or one JPA provider for another, and the exception types
  your business logic catches don't need to change. → Follow-up: *Why is `DataAccessException`
  unchecked (a `RuntimeException`) rather than checked, unlike the raw `SQLException` it often
  wraps?* Spring's design philosophy: checked exceptions force every intermediate layer to declare
  or catch them even when they have no meaningful recovery action to take (a service method three
  layers up usually can't "recover" from a broken DB connection any more meaningfully than by
  propagating the failure) — unchecked exceptions let only the layers that actually *can* do
  something meaningful (retry, translate to an API error, roll back) choose to catch them, without
  polluting every method signature in between.
- [Intermediate] A unique-constraint violation at the DB level throws
  `DataIntegrityViolationException` — how would you translate that into a clean `409 Conflict` API
  response without leaking the raw SQL constraint name to the client? → Catch
  `DataIntegrityViolationException` in a `@RestControllerAdvice` handler (or at the service layer
  if you want to attach more context first, e.g. re-throwing your own
  `DuplicateResourceException` with the specific field that conflicted), map it to `409 Conflict`,
  and return a client-safe message like `"A user with this email already exists"` rather than the
  raw exception message (which often contains the literal DB constraint name, e.g.
  `"ux_users_email"`, and sometimes the offending value — both are implementation details that
  shouldn't leak, and constraint names especially reveal schema internals). → Follow-up: *What's
  the risk of trying to prevent duplicates purely with an application-level `existsByEmail()` check
  before insert, without a DB unique constraint as a backstop?* A race condition — two concurrent
  requests can both pass the `existsByEmail()` check (neither sees the other's row yet, since
  neither has committed) and both proceed to insert, creating a duplicate; the DB unique constraint
  is the only reliable enforcement point because it's atomic at the storage engine level, so the
  API-layer check should be treated as a fast-path UX improvement (nicer error before hitting the
  DB), never as the sole enforcement mechanism.
- [Advanced] Where should exception translation from the persistence layer to API-facing exceptions
  happen — in the repository, the service layer, or the `@RestControllerAdvice` — and why? →
  Generally: let Spring's own `DataAccessException` translation handle the JDBC/JPA-specific-to-
  generic-Spring-exception translation automatically (you get this for free with
  `@Repository`-annotated beans via `PersistenceExceptionTranslationPostProcessor`). Then, in the
  **service layer**, catch narrow, specific `DataAccessException` subtypes where the service has
  enough business context to translate them into a *meaningful domain exception*
  (`DataIntegrityViolationException` on an email insert → `EmailAlreadyRegisteredException`,
  carrying the actual email that conflicted) — this is the layer that knows *why* the constraint
  exists semantically, not just that a constraint fired. The `@RestControllerAdvice` then only
  needs to map that clean **domain** exception to an HTTP status/response shape — it shouldn't be
  doing persistence-specific exception inspection (checking constraint names, SQL states) itself,
  since that couples your global error handler to persistence implementation details it has no
  business knowing about. → Follow-up: *What goes wrong if you instead do all persistence-exception
  interpretation directly inside `@RestControllerAdvice`?* It becomes a dumping ground that has to
  understand every table/constraint's meaning across the entire application to produce good error
  messages, tightly coupling a cross-cutting, global component to persistence-layer specifics that
  change per-feature — every new unique constraint anywhere in the app requires editing the one
  shared global handler, instead of being a local concern of the service that owns that entity.

---

**File question count: 15** ([Basic] 3, [Intermediate] 7, [Advanced] 5)
