# REST APIs & Validation — Controllers, Binding, Bean Validation, Versioning

REST questions at the 3+ YOE level go past "what's `@GetMapping`" into request/response binding
mechanics, content negotiation, validation groups, and API design trade-offs (versioning,
idempotency, pagination) you'd actually be expected to weigh in on for a production API design
review. This file covers `@RestController`/`@RequestMapping`, binding, content negotiation, Bean
Validation, versioning, idempotency, and pagination/HATEOAS basics.

## Controllers & Request Mapping

- [Basic] What does `@RestController` do differently from `@Controller`? → `@RestController` is
  `@Controller` + `@ResponseBody` combined — every handler method's return value is serialized
  directly into the HTTP response body (typically as JSON via Jackson) instead of being resolved as
  a view name for a template engine to render. `@Controller` alone is for traditional MVC apps
  returning view names (Thymeleaf/JSP); mixing them up (`@Controller` without `@ResponseBody`)
  causes Spring to try resolving your JSON string as a view name, throwing a 404 for a missing
  template. → Follow-up: *Can a single `@Controller` class have some methods return views and
  others return JSON?* Yes — mark individual methods `@ResponseBody` in an otherwise plain
  `@Controller`, useful in hybrid apps serving both server-rendered pages and a JSON API from the
  same codebase.
- [Basic] What's the difference between `@PathVariable` and `@RequestParam`? → `@PathVariable`
  extracts a segment from the URI path itself (`/users/{id}` → `@PathVariable Long id`), used for
  identifying a specific resource; `@RequestParam` extracts a query string parameter
  (`/users?status=active` → `@RequestParam String status`), used for filtering, pagination, or
  optional modifiers. → Follow-up: *How do you make a `@RequestParam` optional with a default?*
  `@RequestParam(required = false, defaultValue = "10") int pageSize` — if the client omits it,
  Spring binds the default instead of throwing `MissingServletRequestParameterException`.
- [Intermediate] How does Spring MVC pick which controller method handles a given request when
  multiple methods share the same path but differ in HTTP method, params, or headers? →
  `RequestMappingHandlerMapping` builds a mapping from a **composite key**
  (path pattern + HTTP method + optional `params`/`headers`/`consumes`/`produces` conditions) to
  the handler method at startup, then at request time matches the incoming request against all
  registered mappings, picking the **most specific** match (e.g. an exact path beats a wildcard
  path, a request matching a `consumes = "application/json"` condition beats one without it) — if
  two mappings are equally specific and both match, it's ambiguous and throws
  `IllegalStateException` at startup (`Ambiguous mapping`) rather than picking arbitrarily at
  runtime. → Follow-up: *How would you route the same path to two different methods based on the
  `Accept` header (e.g. XML vs JSON clients)?* Use `produces = "application/json"` and
  `produces = "application/xml"` on two separate handler methods for the same path — Spring content-
  negotiates based on the request's `Accept` header to pick the matching method.

## Request/Response Binding & Content Negotiation

- [Basic] How does `@RequestBody` deserialize an incoming JSON payload into a Java object? →
  Spring MVC delegates to an `HttpMessageConverter` registered for the request's `Content-Type` —
  for JSON, that's `MappingJackson2HttpMessageConverter`, which uses Jackson's `ObjectMapper` to
  deserialize the raw body into the target type via reflection (matching JSON field names to Java
  property names/setters, or constructor params for records). → Follow-up: *What HTTP status does
  Spring return if the JSON body doesn't match the target type at all (e.g. a string where a number
  is expected)?* 400 Bad Request, via `HttpMessageNotReadableException` — assuming you haven't
  overridden the default exception handling, Spring Boot's default error handling maps that
  exception to a 400 automatically.
- [Intermediate] What is content negotiation, and what are the mechanisms Spring MVC supports for
  it? → Content negotiation is choosing the response representation format (JSON, XML, etc.) based
  on what the client can accept. Spring MVC's `ContentNegotiationManager` checks, in order by
  default: (1) a URL path extension like `.json`/`.xml` (deprecated/disabled by default since it's
  a security/ambiguity risk), (2) a query parameter (`?format=json`, off by default), (3) the
  **`Accept` HTTP header** (the standard, recommended mechanism) — matching it against the
  `produces` conditions on candidate handler methods and the `HttpMessageConverter`s available on
  the classpath. → Follow-up: *If a client sends `Accept: application/xml` but only
  `jackson-databind` (JSON) is on the classpath, what happens?* Spring can't find a converter that
  produces XML, so it returns `406 Not Acceptable` — this is a common "works locally, fails after a
  dependency was removed" bug when `jackson-dataformat-xml` gets accidentally dropped.
