# Testing Spring Boot Applications — Slice Tests, MockMvc, Testcontainers

Testing questions at this level separate candidates who write `@SpringBootTest` for everything
(slow suites, 90-second CI runs for a one-line change) from those who understand the testing
pyramid and pick the narrowest test type that actually proves the thing they're testing. This file
covers `@SpringBootTest` vs slice tests (`@WebMvcTest`, `@DataJpaTest`, `@JsonTest`) vs plain unit
tests with Mockito, MockMvc for controller testing, Testcontainers, and test data setup strategies.

## Test Levels: Unit vs Slice vs Full Context

- [Basic] What's the difference between a plain Mockito unit test and a `@SpringBootTest`? → A
  plain unit test (`@ExtendWith(MockitoExtension.class)`, no Spring involved at all) instantiates
  the class under test directly with `new`, injecting **mocks** for its dependencies
  (`@Mock`/`@InjectMocks`) — it runs in milliseconds, tests pure business logic in isolation, and
  never touches a real Spring context, database, or web server. `@SpringBootTest` boots the
  **entire** application context (every bean, every auto-configuration, optionally a real embedded
  web server with `webEnvironment = RANDOM_PORT`) — genuinely testing wiring and integration, but
  taking seconds per test class to start the context (amortized across tests in the same class, but
  still far slower than a unit test, and multiplies badly across a large suite if context caching
  isn't working — see below). → Follow-up: *For a service class with straightforward business
  logic and no Spring-specific behavior (no `@Transactional`, no bean lifecycle dependency), which
  should you default to, and why?* A plain Mockito unit test — if the class doesn't actually
  exercise anything Spring-specific, paying the cost of a full context (or even a slice context) to
  test it buys nothing; the fastest, most isolated test that actually proves correctness is the
  right default, reserving Spring test infrastructure for what genuinely needs it (wiring,
  transactional behavior, HTTP binding).
- [Intermediate] What is a Spring "slice test," and why does `@WebMvcTest` load faster than
  `@SpringBootTest` for testing a controller? → A slice test loads **only the subset of the
  application context relevant to one architectural layer**, auto-configuring just what that layer
  needs and explicitly excluding everything else. `@WebMvcTest(UserController.class)` loads the web
  layer (the specified controller, `@ControllerAdvice` beans, Jackson message converters, Spring
  Security's web filters if present) but explicitly does **not** load `@Service`/`@Repository`
  beans or set up a real database — any injected service dependency must be supplied as a
  `@MockBean`, and you're testing purely "does this controller handle requests/responses,
  validation, and error mapping correctly," not the business logic behind it (which is exactly
  what a unit test on the service itself covers separately). This narrower context is both much
  faster to start and forces a cleaner separation of what each test is actually verifying. →
  Follow-up: *Name two other common slice test annotations and what each scopes to.*
  `@DataJpaTest` — loads only JPA/Hibernate configuration, an embedded/test `DataSource`, and
  repository beans (no web layer, no service layer); by default runs each test wrapped in a
  transaction that's rolled back afterward for isolation. `@JsonTest` — loads only Jackson
  (de)serialization configuration and provides `JacksonTester`/`GsonTester` helpers, for testing
  that a DTO serializes/deserializes to the expected JSON shape without any web or persistence
  layer involved at all.
- [Intermediate] What is Spring's **test context caching**, and why does getting it wrong silently
  make a whole test suite dramatically slower? → Spring's test framework caches a started
  `ApplicationContext` **keyed by its exact configuration** (the set of config classes, active
  profiles, property overrides, mocked beans, etc.) across test classes — if two test classes
  request the *identical* context configuration, the second one reuses the already-started context
  instead of paying the startup cost again. This is why, in a large `@SpringBootTest` suite,
  seemingly small differences between test classes (a different `@ActiveProfiles`, a different set
  of `@MockBean`s, a different `@TestPropertySource` value) **each create a distinct cache entry**,
  forcing Spring to start an entirely new context per distinct configuration — a suite with 50 test
  classes each using a slightly different `@MockBean` combination can end up starting 50 separate
  full contexts instead of reusing 1-2, turning a suite that could run in under a minute into one
  that takes many minutes, purely from avoidable context restarts. → Follow-up: *What's a concrete
  practice that maximizes context cache hits across a large test suite?* Standardize on a small,
  shared set of base test configuration (a common `@SpringBootTest` base class or shared test
  configuration annotation used consistently across test classes) rather than each test class
  declaring its own slightly-different bespoke set of mocks/profiles/properties — the more test
  classes share an *identical* configuration fingerprint, the more contexts get reused instead of
  rebuilt.

## MockMvc for Controller Testing

- [Basic] What is `MockMvc`, and how does testing with it differ from spinning up a real embedded
  server and using `TestRestTemplate`/`WebTestClient`? → `MockMvc` simulates the Spring MVC request
  dispatch process **without a real HTTP server or network stack** — it constructs a mock
  `HttpServletRequest`, runs it through the actual `DispatcherServlet` machinery (handler mapping,
  argument resolution, your real controller code, `@ExceptionHandler`s, view resolution/JSON
  serialization), and captures a mock `HttpServletResponse`, all in-process. This is much faster
  than a real server round-trip (no actual sockets/ports involved) while still exercising the real
  Spring MVC request-handling pipeline (unlike calling the controller method directly, which
  would skip argument binding, validation, and content negotiation entirely). A real embedded
  server + `TestRestTemplate`/`WebTestClient` genuinely sends HTTP over a real (loopback) socket —
  slower, but it's the only way to catch issues that are specifically about the real server/
  serialization stack (e.g. certain filter or servlet-container-specific behavior) that `MockMvc`'s
  simulation might not perfectly replicate. → Follow-up: *For most controller tests, which is the
  pragmatic default, and why?* `MockMvc` — it exercises everything that actually matters for
  verifying controller behavior (routing, binding, validation, serialization, exception handling)
  at a fraction of the cost of a real server round-trip; a real embedded server is reserved for
  genuinely needing to test actual network-level or container-specific behavior.
- [Intermediate] Write (describe) a `MockMvc` test verifying a POST endpoint returns 400 with a
  structured error body when a required field is missing, and explain what each part of the test
  actually exercises. → 
  ```java
  mockMvc.perform(post("/api/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"email\": \"\"}"))          // missing/blank required "name" field
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors[0].field").value("name"))
      .andExpect(jsonPath("$.errors[0].message").exists());
  ```
  This exercises the **full request pipeline** for that endpoint: JSON deserialization into the
  request DTO (`HttpMessageConverter`), `@Valid` triggering Bean Validation against the DTO's
  constraints, the resulting `MethodArgumentNotValidException` being caught by your
  `@RestControllerAdvice`'s handler, and that handler's actual response **shape** (via
  `jsonPath` assertions on the real serialized JSON) — it proves the validation-to-error-response
  contract end to end, not just that a constraint annotation exists on the DTO. →
  Follow-up: *Why use `jsonPath` assertions on specific fields rather than asserting the entire
  response body string matches exactly?* Asserting the full body string is brittle — it breaks on
  any unrelated field addition/reordering/formatting change, even ones that don't affect the
  actual contract being tested; `jsonPath` lets the test assert only the specific fields it cares
  about, making the test resilient to unrelated response-shape evolution while still catching
  genuine regressions in the fields that matter.
- [Intermediate] How do you test a Spring Security-protected endpoint with `MockMvc` without going
  through a real login flow? → `spring-security-test`'s `@WithMockUser(username = "alice", roles =
  {"ADMIN"})` annotation on a test method (or `mockMvc.perform(...).with(user("alice").roles("ADMIN"))`
  per-request) injects a pre-authenticated `Authentication` directly into the test's
  `SecurityContext`, skipping the actual credential-verification/login flow entirely while still
  exercising the real **authorization** logic (`@PreAuthorize`, `.hasRole(...)` URL rules) against
  that simulated principal — letting you test "does an ADMIN successfully reach this endpoint" and
  "does a non-ADMIN get 403" without needing a real user store, password check, or token issuance
  in the test at all. → Follow-up: *What does `@WithAnonymousUser` let you specifically verify that
  `@WithMockUser` doesn't?* That an endpoint correctly rejects a genuinely **unauthenticated**
  request (401, not 403) — useful for explicitly testing the "no credentials at all" case
  separately from the "authenticated but insufficient role" case that `@WithMockUser` with the
  wrong role tests.

## Slice Tests in Depth

- [Intermediate] What does `@DataJpaTest` set up by default, and why does it replace your real
  production `DataSource` unless configured otherwise? → By default, `@DataJpaTest` auto-configures
  an **in-memory embedded database** (H2, if on the classpath) instead of your real configured
  `DataSource`, wraps each test method in a transaction that's **rolled back** after the test
  completes (so tests don't pollute each other's data or require manual cleanup), and scans/loads
  only JPA entities and Spring Data repositories — no service or web layer. This default exists
  because repository tests should be fast and fully isolated/repeatable, and an in-memory DB
  starts near-instantly with no external infrastructure dependency. → Follow-up: *What's the real
  risk of testing repository queries against H2 when production runs PostgreSQL, and how do you
  fix it?* SQL dialect differences (a query using a PostgreSQL-specific function, JSON column type,
  or subtly different type-coercion/date-handling behavior) can pass against H2 in tests and then
  **fail or behave differently in production** — a classic "works in CI, breaks in prod" gap. The
  fix is `@AutoConfigureTestDatabase(replace = Replace.NONE)` combined with **Testcontainers**
  spinning up a real PostgreSQL container for the test, so `@DataJpaTest` runs against the actual
  production database engine instead of an H2 stand-in (see the Testcontainers section below).
- [Intermediate] `@DataJpaTest` rolls back each test's transaction automatically — what's a subtle
  bug this can hide that wouldn't be caught until production? → Because each test transaction
  rolls back, tests never actually exercise a **commit**, meaning any behavior that only manifests
  at commit time — a `@PrePersist`/`@PreUpdate` callback quirk, a DB trigger, a deferred constraint
  check, or (more commonly) a bug related to Hibernate's **flush timing** versus your query
  expectations (e.g. querying for an entity you just saved but haven't explicitly flushed, which
  might still work inside the test's uncommitted transaction due to the persistence context's
  first-level cache, but represents an ordering assumption that a real, separate transaction in
  production wouldn't share) — can pass in tests but misbehave in production, where actual commits
  happen and separate transactions can't see each other's uncommitted first-level-cache state the
  way a single rolled-back test transaction implicitly can. → Follow-up: *How would you
  specifically test that a repository method behaves correctly across a real commit boundary
  (e.g. testing an actual unique-constraint violation)?* Explicitly call
  `entityManager.flush()` (or use `TestEntityManager.flush()` in `@DataJpaTest`, which gives direct
  access to flush without a full commit) to force Hibernate to send the SQL to the database and
  surface constraint violations immediately, rather than relying on the test's implicit rollback
  behavior to mask timing-dependent issues.
- [Basic] What does `@WebMvcTest` NOT load, and what's the consequence if your controller has an
  `@Autowired` field for a `@Service` bean? → It does not load `@Service`, `@Repository`, or
  `@Component` beans outside the web layer, nor does it start a real database. If your controller
  depends on a `@Service` and that dependency isn't supplied as a `@MockBean` in the test, context
  startup fails with a "no qualifying bean" error, since the real service (and its own transitive
  dependencies, all the way down to the database) was never loaded and Spring can't satisfy the
  autowiring. → Follow-up: *What's the fix, and what does it let you control that a real service
  wouldn't?* Declare `@MockBean private UserService userService;` in the test class, then stub its
  behavior per test (`when(userService.findById(1L)).thenReturn(...)`) — this lets you precisely
  control what the service layer returns (including simulating error conditions like a thrown
  exception) without needing real business logic or database state to produce that specific
  scenario, keeping the controller test focused purely on the web layer's behavior.

## Testcontainers

- [Basic] What problem does Testcontainers solve for integration testing, and why is it preferred
  over an in-memory database like H2 for testing persistence logic? → Testcontainers spins up
  **real** Dockerized instances of your actual production dependencies (PostgreSQL, Kafka, Redis,
  etc.) programmatically for the duration of a test run, then tears them down automatically — this
  means integration tests run against the **exact same database engine, version, and SQL dialect**
  as production, instead of a substitute like H2 that only approximates it. This directly closes
  the "works in CI against H2, breaks in prod against PostgreSQL" gap described above (dialect
  differences, vendor-specific SQL functions, JSON column types, case-sensitivity behavior, etc.) —
  the trade-off is slower test startup (pulling/starting a real container) versus H2's near-instant
  in-memory startup. → Follow-up: *What's a common strategy to keep Testcontainers-based test
  suites fast despite the container startup cost?* Reuse a **single container instance across the
  whole test suite** rather than starting a fresh one per test class (a static/shared container, or
  Testcontainers' `withReuse(true)` support) — paying the startup cost once for the whole run
  instead of once per test class, combined with per-test data cleanup (e.g. `@Sql` scripts or
  transactional rollback) instead of a full container restart for isolation between tests.
- [Intermediate] How does a `@SpringBootTest` combined with Testcontainers typically wire the test
  database connection details (host, mapped port) into Spring's `DataSource` configuration? → The
  modern approach uses `@ServiceConnection` (Spring Boot 3.1+) on a static Testcontainers
  `@Container` field — Spring Boot automatically detects the container type (e.g.
  `PostgreSQLContainer`) and auto-configures the matching `DataSource` connection properties (URL,
  username, password, using the container's dynamically-assigned host port) with no manual
  property wiring needed at all. The older, still-common pattern uses
  `@DynamicPropertySource` — a static method that registers `spring.datasource.url`,
  `.username`, `.password` dynamically at context-refresh time, reading them off the started
  container's `getJdbcUrl()`/etc., since the container's mapped port isn't known until it's
  actually running (unlike a static `application-test.yml` value, which can't express "whatever
  port Docker happened to assign this run"). → Follow-up: *Why can't the container's connection
  details just be hard-coded in `application-test.yml`?* Testcontainers maps the container's
  internal port to a **dynamically-assigned free host port** at startup (to avoid port conflicts
  between parallel test runs / other services using the same default port), so the actual port
  isn't known until the container has actually started — it must be read at runtime and injected
  into Spring's property sources, not hard-coded ahead of time.
- [Advanced] Your team wants integration tests against a real PostgreSQL and a real Kafka broker
  for testing an event-driven consumer end to end, but the CI pipeline is now noticeably slower.
  How would you structure the test suite to balance realism against feedback speed? → Follow the
  testing pyramid deliberately rather than making everything a full Testcontainers integration
  test: keep the **bulk** of tests as fast unit tests (business/mapping logic, mocked
  dependencies) and slice tests (`@DataJpaTest` even against H2 is often fine for simple query
  correctness that doesn't depend on PostgreSQL-specific behavior) — reserve full Testcontainers-
  backed integration tests (real Postgres + real Kafka) for a **smaller** set of true end-to-end
  scenarios that specifically need to prove the real infrastructure interaction works (message
  production → consumption → persisted side effect, or a genuinely dialect-sensitive query).
  Structurally: separate these into distinct test source sets/Maven profiles (e.g. `mvn test` runs
  fast unit+slice tests on every commit/PR; a separate `mvn verify` or a dedicated CI stage runs the
  Testcontainers-based integration suite, either on every PR if the team can tolerate the time cost,
  or gated to merge-to-main/nightly if not) so the fast feedback loop for most changes isn't
  penalized by the slower suite's cost, while the slower suite still runs regularly enough to catch
  real integration regressions before they reach production. → Follow-up: *Why is it a mistake to
  respond to "CI got slow" by just deleting the Testcontainers tests instead of restructuring
  when they run?* Those tests exist specifically to catch the class of bug (real dialect/broker
  behavior differences) that faster unit/mocked tests structurally cannot catch — removing them
  reopens exactly the gap they were added to close; the fix for slow feedback is tiering when
  expensive-but-valuable tests run, not eliminating the coverage they uniquely provide.

## Test Data Setup Strategies

- [Intermediate] Compare three common strategies for setting up test data in integration tests:
  `@Sql` scripts, a test data builder/factory pattern, and relying on the application's own
  service-layer calls to create data. → **`@Sql` scripts** (raw SQL run before/after a test method)
  are explicit and fast (no framework overhead), but become a maintenance burden as schema evolves
  — every column addition/rename potentially requires updating scattered SQL files, and they can
  drift out of sync with the actual entity mappings. **Test data builder/factory pattern**
  (a fluent builder like `UserTestDataBuilder.aUser().withEmail(...).build()`, or a library like
  `instancio`/`easy-random` for filling irrelevant fields with sensible defaults) keeps test data
  construction in Java, refactor-safe (renaming a field via IDE refactoring updates the builder
  too), and expressive about which fields actually matter for a given test versus which are
  incidental defaults. **Relying on real service-layer calls** to create prerequisite data
  (e.g. calling the real `UserService.createUser(...)` to set up a user needed by an order test)
  exercises real business logic/validation as a side effect of setup, which can catch real bugs but
  also means a test can fail for reasons unrelated to what it's actually testing (a bug in
  `createUser` breaks every test that uses it for setup), and is slower than direct
  repository/builder-based insertion. → Follow-up: *Which would you reach for by default for most
  integration tests, and why?* A builder/factory pattern for direct data setup — it's fast,
  refactor-safe, and keeps each test's setup focused on exactly the data shape relevant to that
  test; reserve "set up via real service calls" specifically for tests that are deliberately testing
  a multi-step workflow spanning several service calls, not as the default way to get a row into a
  table for an unrelated test.
- [Advanced] Integration tests sharing one Testcontainers database instance across a whole suite
  start failing intermittently when run in parallel — likely cause and fix? → Parallel test
  execution against a **shared** database instance means tests can interfere with each other's
  data: test A's inserted rows can be visible to test B's query (making an assertion like "there are
  exactly 3 users" fail depending on execution order/timing), or two tests operating on
  overlapping data can race. Fixes: (1) **isolate data per test** — wrap each test in a transaction
  rolled back afterward (works well for `@DataJpaTest`-style repository tests, but doesn't
  naturally compose with genuinely async/multi-threaded scenarios like a Kafka consumer test, since
  the consumer thread operates outside the test's own transaction); (2) **isolate by unique keys**
  — generate unique-per-test identifiers (a UUID-suffixed email/username) so tests never collide on
  the same rows even when run concurrently against shared data; (3) **isolate the schema/database
  per test class** — a separate schema or database per test class (still against the same
  Testcontainers instance, avoiding the startup cost of a fresh container) so tests in different
  classes can't see each other's data at all, while tests within the same class still share
  (managed via option 1 or 2); (4) as a last resort, **disable parallel execution** for the specific
  test classes that can't be cleanly isolated, accepting the slower sequential run for just that
  subset. → Follow-up: *Why is transaction-rollback-per-test insufficient for testing an
  asynchronous Kafka consumer specifically?* The consumer processes the message on a **separate
  thread** (the Kafka listener container's own thread), entirely outside the test method's own
  transaction — the consumer's DB writes commit independently of whatever transaction the test
  method itself might be wrapped in, so a rollback-per-test strategy doesn't clean up what the
  consumer thread wrote; such tests generally need explicit data cleanup (e.g. `@AfterEach`
  deleting inserted rows, or unique-per-test keys) rather than relying on transactional rollback.

---

**File question count: 15** ([Basic] 3, [Intermediate] 9, [Advanced] 3)
