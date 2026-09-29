# Microservices — Discovery, Config, Resilience, Kafka, Gateway, Tracing

Microservices questions probe whether you've actually reasoned about failure modes in a
distributed system, not just memorized a component's name. Interviewers expect you to explain
*why* a circuit breaker prevents cascading failure, not just that Resilience4j has one. This file
covers service discovery, centralized configuration, resilience patterns, API gateway
responsibilities, Kafka producer/consumer basics and delivery semantics, and distributed tracing.

## Service Discovery

- [Basic] Why is service discovery needed in a microservices architecture, and what problem does it
  solve that hard-coded URLs don't? → In a dynamic environment (containers/pods being created,
  destroyed, rescheduled, auto-scaled), service instances' network locations (IP:port) change
  constantly — hard-coding `http://10.0.1.5:8080` in a downstream service's config breaks the moment
  that instance is replaced. Service discovery provides a **registry** that instances register
  themselves into on startup (and deregister/get evicted from on shutdown or health-check failure),
  and a lookup mechanism so callers resolve a **logical service name** (`order-service`) to a
  current, healthy set of instance addresses at call time instead of a static config value. →
  Follow-up: *What are the two main styles of service discovery, client-side and server-side, and
  which does Netflix Eureka use?* **Client-side discovery** — the calling service itself queries the
  registry and picks an instance (often with client-side load balancing), which is what Eureka
  (paired with a client-side load balancer like Spring Cloud LoadBalancer) uses.
  **Server-side discovery** — the caller just sends the request to a well-known
  load balancer/gateway, which itself queries the registry and routes — this is closer to how
  Kubernetes Services or a cloud load balancer work, hiding discovery from the caller entirely.
- [Intermediate] How does a Eureka client's registration and health-check cycle actually work, and
  what's the real-world consequence of the default heartbeat/eviction timing being too slow? →
  On startup, a Eureka client (`@EnableDiscoveryClient` / `spring-cloud-starter-netflix-eureka-
  client`) registers itself with the Eureka server, then sends a periodic **heartbeat** (default
  every 30s) to renew its lease; if the server doesn't receive a heartbeat within the **eviction
  timeout** (default 90s), it removes the instance from the registry. Clients also cache the
  registry locally (refreshed periodically, default every 30s) rather than querying the server on
  every call, for resilience/performance. Real-world consequence: with these defaults, a genuinely
  crashed instance can remain in the registry (and keep receiving traffic from clients using a
  stale local cache) for **up to a couple of minutes** after it actually died — for a
  latency-sensitive production service, this default staleness window is often tuned down
  (shorter heartbeat/eviction intervals) at the cost of more registry chatter, or paired with
  **client-side circuit breakers** (see below) so a dead instance quickly gets skipped by callers
  regardless of what the registry still says. → Follow-up: *Why can't Eureka just evict instantly
  the moment one heartbeat is missed?* Network blips causing one missed heartbeat are common and
  don't mean the instance is actually down — instant eviction on a single miss would cause
  unnecessary churn (flapping instances in/out of the registry) for transient network noise; the
  timeout window trades a bit of staleness for tolerance of normal network jitter.
- [Intermediate] In a Kubernetes-native deployment, do you still need Eureka/Consul, or does k8s
  already provide service discovery? → Kubernetes provides its own built-in service discovery via
  **Services** and internal DNS (`my-service.namespace.svc.cluster.local` resolves to a stable
  virtual IP that load-balances across healthy pod endpoints, using `kube-proxy`/iptables/IPVS
  rules) — for a k8s-native deployment, this largely replaces the need for a separate
  application-level registry like Eureka; adding Eureka on top is usually redundant complexity.
  Eureka (or Consul) still earns its place when running **outside** Kubernetes (VMs, bare metal,
  or a hybrid/multi-cluster setup) or when you need discovery features k8s Services don't provide
  out of the box (Consul in particular supports multi-datacenter service mesh and richer health
  check types). → Follow-up: *What does Consul offer beyond basic discovery that Eureka doesn't,
  which is sometimes worth adopting even in k8s?* Consul doubles as a general-purpose distributed
  **key-value store** (usable for centralized config, similar to what Spring Cloud Config
  provides) and has first-class multi-datacenter federation and service-mesh (Consul Connect)
  support — teams already using Consul for config/KV sometimes use its discovery too just to avoid
  running two separate systems, even inside k8s.

