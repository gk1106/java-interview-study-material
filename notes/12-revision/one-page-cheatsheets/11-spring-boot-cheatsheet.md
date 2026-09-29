# Cheat Sheet — 11: Spring Boot Interview Questions (120+ Q bank)

One-page pre-interview skim. Full notes: `notes/11-spring-boot-interview-questions/`.

## Highest-value facts to have loaded

1. **Bean scopes**: `singleton` (default, one per container), `prototype` (new instance per request), `request`/`session`/`application` (web-aware). Injecting `prototype` into `singleton` freezes it to one instance unless you use `ObjectProvider`/`@Lookup`/scoped proxy.
2. **Constructor injection is preferred** over field injection: enables `final` fields, makes required deps impossible to forget, fails circular dependencies fast at startup (`BeanCurrentlyInCreationException`), trivially testable with plain `new`.
3. **`@Transactional` propagation**: `REQUIRED` (default, join or create) · `REQUIRES_NEW` (suspend outer, independent tx — e.g. audit logs that must survive a rollback) · `NESTED` (DB savepoint within the same physical tx — per-item batch failures).
4. **Isolation levels** (each prevents what the level below allows): `READ_UNCOMMITTED` (dirty reads) → `READ_COMMITTED` (no dirty reads, allows non-repeatable reads) → `REPEATABLE_READ` (no non-repeatable reads, allows phantom reads) → `SERIALIZABLE` (fully serial). MySQL/InnoDB defaults to `REPEATABLE_READ`; most others default to `READ_COMMITTED`.
5. **`@Transactional` self-invocation bug**: calling a `@Transactional` method from another method in the *same class* bypasses the AOP proxy entirely — no transaction starts. Same root cause disables any proxy-based aspect (`@Async`, custom `@Aspect`).
6. **`UnexpectedRollbackException`**: an inner `REQUIRED` call throwing marks the *whole physical transaction* rollback-only immediately — catching/swallowing the exception in the outer method does not un-mark it; commit still fails.
7. **N+1 select problem**: lazy-loading a collection per parent row in a loop issues 1 query for parents + N queries for children — fix with `JOIN FETCH`, `@EntityGraph`, or batch fetching.
8. **Auto-configuration** = conditional bean registration (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`) driven by classpath contents, evaluated *last* (after your own `@Configuration`/`@Component` beans) so it can back off. Debug with `--debug` for the positive/negative match report.
9. **`@Configuration` CGLIB proxying** (`proxyBeanMethods=true` default): inter-`@Bean`-method calls within the same config class return the container's singleton, not a fresh instance — `proxyBeanMethods=false` skips this (faster startup, only safe if beans don't call each other).
10. Bean lifecycle order: constructor → DI → Aware callbacks → `BeanPostProcessor.postProcessBeforeInitialization` → `@PostConstruct` → `InitializingBean.afterPropertiesSet()` → custom `init-method` → `postProcessAfterInitialization` (AOP proxies created here) → ready → (shutdown) `@PreDestroy` → `DisposableBean.destroy()`.
11. `@Autowired` resolves **by type first**, then `@Primary`, then `@Qualifier`, then field/param name match; `@Resource` resolves **by name first**. Multiple candidates + no disambiguation → `NoUniqueBeanDefinitionException`.
12. Filter chain / security, JWT, CSRF, resilience patterns (circuit breaker/retry/bulkhead via Resilience4j), and Kafka delivery semantics (at-most-once/at-least-once/exactly-once) are recurring "explain the mechanism" topics — see security.md and microservices.md.

## Question-bank coverage (headings per file)

- **core-spring.md**: IoC container & DI (BeanFactory vs ApplicationContext, 3 injection styles, why constructor injection wins, multi-constructor ambiguity, `List<Interface>` strategy-pattern injection), Bean Scopes (prototype-in-singleton trap, request/session scoped-proxy mechanism), Bean Lifecycle (phase order, `@PostConstruct` vs `InitializingBean`, `BeanPostProcessor` + AOP proxy creation, circular-dependency 3-level cache and why it only works for setter/field injection), `@Configuration` vs `@Component` vs `@Bean` (CGLIB proxying, component-scan base package), Autowiring (`@Primary` vs `@Qualifier`, `@Resource` vs `@Autowired`, multi-implementation strategy resolution).
- **spring-boot.md**: Auto-Configuration Mechanism (`@SpringBootApplication` composition, `AutoConfiguration.imports` file, conditional annotations, `--debug` report), Starters, Profiles, Externalized Configuration & property precedence, Actuator, Embedded Server Tuning.
- **rest-and-validation.md**: Controllers & Request Mapping, Request/Response Binding & Content Negotiation, Bean Validation, API Design (versioning, idempotency, pagination).
- **exception-handling.md**: `@ControllerAdvice`/`@ExceptionHandler`, `ResponseEntityExceptionHandler` & `ProblemDetail`, Consistent Error-Response Design, Exception Translation from the Persistence Layer.
- **data-jpa.md**: N+1 Query Problem, Lazy vs Eager Loading, `@Transactional` Propagation & Isolation (see facts above), First/Second-Level Cache, Optimistic vs Pessimistic Locking, Projections/DTOs vs Entities.
- **security.md**: Filter Chain & `SecurityFilterChain` Configuration, Authentication vs Authorization, JWT Flow for Stateless APIs, OAuth2/OIDC Basics, CSRF, Password Encoding.
- **microservices.md**: Service Discovery, Centralized Configuration, Resilience Patterns (Resilience4j: circuit breaker/retry/bulkhead/rate limiter), API Gateway, Kafka Basics & Delivery Semantics, Distributed Tracing.
- **testing.md**: Test Levels (Unit vs Slice vs Full Context), MockMvc for Controller Testing, Slice Tests in Depth (`@WebMvcTest`, `@DataJpaTest`), Testcontainers, Test Data Setup Strategies.

## Scenario-based questions (scenario-based.md — 10 total, [Advanced]/[Intermediate])

1. p99 latency tripled over the last hour, no deployment — what do you check, in order?
2. Intermittent 500s under load, fine at low traffic, passes all tests — how do you debug it?
3. Rolling out a schema migration (renaming a column, adding NOT NULL) without downtime.
4. Debugging a memory leak in a running Spring Boot application in production.
5. A downstream dependency starts responding slowly (not down) — what's your response?
6. Deploying a breaking REST API change that mobile clients depend on.
7. CI/CD deployed a bad change, traffic is currently affected — immediate response?
8. One specific customer's requests are consistently slow — how do you investigate?
9. A `@Scheduled` batch job that used to take 5 minutes now takes 45 minutes.
10. Introducing caching to reduce database load on a read-heavy endpoint.

Answer these using the **triage structure** the notes model: what you'd check first/second/third —
not a one-line guess. Interviewers grade the process as much as the final root cause.

## Top pitfalls to name-drop

- Self-invocation defeats `@Transactional`/`@Async`/any AOP-proxy-based aspect.
- Catching an exception doesn't undo `UnexpectedRollbackException` once the tx is marked rollback-only.
- N+1 selects from lazy collections iterated in a loop.
- Field injection hides missing-dependency bugs until runtime; constructor injection fails at startup.
- Forgetting `proxyMode` on a request/session-scoped bean injected into a singleton → `ScopeNotActiveException`.
