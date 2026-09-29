# Spring Boot — Auto-Configuration, Starters, Profiles, Properties & Actuator

Spring Boot questions test whether you understand what the "magic" is actually doing under the
hood — auto-configuration isn't magic, it's conditional bean registration driven by what's on the
classpath. This file covers the auto-configuration mechanism, starters, profiles, externalized
configuration and property precedence, Actuator, and embedded server tuning.

## Auto-Configuration Mechanism

- [Basic] What does `@SpringBootApplication` actually do? → It's a meta-annotation bundling three:
  `@SpringBootConfiguration` (a specialized `@Configuration`, marks the class as a config source),
  `@EnableAutoConfiguration` (triggers the auto-configuration mechanism described below), and
  `@ComponentScan` (scans the current package and sub-packages for `@Component` beans). →
  Follow-up: *Why does the main class's package location matter?* `@ComponentScan`'s default base
  package is wherever the annotated class lives — placing your main class in the root package above
  all feature packages ensures everything gets scanned; put it in a leaf package and sibling
  packages silently get skipped.
- [Intermediate] How does `@EnableAutoConfiguration` decide which auto-configuration classes to
  activate? → In Spring Boot 2.7+, it reads a file at
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` inside each
  jar on the classpath (older versions used `META-INF/spring.factories` under the
  `EnableAutoConfiguration` key) — this lists every candidate `@Configuration` class the starter
  jar ships (e.g. `DataSourceAutoConfiguration`, `JacksonAutoConfiguration`). Every listed class is
  then evaluated, but each is typically guarded by `@Conditional*` annotations
  (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, etc.) so it only
  actually registers its beans if its conditions hold — this is what makes auto-config "smart": it
  backs off automatically the moment you define your own bean of the same type. → Follow-up: *Why
  did Spring Boot 2.7 move away from `spring.factories` for auto-configuration specifically?* The
  old file mixed many unrelated extension points (auto-config, failure analyzers, etc.) under
  generic keys, making it slow to scan and hard to reason about; the dedicated
  `AutoConfiguration.imports` file is auto-config-specific, faster to process, and clearer intent
  (other extension points still use `spring.factories`).
- [Intermediate] Explain `@ConditionalOnClass`, `@ConditionalOnMissingBean`, and
  `@ConditionalOnProperty` with a concrete example of why each exists. → `@ConditionalOnClass`
  activates a config only if a given class is on the classpath — e.g.
  `DataSourceAutoConfiguration` only fires if a JDBC driver / `DataSource` class is present, so
  adding `spring-boot-starter-data-jpa` is what "turns on" datasource auto-config, not some global
  switch. `@ConditionalOnMissingBean` activates only if the user hasn't already defined their own
  bean of that type — this is the backoff mechanism: define your own `ObjectMapper` `@Bean` and
  Spring Boot's default `JacksonAutoConfiguration` politely steps aside instead of conflicting.
  `@ConditionalOnProperty` activates based on an `application.yml` property value (e.g.
  `management.endpoint.health.enabled=true`), letting ops toggle auto-configured features without
  code changes. → Follow-up: *What order do these conditions get evaluated relative to your own
  `@Configuration` classes?* Auto-configuration classes are processed **last**, after all your
  user-defined `@Configuration`/`@Component` beans are registered — which is exactly why
  `@ConditionalOnMissingBean` works: by the time auto-config runs, your custom bean (if any) is
  already in the registry to check against.
- [Advanced] How would you debug "why is bean X (or X isn't) auto-configured" in a real app? →
  Run with `--debug` (or set `debug=true` in properties) — Spring Boot prints an auto-configuration
  report at startup showing every auto-config class evaluated, split into "Positive matches"
  (conditions passed, beans registered) and "Negative matches" (conditions failed, with the exact
  reason, e.g. "required class 'javax.sql.DataSource' not found" or "matched" vs "did not find
  bean of type X"). This report is the single fastest way to answer "why isn't my auto-configured
  bean showing up" without stepping through framework source in a debugger. → Follow-up: *What's a
  common real cause of an auto-configuration silently not firing that this report catches quickly?*
  A missing starter dependency (the required class genuinely isn't on the classpath), or an
  unintentional user-defined bean of the same type elsewhere in the codebase that's triggering
  `@ConditionalOnMissingBean` backoff without anyone realizing it.

## Starters

- [Basic] What is a Spring Boot "starter," and why do they exist? → A starter (e.g.
  `spring-boot-starter-web`, `spring-boot-starter-data-jpa`) is a curated, version-aligned
  dependency bundle — a single Maven/Gradle coordinate that pulls in everything typically needed
  for that concern (for `-web`: Spring MVC, embedded Tomcat, Jackson) plus the matching
  auto-configuration classes, all at versions tested to work together via Spring Boot's parent
  BOM (`spring-boot-dependencies`). They exist to eliminate manual dependency-version hunting and
  "works on my machine" mismatches between, say, Jackson and Spring MVC versions. → Follow-up:
  *What does the parent POM / BOM actually control?* It manages (pins) dependency **versions**
  across the whole transitive graph — you typically don't specify a version for any Spring-managed
  dependency yourself; Spring Boot's BOM guarantees a tested, compatible version set for that Boot
  release.
- [Intermediate] What's the practical difference between `spring-boot-starter-web` and
  `spring-boot-starter-webflux` at the architecture level, not just dependency contents? →
  `-web` brings Spring MVC on an embedded **Tomcat** (or Jetty/Undertow) — a traditional
  **thread-per-request**, blocking servlet model: each request occupies a thread for its full
  duration, including while blocked on I/O (DB call, downstream HTTP call). `-webflux` brings
  **Spring WebFlux** on **Netty** by default — a **reactive, non-blocking, event-loop** model built
  on Project Reactor (`Mono`/`Flux`): a small, fixed pool of event-loop threads handles many
  concurrent requests by never blocking a thread on I/O, instead registering callbacks. → Follow-up:
  *When does WebFlux's model actually pay off versus just adding complexity?* Under high
  concurrency with I/O-bound, non-CPU-heavy workloads (many concurrent slow downstream calls) —
  it lets far fewer threads serve far more concurrent requests. For CPU-bound work, or a codebase
  whose downstream clients are all blocking anyway (a blocking JDBC driver, say), WebFlux adds
  learning curve and debugging complexity (stack traces across reactive operators) without a real
  throughput win — plain MVC with virtual threads (Java 21+) is increasingly the simpler answer to
  the same "many concurrent blocked requests" problem.

## Profiles

- [Basic] How do Spring profiles work, and how do you activate one? → `@Profile("dev")` on a
  `@Configuration`/`@Component`/`@Bean` restricts that bean's registration to when the `"dev"`
  profile is active; `application-{profile}.yml`/`.properties` files (e.g.
  `application-prod.yml`) are automatically layered on top of the base `application.yml` when that
  profile is active, overriding matching keys. Activate via `spring.profiles.active=prod`
  (env var `SPRING_PROFILES_ACTIVE`, JVM arg `-Dspring.profiles.active=prod`, or in
  `application.yml` itself). → Follow-up: *What happens if two active profiles define the same
  property differently?* The profile listed **later** in `spring.profiles.active` (comma-separated)
  wins for that key — later entries take precedence over earlier ones.
- [Intermediate] What's the difference between profile-specific YAML files and Spring Boot's
  multi-document YAML (`---` separators with `spring.config.activate.on-profile`)? → Separate files
  (`application-dev.yml`, `application-prod.yml`) keep each environment fully isolated in its own
  file — easy to diff/review per environment, but you lose an at-a-glance view of what differs.
  Multi-document YAML puts all profiles in one `application.yml`, separated by `---`, each guarded
  by `spring.config.activate.on-profile: prod` — easier to see all environment variants side by
  side in one file, at the cost of a larger single file. Both are equally valid; team convention
  usually decides. → Follow-up: *Can you combine profiles, e.g. `prod` and `eu-region`
  simultaneously?* Yes — `spring.profiles.active=prod,eu-region` activates both; beans/properties
  guarded by either profile are included, and if both define the same property key, the later one
  in the list wins.
- [Intermediate] Why is it a bad practice to bake environment-specific secrets (DB passwords, API
  keys) directly into `application-prod.yml` committed to git? → It puts production secrets in
  version control history permanently (even if later removed, they remain in git log/blame),
  visible to anyone with repo read access, and impossible to rotate without a code change +
  redeploy. The standard fix is **externalizing** secrets outside the packaged artifact entirely —
  environment variables injected by the deployment platform, a secrets manager
  (AWS Secrets Manager, HashiCorp Vault, Kubernetes Secrets mounted as env vars or files), or
  Spring Cloud Config with encrypted values — so the jar/image itself contains no secrets and
  secrets can be rotated independently of deployments. → Follow-up: *How does Spring Boot let an
  environment variable override a value in `application.yml` without any code change?* Spring's
  relaxed binding maps `MY_PROPERTY_NAME` (env var, upper snake case) to `my.property.name` (YAML
  key) automatically — environment variables are one of the standard property sources in the
  precedence chain (see next section), so setting `DB_PASSWORD` as an env var overrides
  `db.password:` in YAML with zero code or config changes.

## Externalized Configuration & Property Precedence

- [Basic] Name the property source precedence order, highest to lowest, for the sources you use
  day to day. → Roughly (highest wins): (1) command-line arguments
  (`--server.port=8081`), (2) JVM system properties (`-Dserver.port=8081`), (3) OS environment
  variables, (4) profile-specific `application-{profile}.yml`, (5) the base `application.yml`,
  (6) `@PropertySource`-annotated values, (7) default values set in `@Value("${x:default}")` /
  code. → Follow-up: *Why does this order make sense operationally?* It lets you override any
  baked-in config at deploy time without rebuilding the artifact — ops can flip a command-line flag
  or env var to override what's in the packaged YAML, which is exactly the flexibility needed for
  promoting the same build artifact through dev/staging/prod with different configuration per
  environment (the "build once, configure per environment" principle).
- [Intermediate] What's the difference between `@Value` and `@ConfigurationProperties` for binding
  external config, and when would you choose one over the other? → `@Value("${app.timeout:30}")`
  injects a single property directly into a field, with SpEL support — fine for one or two ad-hoc
  values, but it's untyped-string-key-based (typos in the key string fail silently or at runtime,
  not compile time) and doesn't group related settings. `@ConfigurationProperties(prefix = "app")`
  binds an entire hierarchical block of properties into a strongly-typed, validated (with
  `@Validated` + Bean Validation annotations) POJO in one shot, supports relaxed binding (kebab-case
  YAML to camelCase fields), and is far easier to test and IDE-autocomplete (with the
  `spring-boot-configuration-processor` generating metadata). Prefer `@ConfigurationProperties` for
  any config group with more than 1-2 related values, especially anything you'd want validated at
  startup (fail fast on a missing required property) rather than discovering a `null` deep in
  request handling. → Follow-up: *How do you make the app fail to start if a required
  `@ConfigurationProperties` field is missing/invalid?* Annotate the POJO with `@Validated` and put
  Bean Validation annotations (`@NotNull`, `@Min`, etc.) on its fields — Spring Boot validates the
  bound object at context startup and throws a `BindValidationException`, failing fast instead of
  discovering a `null` in production traffic.
- [Advanced] You need the same Docker image to run in dev, staging, and prod with different config,
  without rebuilding. Walk through how you'd design this with Spring Boot's config model. →
  Package the image with only defaults in `application.yml` (safe, non-secret values) and no
  environment-specific secrets baked in at all. At deploy time, inject `SPRING_PROFILES_ACTIVE` as
  an environment variable to select the profile (activates `application-{env}.yml` if you ship
  those inside the image for non-secret, environment-shaped config like connection pool sizes or
  feature flags), and inject actual secrets (DB credentials, API keys) purely via environment
  variables or mounted files from the platform's secret store — never from a profile YAML checked
  into the image. For config that needs to change **without a redeploy** (a feature flag flipped
  mid-incident), point to a centralized config source (Spring Cloud Config Server, or a
  Kubernetes ConfigMap mounted as a volume with Spring Boot's file-watching config reload, or
  Consul/Vault) so the running pods can pick up changes via `/actuator/refresh` or a restart without
  a new image build. → Follow-up: *What's the risk of relying purely on environment variables for
  every single config value at scale?* It becomes hard to audit/version what config a given
  deployment actually ran with (env vars aren't typically diffed/reviewed the way a config-repo
  commit is), and there's no single source of truth for "what does prod look like right now" —
  centralized config management (Config Server, GitOps-managed ConfigMaps) solves that audit trail
  problem that raw env vars don't.

## Actuator

- [Basic] What is Spring Boot Actuator, and what does the `/actuator/health` endpoint show by
  default? → Actuator is a starter (`spring-boot-starter-actuator`) that exposes production-ready
  operational endpoints — health checks, metrics, environment info, thread dumps, etc. — over HTTP
  (or JMX). `/health` by default shows a simple `{"status": "UP"}`/`"DOWN"` aggregate, but with
  `management.endpoint.health.show-details=always` it breaks down per **HealthIndicator**
  (database connectivity, disk space, custom checks), each contributing its own status that's
  aggregated into the overall result. → Follow-up: *Which Actuator endpoints are exposed over HTTP
  by default, and why so few?* Only `/health` and `/info` are exposed by default in a Boot app;
  everything else (`/env`, `/beans`, `/threaddump`, `/metrics`, `/mappings`) must be explicitly
  opted into via `management.endpoints.web.exposure.include`, because endpoints like `/env` and
  `/heapdump` can leak secrets or sensitive internals if left open on a public-facing app — secure
  by default.
- [Intermediate] How would you write a custom `HealthIndicator` for a downstream dependency (say, a
  payment gateway), and why does that matter for a Kubernetes deployment? → Implement
  `HealthIndicator` (or extend `AbstractHealthIndicator`), override `doHealthCheck`/`health()` to
  ping the dependency (a lightweight call, not a full transaction) and return `Health.up()` or
  `Health.down().withDetail("error", ...)`. Register it as a `@Component` and Spring auto-detects
  it, folding it into the aggregate `/health` status. This matters directly for k8s **readiness
  probes** — if `/health` is wired as the readiness probe endpoint and a critical downstream is
  unreachable, the pod reports not-ready, k8s stops routing traffic to it (removes it from the
  Service's endpoint list) without killing the pod, giving the dependency time to recover instead
  of every pod failing requests. → Follow-up: *What's the difference between wiring `/health` as a
  liveness probe versus a readiness probe, and why does mixing them up cause outages?* Liveness
  answers "should k8s restart this pod" (only fail it for unrecoverable states — a deadlocked JVM,
  not a flaky downstream); readiness answers "should traffic be routed here right now" (fail it for
  transient issues like a downstream outage). Wiring a downstream dependency check into
  **liveness** instead of readiness means a temporary third-party outage causes k8s to kill and
  restart every pod in a loop — restarting your app fixes nothing about a broken downstream, so
  that's actively harmful; the dependency check belongs in readiness only.
- [Intermediate] How does `/actuator/metrics` relate to Micrometer, and how would you export
  metrics to Prometheus? → Actuator's metrics support is built on **Micrometer**, a
  vendor-neutral metrics facade (analogous to SLF4J for logging) — your code (or auto-configured
  instrumentation for HTTP requests, JVM, DataSource pools, etc.) records metrics against
  Micrometer's API, and a **registry implementation** determines where they're published.
  Add `micrometer-registry-prometheus` to the classpath and Micrometer auto-configures a
  `/actuator/prometheus` endpoint exposing all metrics in Prometheus's scrape format, which a
  Prometheus server then polls on an interval. → Follow-up: *What auto-instrumented metric would
  you check first if p99 latency spiked but CPU looked fine?* `http.server.requests` broken down by
  URI/status (find which endpoint is slow), cross-referenced with connection-pool metrics like
  `hikaricp.connections.pending` / `hikaricp.connections.active` — a saturated DB connection pool
  causing requests to queue for a connection is a classic cause of latency spikes with normal CPU.

## Embedded Server Tuning

- [Intermediate] What are the key Tomcat thread-pool properties you'd tune under load, and what do
  they control? → `server.tomcat.threads.max` (max worker threads handling requests — the ceiling
  on concurrent blocking request handling), `server.tomcat.threads.min-spare` (threads kept warm
  even when idle), `server.tomcat.accept-count` (queue depth for connections once all worker
  threads are busy — beyond this, new connections are refused/reset), and
  `server.tomcat.max-connections` (max simultaneous open TCP connections, including queued ones). A
  saturated app under load typically means `threads.max` is undersized relative to the incoming
  concurrency and the wait/compute ratio of the average request (blocked heavily on a downstream
  DB or API call needs proportionally more threads, same reasoning as `ThreadPoolExecutor` sizing).
  → Follow-up: *If you raise `threads.max` very high to "fix" latency under load, what's the risk?*
  Each blocked thread still consumes memory (default ~1MB stack) and adds context-switch overhead;
  a huge thread count doesn't fix a slow downstream dependency — it just delays hitting an
  OutOfMemoryError or moves the bottleneck from "requests queued in Tomcat" to "requests queued at
  the downstream DB/API," often making the downstream's own overload worse. The real fix is usually
  addressing the slow downstream (timeouts, circuit breaker, connection pool sizing) rather than
  scaling threads indefinitely.
- [Advanced] Your Spring Boot app's actuator shows healthy CPU and memory, but p99 latency is high
  and the Tomcat thread pool is consistently near `threads.max`. What's your diagnostic approach? →
  Thread saturation with normal CPU strongly suggests threads are **blocked waiting**, not doing
  CPU work — so: (1) pull a thread dump (`/actuator/threaddump` or `jstack`) and look at what state
  the bulk of "busy" threads are actually in — most will likely show `BLOCKED`/`WAITING` in a
  downstream call (JDBC, `RestTemplate`/`WebClient`, a lock); (2) check downstream latency metrics
  (`http.client.requests` for outbound calls, DB query duration, `hikaricp.connections.pending`) —
  a slow downstream directly explains threads piling up upstream; (3) check for a shared lock or
  `synchronized` block that's become a bottleneck under higher concurrency than it was designed for;
  (4) check connection pool sizing (HikariCP `maximum-pool-size`) — if it's smaller than the
  request concurrency, requests queue for a DB connection even though the DB itself is fine. The
  fix is almost never "raise `threads.max`" in isolation — that just moves the queueing point
  downstream or trades it for memory pressure; it's addressing whatever the threads are actually
  blocked on. → Follow-up: *Why is a thread dump more diagnostic here than an APM's CPU flame
  graph?* A CPU flame graph only samples threads that are actively running on-CPU — a thread
  sitting `BLOCKED` waiting on I/O or a lock barely shows up in CPU-based profiling at all; a thread
  dump captures the *stack and state* of every thread regardless of whether it's using CPU, which is
  exactly what you need to see "84 threads all stuck inside `HikariPool.getConnection()`."

---

**File question count: 15** ([Basic] 4, [Intermediate] 8, [Advanced] 3)