## Centralized Configuration

- [Basic] What problem does Spring Cloud Config Server solve, and how does a client pull its
  config? → Instead of each microservice bundling its own `application.yml` with environment-specific
  values baked into its deployed artifact, a **Config Server** centralizes configuration (typically
  backed by a Git repo, so config changes are versioned/reviewable like code) and serves it over
  HTTP; each client service (`spring-cloud-starter-config`) fetches its config from the Config
  Server **at startup**, keyed by application name + active profile
  (`GET /order-service/prod`), before the rest of the Spring context initializes. → Follow-up:
  *Why is backing the Config Server with a Git repo specifically valuable, beyond just central
  storage?* Config changes get full version history, code-review-style pull requests, rollback via
  `git revert`, and an audit trail of who changed what and when — the same governance discipline
  applied to application code, applied to configuration.
- [Intermediate] By default, config is only fetched once at startup — how do you get a running
  service to pick up a config change without a full redeploy/restart? → Add
  `spring-boot-starter-actuator` and expose the `/actuator/refresh` endpoint (requires
  `@RefreshScope` on beans whose values should be re-bound) — calling it re-fetches config from the
  Config Server and re-initializes `@RefreshScope`-annotated beans with the new values, without
  restarting the JVM. Calling `/actuator/refresh` on every instance individually doesn't scale well
  for a fleet, so Spring Cloud Bus (backed by a message broker like RabbitMQ or Kafka) is commonly
  paired with it — a single webhook/trigger (often from the Git repo on push) broadcasts a refresh
  event over the bus, and every subscribed instance refreshes itself in response, propagating a
  config change fleet-wide from one action. → Follow-up: *What's a real risk of `@RefreshScope`
  for a bean holding a stateful resource, like a connection pool sized from config?* Refreshing
  recreates the bean's proxy target on next access, which can be disruptive for beans wrapping
  expensive/stateful resources (a `DataSource` connection pool mid-use) — `@RefreshScope` is
  best suited to simple, stateless config values (feature flags, thresholds, timeouts), not beans
  managing live external resource connections, where an uncoordinated refresh could interrupt
  in-flight work.
- [Advanced] What's the operational risk of the Config Server itself becoming unavailable, and how
  do you design against it? → If every service fetches its config exclusively at startup from a
  single Config Server, an outage there **doesn't affect already-running instances** (they already
  have their config in memory) — but it **does block any new instance from starting**, which is
  exactly what happens during an auto-scaling event or a rolling deployment/restart, potentially at
  the worst possible time (e.g. scaling up during a traffic spike, right when the Config Server
  itself might also be under load). Mitigations: run the Config Server itself as a
  highly-available, horizontally-scaled deployment (not a single instance — the classic
  single-point-of-failure mistake for a component everything else depends on at startup); enable
  each client's **local config caching/fallback** (Spring Cloud Config clients can be configured to
  fall back to a locally cached copy of the last successfully fetched config,
  `spring.cloud.config.fail-fast=false` plus retry settings, so a transient Config Server blip
  during a rolling restart doesn't hard-fail every new instance); and set sensible retry/backoff
  behavior on the client so transient Config Server unavailability is retried rather than
  immediately fatal. → Follow-up: *Why is "fail fast" (`fail-fast=true`) sometimes still the right
  choice despite this risk?* For genuinely critical config (a required secret, a mandatory
  feature toggle whose absence would cause silently wrong behavior rather than an obvious startup
  crash), starting up with **stale or missing** config can be more dangerous than not starting at
  all — failing fast surfaces the problem immediately and loudly (a crash-looping pod is visible
  in monitoring) rather than a service silently running with wrong/default values, which is often
  the worse outcome.