- [Intermediate] What's the difference between `ResponseEntity<T>` and just returning `T` directly
  from a controller method? → Returning `T` directly always responds with `200 OK` (or `204` for
  `void`) and lets Spring set headers automatically — fine for the simple "success" case.
  `ResponseEntity<T>` gives full control over the **status code**, headers, and body together in one
  return value — essential whenever the response varies (`201 Created` with a `Location` header
  after a POST, `404` when a resource isn't found, `202 Accepted` for an async operation), which is
  most real endpoints beyond trivial GETs. → Follow-up: *How would you return a `201 Created` with a
  `Location` header pointing at the new resource's URI after a POST?*
  `return ResponseEntity.created(URI.create("/users/" + saved.getId())).body(saved);` —
  `ResponseEntity.created(uri)` sets both the status and the `Location` header in one call.

## Bean Validation

- [Basic] How does `@Valid` on a `@RequestBody` parameter trigger validation, and what happens on
  failure? → `@Valid` (or `@Validated` for group support) tells Spring's argument resolver to run
  Bean Validation (Hibernate Validator, the JSR-380 reference implementation) against the
  deserialized object's constraint annotations (`@NotNull`, `@Size`, `@Email`, etc.) **after**
  JSON deserialization succeeds, before the handler method body runs. On any constraint violation,
  Spring throws `MethodArgumentNotValidException`, which Spring Boot's default error handling maps
  to `400 Bad Request` with a body listing the field errors — unless you've defined a
  `@ExceptionHandler` for it to customize the response shape. → Follow-up: *Does `@Valid` validate
  nested objects automatically, e.g. an `Address` field inside a `User` DTO?* Only if the nested
  field is **also** annotated `@Valid` on the containing class (`@Valid private Address address;`)
  — Bean Validation does not cascade into nested objects by default; forgetting the nested `@Valid`
  is a common bug where sub-object fields silently skip validation.
- [Intermediate] What's the difference between `@NotNull`, `@NotEmpty`, and `@NotBlank`? →
  `@NotNull` only rejects `null` — an empty string `""` or empty list `[]` passes.
  `@NotEmpty` rejects `null` **and** empty (`""`, empty collection/array/map) but allows
  whitespace-only strings (`"   "` passes). `@NotBlank` (strings only) additionally rejects
  whitespace-only strings — it trims before checking. For a "required, meaningful" string field
  (e.g. a username), `@NotBlank` is almost always the right choice over `@NotNull`. →
  Follow-up: *Which of these three works on a `List<String>` field?* Only `@NotNull` and
  `@NotEmpty` — `@NotBlank` is string-specific (it calls `.trim()` internally, which doesn't apply
  to collections).
- [Intermediate] How do you write a custom Bean Validation constraint (e.g. validating a phone
  number format specific to your domain)? → Define an annotation meta-annotated with
  `@Constraint(validatedBy = PhoneNumberValidator.class)`, plus the standard
  `@Target`/`@Retention`/`message`/`groups`/`payload` boilerplate Bean Validation requires; then
  implement `ConstraintValidator<PhoneNumber, String>` with an `isValid(String value,
  ConstraintValidatorContext ctx)` method containing the actual regex/logic — optionally building a
  custom violation message via `ctx.buildConstraintViolationWithTemplate(...)` for field-specific
  errors. Apply `@PhoneNumber` on the DTO field like any built-in constraint. → Follow-up: *Why
  would you prefer a custom constraint annotation over just writing an `if` check manually inside
  the controller/service?* Declarative, reusable across every DTO that needs it, automatically
  participates in the same `@Valid` validation pass (single consistent error-response shape for all
  validation failures, not a special-cased manual check with a different error format), and is
  independently unit-testable.
- [Advanced] What are Bean Validation groups, and when would you actually need them (give a
  concrete scenario)? → Groups let the *same* DTO apply different subsets of constraints depending
  on context, via `@NotNull(groups = OnCreate.class)` and validating with
  `@Validated(OnCreate.class)` at a specific endpoint. Concrete scenario: a `UserDto` used for both
  `POST /users` (create — `id` must be null/absent, `password` required) and
  `PUT /users/{id}` (update — `id` required/must match the path, `password` optional since you're
  not necessarily changing it). Without groups you'd need two near-duplicate DTOs or manual
  validation logic scattered in each handler; groups let one DTO class carry all constraints, each
  tagged with which operation(s) it applies to, and the endpoint declares which group(s) to enforce.
  → Follow-up: *What's a simpler alternative to validation groups that many teams prefer, and why?*
  Separate DTOs per operation (`CreateUserRequest`, `UpdateUserRequest`) — a bit more boilerplate,
  but each class's constraints are unconditionally true for that class (no group bookkeeping to get
  wrong), and it also naturally prevents accidentally exposing/accepting fields (like `id` or
  `role`) that shouldn't be settable on create — many teams find that clarity worth the extra
  classes over the flexibility of shared-DTO validation groups.

## API Design: Versioning, Idempotency, Pagination