## Resilience Patterns (Resilience4j)

- [Basic] What does a circuit breaker do, and what problem does it solve that a plain timeout
  doesn't? → A circuit breaker wraps calls to a downstream dependency and tracks the recent
  **failure rate**; once failures exceed a configured threshold, it **trips open** — for a
  configured wait duration, it stops even attempting calls to that dependency at all, failing fast
  locally instead (returning an error or a fallback immediately, no network call made). A plain
  timeout still lets every single request **attempt** the call and wait out the full timeout before
  failing — if a downstream is genuinely down, that means every caller thread still ties up
  resources (a thread, a connection) for the full timeout duration on every request, which under
  load can exhaust the caller's own thread pool/connection pool purely from waiting on a dependency
  that's already known to be broken — this cascading resource exhaustion is exactly what a tripped-
  open circuit prevents, by skipping the attempt (and the wait) entirely once the dependency is
  known to be unhealthy. → Follow-up: *What are the three states of a circuit breaker, and what
  triggers each transition?* **Closed** (normal — calls pass through, failures tracked) →
  **Open** (failure threshold exceeded — calls fail fast immediately, no downstream attempt) →
  after a wait duration, **Half-Open** (a small number of trial calls are let through to test if the
  dependency has recovered) → if trial calls succeed, back to **Closed**; if they still fail, back
  to **Open** for another wait period.
- [Intermediate] Explain the difference between Retry, Circuit Breaker, and Bulkhead in
  Resilience4j, and how they compose together for one downstream call. → **Retry** re-attempts a
  failed call a configured number of times (ideally with exponential backoff + jitter), for
  **transient** failures expected to self-resolve quickly (a momentary network blip). **Circuit
  Breaker** protects against a **sustained** downstream failure by stopping attempts entirely once a
  failure threshold is crossed, preventing wasted retries against something that's clearly broken,
  not just transiently glitching. **Bulkhead** limits the number of **concurrent** calls to a given
  downstream (via a semaphore or a dedicated thread pool), so one slow/overloaded dependency can't
  consume unbounded caller resources (threads, connections) and starve calls to *other*,
  perfectly healthy dependencies sharing the same caller service. Composed together (Resilience4j
  supports this as a decorator chain): Bulkhead limits concurrency first, Circuit Breaker decides
  whether to even attempt the call, and Retry governs what happens on a failed attempt within
  what the circuit breaker currently allows — the standard order is roughly
  Bulkhead → CircuitBreaker → Retry → TimeLimiter, wrapping the actual call, each layer protecting
  against a different failure mode. → Follow-up: *Why is naive, unconditional retry dangerous for a
  downstream that's already overloaded?* Retrying an already-overloaded/failing dependency adds
  **more** load to it right when it can least handle more — a "retry storm" from many callers
  simultaneously retrying a struggling dependency can be exactly what tips a recoverable slowdown
  into a full outage; this is why retries should be paired with a circuit breaker (stop retrying
  once it's clearly not transient) and backoff+jitter (spread retries out in time instead of
  synchronized bursts).