- [Intermediate] Compare the common REST API versioning strategies — URI versioning, header
  versioning, and content-type (media-type) versioning. → **URI versioning** (`/api/v1/users`) is
  the simplest, most visible/cacheable, easiest for clients and API gateways to route on, but
  "pollutes" the URI (arguably the same resource now has multiple URIs) and tends to accumulate
  whole duplicated controller versions over time. **Header versioning**
  (`X-API-Version: 2`) keeps URIs clean/stable but is less visible/discoverable (you can't see the
  version from the URL alone, harder to test with a browser, and some intermediate caches/proxies
  don't vary caching by custom headers by default). **Content-type versioning**
  (`Accept: application/vnd.myapp.v2+json`) is the most "RESTfully pure" (the representation format
  itself is versioned, matching HTTP content negotiation semantics) but is the least ergonomic for
  API consumers and hardest to test manually. In practice, URI versioning dominates in industry
  because of its simplicity and gateway/routing friendliness, despite being the least "pure." →
  Follow-up: *How do you version a REST API without breaking existing clients at all, avoiding
  versioning entirely?* Strict backward-compatible evolution — only add optional new fields (never
  remove or repurpose existing ones), never change a field's type or semantics in place, use
  feature flags/additive endpoints for genuinely breaking changes, and deprecate with a sunset
  timeline (`Deprecation`/`Sunset` HTTP headers) rather than an immediate break — this avoids
  version proliferation but requires real API design discipline.
- [Intermediate] What does "idempotent" mean for an HTTP method, and which standard methods are
  idempotent? → Idempotent means making the same request multiple times has the same effect as
  making it once (the *result state* doesn't change on repeats, even though each call still
  executes). `GET`, `PUT`, `DELETE`, `HEAD`, `OPTIONS` are idempotent by the HTTP spec (`PUT` fully
  replaces a resource with the same representation each time; `DELETE`ing an already-deleted
  resource is still "deleted" afterward, typically returning 404 or 204 on the repeat rather than
  an error). `POST` and `PATCH` are **not** idempotent by default — a repeated `POST` typically
  creates a second resource. → Follow-up: *How do you make a `POST` (e.g. "create an order")
  idempotent so a client's network retry after a timeout doesn't create a duplicate order?* Require
  an **idempotency key** — the client generates a unique key (e.g. a UUID) per logical operation and
  sends it in a header (`Idempotency-Key`); the server persists a record of keys it has already
  processed (with the resulting response), and if the same key arrives again, it returns the
  **stored** result instead of re-executing the operation — the standard pattern used by payment
  APIs (Stripe, etc.) precisely because network retries on `POST` are otherwise dangerous.
- [Basic] What are the two common pagination strategies for a REST list endpoint, and what's the
  trade-off between them? → **Offset-based** (`?page=3&size=20`, translating to `LIMIT 20 OFFSET
  60` in SQL) is simple and lets clients jump to an arbitrary page, but degrades in performance on
  large offsets (the DB still has to scan/skip all preceding rows) and is **unstable under
  concurrent writes** — if a row is inserted/deleted between page requests, results can shift,
  causing skipped or duplicated rows across pages. **Cursor-based** (`?after=<lastSeenId>&size=20`,
  translating to `WHERE id > :lastSeenId ORDER BY id LIMIT 20`) is stable under concurrent writes
  (each page is anchored to a specific row, not a shifting offset) and performs consistently
  regardless of how deep into the data you page, at the cost of not supporting "jump to page N"
  navigation. → Follow-up: *Why is cursor-based pagination generally preferred for high-write,
  large-dataset APIs (e.g. a social feed or transaction history)?* Because offset pagination's
  instability under concurrent writes (skipped/duplicated rows as data shifts between page
  fetches) and its degrading performance at high offsets are both actively worse the larger and
  more write-heavy the dataset is — exactly the conditions those APIs run under.
- [Advanced] What is HATEOAS, and honestly, how often is it used in real production APIs versus
  taught in theory? → HATEOAS (Hypermedia as the Engine of Application State) means API responses
  include **links** to related/available actions (e.g. an `Order` response includes a `_links`
  block with `self`, `cancel`, `pay` URIs) so a client can discover valid next actions dynamically
  from the response itself rather than hard-coding URL construction — Spring supports it via
  `spring-hateoas` (`EntityModel`, `RepresentationModel`, `WebMvcLinkBuilder`). In practice, it's
  used far less than REST's original design intent suggests — most production APIs (internal
  microservices especially) skip it entirely because clients are usually written against a fixed
  API contract anyway (an SDK or a specific frontend), and the discoverability HATEOAS provides
  matters most for truly generic, long-lived public APIs with many independent, evolving client
  implementations (which most internal service-to-service APIs aren't). It's more commonly seen in
  public-facing APIs designed for long-term stability (payment gateways, some public REST APIs)
  than in typical internal microservice-to-microservice calls. → Follow-up: *If you're not using
  HATEOAS, how else do you keep API clients from breaking when you evolve the URI structure?*
  Strict URI/versioning discipline (see versioning question above) plus a published API contract
  (OpenAPI/Swagger spec) that's treated as the actual interface — clients are generated from or
  written against that contract rather than discovering it dynamically, and breaking URI changes go
  through the same deprecation/versioning process as any other breaking change.

---

**File question count: 15** ([Basic] 4, [Intermediate] 7, [Advanced] 4)