- [Advanced] Design a resilience strategy for a service that calls three downstreams: a critical
  payment gateway (must succeed or the whole request fails), a recommendations service (nice-to-
  have, can degrade gracefully), and an internal audit-log service (fire-and-forget, must not block
  the response). → **Payment gateway**: circuit breaker (fail fast once it's clearly down, rather
  than hanging every payment request), a short bounded retry only for clearly-transient errors
  (not for e.g. a declined-card business response, which retrying won't fix), and a strict timeout
  — but **no fallback that fakes success**; if it's genuinely unavailable, the request should fail
  loudly and visibly, since silently proceeding without payment confirmation is worse than an
  explicit error. **Recommendations service**: circuit breaker + a **fallback** that returns an
  empty/default/cached recommendation list instead of propagating the failure — the main request
  (e.g. "show product page") should succeed even if recommendations are unavailable, since
  recommendations are enhancement, not core functionality; a short timeout so a slow
  recommendations call doesn't drag down the whole page's response time. **Audit log**: called
  **asynchronously** (fire-and-forget via a message queue/Kafka topic, or at minimum a separate
  thread pool with its own bulkhead) so it structurally **cannot** block or fail the main response
  regardless of its own health — if it's down, log locally/buffer and move on, never let it be a
  synchronous dependency of the critical path at all. The general principle: resilience
  configuration should match each dependency's actual criticality to the request's success
  criteria, not a one-size-fits-all timeout/retry policy applied uniformly — treating a
  nice-to-have dependency with the same strictness as a critical one either makes the whole request
  needlessly fragile (fails when recommendations blip) or, treating a critical dependency too
  leniently (silently falling back), causes real correctness bugs (a payment silently "succeeding"
  without confirmation). → Follow-up: *Why is a synchronous, blocking call to a fire-and-forget
  audit log a real production risk even with a short timeout configured?* Even a short timeout adds
  latency to *every* request on the critical path for a dependency that provides no value to the
  actual response — and if the audit service degrades (not fully down, just slow), that latency
  compounds across every request; making it genuinely asynchronous removes it from the critical
  path's latency budget entirely rather than just bounding how much it can add.

## API Gateway

- [Intermediate] What responsibilities does an API Gateway typically centralize, and why put them
  there instead of in each individual microservice? → Common responsibilities: routing (mapping
  external paths to internal services), authentication/token validation (verify a JWT once at the
  edge rather than redundantly in every downstream service), rate limiting/throttling, request/
  response transformation, TLS termination, centralized logging/metrics for all inbound traffic,
  and sometimes response aggregation (combining calls to multiple backend services into one client
  response, particularly for a BFF — Backend-for-Frontend — pattern). Centralizing these at the
  gateway avoids duplicating the same cross-cutting logic (especially auth) in every single
  microservice, gives one place to change a cross-cutting policy (a new rate limit) without
  touching N services, and keeps internal service-to-service calls (behind the gateway, inside the
  trusted network) simpler since some of that cross-cutting concern is already handled at the
  edge. → Follow-up: *What's a risk of putting too much business logic into the gateway layer
  itself?* The gateway becomes a bottleneck for both performance (a single choke point all traffic
  passes through) and development velocity (a shared component that every team's feature now
  requires changes to, recreating the "one team blocks everyone" problem microservices were meant
  to avoid) — the gateway should stay focused on cross-cutting, generic concerns, not
  service-specific business logic.
- [Intermediate] Should internal, service-to-service calls also go through the API Gateway, or
  bypass it? → Generally **bypass it** — internal service-to-service traffic (inside the trusted
  network/cluster) typically calls the target service **directly** (via service discovery, or a
  service mesh sidecar for cross-cutting concerns like mTLS/retries at that layer instead), because
  routing every internal call through the gateway adds an unnecessary latency hop and makes the
  gateway a scaling bottleneck for traffic it doesn't actually need to see (the gateway's real job
  is the **external-facing** boundary — authenticating and routing traffic from outside the
  trust boundary in). A service mesh (Istio, Linkerd) is the more common answer for
  internal-traffic cross-cutting concerns (mTLS, retries, internal traffic policy) precisely because
  it operates per-service via sidecars rather than funneling everything through one central
  gateway hop. → Follow-up: *What's the difference in responsibility between an API Gateway and a
  service mesh, since both sound like they handle "cross-cutting traffic concerns"?* API Gateway =
  **north-south** traffic (external client to the system's edge) — auth, external routing, rate
  limiting at the boundary. Service mesh = **east-west** traffic (service-to-service, internal) —
  mTLS between services, internal load balancing, retries/circuit breaking at the network layer,
  observability for internal calls — they're complementary, not competing, layers.

## Kafka Basics & Delivery Semantics

- [Basic] What are the core building blocks of Kafka — topic, partition, offset, consumer group —
  and how do they relate? → A **topic** is a named stream of messages, split into one or more
  **partitions** for parallelism and scalability; each message within a partition gets a
  monotonically increasing **offset**, its position in that partition's log. A **consumer group**
  is a set of consumer instances that **share** the work of consuming a topic — Kafka assigns each
  partition to exactly **one** consumer within a group at a time (so within a group, partitions are
  load-balanced across consumers, giving parallelism up to the partition count), while **different**
  consumer groups each independently receive their own full copy of every message (this is how
  Kafka supports both queue-like load-balanced consumption within a group and pub/sub-style
  fan-out across groups from the same topic). → Follow-up: *If a topic has 4 partitions and you add
  a 5th consumer to a group of 4 (so 5 consumers, 4 partitions), what happens to the 5th consumer?*
  It sits **idle** — Kafka can assign at most one consumer per partition within a group at any
  time, so with more consumers than partitions in a group, the excess consumers get no partitions
  assigned and do nothing until a partition frees up (e.g. another consumer in the group leaves).
- [Intermediate] Explain at-most-once, at-least-once, and exactly-once delivery semantics in Kafka,
  and what actually determines which one a consumer gets. → It's governed by **when** the consumer
  commits its offset relative to processing the message. **At-most-once**: commit the offset
  **before** processing the message (or auto-commit on a timer regardless of processing outcome) —
  if the consumer crashes mid-processing, that message is lost (never reprocessed, since the
  offset already advanced past it). **At-least-once**: commit the offset **after** successfully
  processing the message — if the consumer crashes after processing but before committing, the
  message gets **redelivered** on restart (since the offset wasn't advanced), meaning the consumer
  must be able to safely process the same message more than once. **Exactly-once**: requires
  Kafka's transactional producer/consumer APIs (idempotent producers + transactional writes that
  atomically tie a consumer's offset commit to the producer's output in a downstream Kafka topic)
  — genuinely hard to achieve end-to-end outside Kafka-to-Kafka pipelines, and largely impractical
  once the "processing" involves an external side effect (a DB write, an external API call) that
  Kafka's transaction coordinator has no visibility into. → Follow-up: *Given exactly-once is hard
  outside Kafka-to-Kafka, what's the practical, widely-used alternative for a consumer that writes
  to a database?* Design the consumer's processing to be **idempotent** — at-least-once delivery
  plus idempotent processing (e.g. an `INSERT ... ON CONFLICT DO NOTHING`/upsert keyed by a message
  ID, or checking "have I already processed message ID X" before acting) achieves the same
  practical outcome (no duplicate side effects) without needing true exactly-once delivery
  machinery — this is the standard, pragmatic production pattern.
- [Advanced] A consumer's processing of a message occasionally throws an exception. Design the
  error-handling strategy — what happens on failure, and how do you avoid either losing the
  message or getting permanently stuck reprocessing a "poison pill" message forever? → Don't let a
  single failure crash the consumer or silently skip-and-lose the message. Standard pattern: (1)
  **retry** the message a bounded number of times, ideally with backoff, for transient failures
  (a downstream DB blip); (2) if retries are exhausted, route the message to a **Dead Letter
  Topic (DLT)** — publish it to a separate `topic-name.DLT` with error metadata (exception,
  timestamp, original topic/partition/offset) — and only **then** commit the offset on the original
  topic, so the consumer group can move past the bad message instead of retrying it forever
  (a "poison pill" — one malformed message that always throws, which without a DLT would otherwise
  block that partition's consumption indefinitely since the offset never advances past it). (3) Set
  up monitoring/alerting on DLT volume so someone actually investigates messages landing there,
  and a process (manual or automated, after a fix) to **replay** DLT messages back into the main
  topic once the root cause is addressed. Spring Kafka supports this pattern natively via
  `DefaultErrorHandler` with a configured `DeadLetterPublishingRecoverer` and a retry `BackOff`
  policy. → Follow-up: *Why does committing the offset only after routing to the DLT (not before)
  matter for correctness?* If the offset were committed **before** confirming the DLT publish
  succeeded, a crash between those two steps would lose the message entirely (offset already
  advanced, but neither successfully processed nor safely captured in the DLT) — committing only
  after the DLT publish succeeds preserves the "the message is accounted for somewhere" guarantee
  even in a failure-of-the-failure-handler scenario.

## Distributed Tracing

- [Intermediate] What problem does distributed tracing solve that centralized logging alone
  doesn't, in a microservices architecture? → A single user request in a microservices system
  typically fans out across many services (gateway → order-service → inventory-service →
  payment-service → notification-service) — centralized logs from each service show what happened
  **within that service**, but correlating "which log lines across 5 different services all belong
  to this one specific slow/failed user request" is nearly impossible from logs alone unless every
  service consistently logs some shared identifier. Distributed tracing (OpenTelemetry, historically
  Sleuth/Zipkin in the Spring ecosystem) solves this by propagating a **trace ID** (identifying the
  whole end-to-end request) and per-hop **span IDs** (identifying each individual service call, with
  parent/child relationships) through HTTP headers/message headers across every service boundary,
  letting a tracing backend (Zipkin, Jaeger, or a vendor APM) reconstruct the full **call graph**
  for one request — showing exactly which hop took how long, and where a failure actually
  originated in a multi-service chain. → Follow-up: *How does the trace ID actually get propagated
  from service A to service B in an HTTP call?* Via standard trace-context HTTP headers
  (W3C Trace Context's `traceparent` header is the modern standard;
  older systems used vendor-specific headers like `X-B3-TraceId`) — an instrumentation library
  (Micrometer Tracing / OpenTelemetry's auto-instrumentation) automatically injects these headers
  into outgoing calls and extracts them from incoming ones, so application code usually doesn't
  need to manually thread trace IDs through every call by hand.
- [Advanced] You see a trace showing a request took 2 seconds total, but no single span in the
  trace is longer than 200ms. What does that tell you, and how would you investigate further? →
  If the sum of visible span durations doesn't account for the total elapsed time, the "missing"
  time is happening **between** spans — commonly: (1) **queueing time** before a span even starts
  (a request sitting in a thread pool's work queue waiting for a worker thread — visible as a gap
  between when the parent span started a child call and when the child span itself begins, if your
  tracing captures that distinction, e.g. client-side vs server-side span timestamps), (2)
  **connection pool wait time** (waiting to acquire a DB or HTTP connection from an exhausted pool
  before the actual call — which the call's own span duration wouldn't include if the timer starts
  only once the connection is acquired), or (3) a genuinely **untraced** hop — a call to something
  the tracing instrumentation doesn't cover (a raw JDBC call without auto-instrumentation, an
  uninstrumented internal library, a synchronous call inside a thread pool task that isn't
  correctly propagating trace context into that new thread). Investigation: check connection pool
  metrics (HikariCP pending-connections count) for the relevant time window, check thread pool
  queue depth/saturation metrics, and audit whether every hop in the actual code path has tracing
  instrumentation attached — a gap in the trace is itself a strong signal to look for an
  uninstrumented or queued/blocked segment rather than assuming the visible spans are the complete
  picture. → Follow-up: *Why does trace context propagation commonly break specifically when work
  is handed off to a new thread (e.g. inside a `CompletableFuture.supplyAsync` or an
  `@Async` method)?* Trace context is typically stored in a `ThreadLocal`-based context — when work
  is submitted to a different thread (an executor's worker thread), that thread doesn't
  automatically inherit the calling thread's `ThreadLocal` state unless the executor/async
  mechanism is specifically wrapped or instrumented to propagate it (Micrometer Tracing and similar
  libraries provide context-propagating executor wrappers for exactly this reason) — using a plain,
  uninstrumented `ExecutorService` silently breaks the trace chain at that hand-off point, showing
  up as a disconnected or missing span.

---

**File question count: 15** ([Basic] 2, [Intermediate] 9, [Advanced] 4)
