# Spring Data JPA — N+1, Lazy/Eager, Transactions, Locking, Projections

This is usually the highest-signal file in the whole module — almost every backend interview at
the 3+ YOE mark includes at least one "explain the N+1 problem" or "what's the difference between
`REQUIRED` and `REQUIRES_NEW`" question, because these are exactly the things that silently work
fine in dev with 5 rows of test data and fall over in production. This file covers the N+1 query
problem, lazy vs eager loading pitfalls, `@Transactional` propagation and isolation, first/second-
level cache, optimistic vs pessimistic locking, and projections/DTOs vs entities.

## N+1 Query Problem

- [Basic] What is the N+1 query problem, and give a concrete example. → It's when fetching N parent
  entities triggers 1 query for the parents plus N additional queries — one per parent — to fetch
  each one's lazily-loaded association, instead of one efficient join. Example: `findAll()` on
  `Order` (1 query, returns 50 orders) then looping `order.getCustomer().getName()` for each order
  — because `customer` is `LAZY`, each access fires a separate `SELECT * FROM customer WHERE
  id = ?`, totaling 1 + 50 = 51 queries where one join query would've sufficed. → Follow-up: *Why
  doesn't this show up as a bug in local testing with a handful of rows?* With 5 test rows, 1+5=6
  queries execute in milliseconds either way — the problem is purely about **scale**: 1+500 queries
  against a real production dataset, each paying real network round-trip latency to the DB, turns
  a sub-100ms endpoint into a multi-second one, which local dev/test data almost never surfaces.
- [Intermediate] How do you detect N+1 problems before they hit production? → (1) Enable SQL
  logging in dev/test (`spring.jpa.show-sql=true` plus
  `logging.level.org.hibernate.SQL=DEBUG`, or better, `logging.level.org.hibernate.orm.jdbc.bind=TRACE`
  for parameter values) and actually read the query count for a given code path during code review
  or manual testing — a suspicious pattern is the same `SELECT ... WHERE id = ?` shape repeated many
  times in a row. (2) Use a dedicated tool like **datasource-proxy** or **p6spy** in test
  environments to assert query counts in integration tests (e.g. "fetching this endpoint should
  execute at most 3 queries, not 1+N"). (3) Hibernate's
  `hibernate.query.fail_on_pagination_over_collection_fetch` and statistics APIs
  (`SessionFactory.getStatistics()`) can programmatically surface query counts. In practice, the
  most reliable habit is simply reviewing generated SQL for any endpoint that iterates over a
  collection and touches an association. → Follow-up: *Why is this hard to catch with just APM
  latency monitoring alone?* APM shows *that* an endpoint is slow and *how much total DB time* it
  used, but often aggregates many small fast queries into a total that doesn't obviously scream
  "N+1" the way it would if you actually looked at the query **count** per request — a good APM
  with per-request span breakdowns (many small near-identical spans) helps, but reading the raw
  query log/count directly is still the most unambiguous signal.
- [Intermediate] What are the main fixes for N+1, and what's the trade-off of each? →
  **`JOIN FETCH`** in JPQL (`SELECT o FROM Order o JOIN FETCH o.customer`) — one query, eager for
  that specific query only (doesn't change the entity's default fetch type), but joining multiple
  collections in one query risks a **cartesian product** (row multiplication — see below).
  **`@EntityGraph`** — declaratively specifies which associations to eagerly fetch for a given
  repository method, without writing custom JPQL; cleaner for simple cases, less flexible than
  hand-written JPQL for complex conditions. **Batch fetching**
  (`@BatchSize(size = 20)` on the association, or the global
  `hibernate.default_batch_fetch_size` property) — instead of one query per lazy association
  access, Hibernate batches up to N pending lazy loads into a single `WHERE id IN (?, ?, ...)`
  query; turns 1+N into roughly `1 + N/batchSize` queries — a good default-safety-net fix that
  doesn't require touching every query site. **DTO projections** (see below) — skip entity loading
  and associations entirely, select exactly the columns needed with a join in one query. →
  Follow-up: *Why is `@BatchSize` often the pragmatic team-wide fix over `JOIN FETCH` everywhere?*
  It's a one-line annotation on the entity/association that fixes N+1 for **every** query path that
  touches that association, present and future, without every developer having to remember to
  write `JOIN FETCH` correctly at every call site — `JOIN FETCH` is more precise/efficient per
  query but requires deliberate per-query effort and is easy to forget on a new code path.
- [Advanced] What's the "cartesian product" problem when using `JOIN FETCH` on two collection
  associations in the same query, and how do you avoid it? → `SELECT o FROM Order o JOIN FETCH
  o.items JOIN FETCH o.payments` where an order has 5 items and 3 payments doesn't return 1 row per
  order — SQL joins produce the **cross product** of the two collections, returning 5×3=15 rows for
  that single order (each row duplicating the order and one item and one payment), which Hibernate
  then has to deduplicate in memory when materializing the `Order` entities — wasteful data transfer
  and processing, and can cause `MultipleBagFetchException` at startup if both associations are
  `List` (non-unique bags) rather than `Set`. Fixes: only `JOIN FETCH` **one** collection
  association per query (fetch the second via a separate query or batch fetching), fetch one
  collection via `JOIN FETCH` and use `@BatchSize` for the other, or restructure to two targeted
  queries instead of one query trying to eagerly load everything. → Follow-up: *Why does
  `MultipleBagFetchException` specifically mention "bags," and what's a bag in Hibernate
  terminology?* A "bag" is Hibernate's term for a `List`-typed collection mapping with no explicit
  ordering/uniqueness guarantee (unlike `Set`, which is unique, or a `List` with `@OrderColumn`,
  which is indexed) — Hibernate can't cleanly deduplicate/reconstruct two simultaneously-fetched
  bags from a joined cartesian result because it has no way to tell which combination of rows
  belongs to which original collection element without a stable order or uniqueness key, so it
  refuses outright rather than risk silently wrong results.

## Lazy vs Eager Loading

- [Basic] What's the default fetch type for `@OneToMany`/`@ManyToMany` versus `@ManyToOne`/`@OneToOne`,
  and why are they different? → Collection associations (`@OneToMany`, `@ManyToMany`) default to
  **`LAZY`**; single-valued associations (`@ManyToOne`, `@OneToOne`) default to **`EAGER`**. The
  reasoning: a collection could be unboundedly large (an order with thousands of line items) so
  loading it eagerly by default risks massive, unpredictable data pulls; a `@ManyToOne`/`@OneToOne`
  reference is a single row, cheap and bounded, so eager-by-default was judged safe by the JPA spec
  — though in practice, many teams override `@ManyToOne` to `LAZY` explicitly too, since even a
  "cheap" eager single-row fetch adds up when you don't actually need that related entity for a
  given use case, and it avoids inconsistent fetch behavior across the codebase. → Follow-up:
  *Why would a team make `@ManyToOne` lazy by default across the whole codebase as a blanket
  policy?* Consistency and control — with everything explicitly `LAZY`, every eager load becomes a
  deliberate, visible `JOIN FETCH`/`@EntityGraph` decision at the query site, rather than some
  associations silently eager-loading extra data by default depending on annotation type, which
  makes N+1 and unnecessary-fetch problems easier to reason about and catch in review.
- [Intermediate] What causes `LazyInitializationException`, and what's the actual root cause versus
  the superficial symptom? → Superficial symptom: you call `order.getItems()` on an `Order` entity
  outside of the Hibernate session/persistence context that originally loaded it (e.g. the
  transaction/session has already closed — commonly, the `@Transactional` service method returned,
  the entity got serialized to JSON in the controller layer, or passed to a view template after the
  request's transaction ended), and Hibernate can't lazily fire the SQL to populate that
  collection because there's no active session to run the query against — it throws
  `LazyInitializationException`. Root cause: the entity **escaped its persistence context boundary**
  before all the data it needs was accessed — this is fundamentally an architecture/layering issue
  (returning entities directly from a `@Transactional` boundary to a layer that isn't
  transaction-aware), not really a "forgot to eager fetch" issue, even though eager-fetching the
  specific association is often the quick patch. → Follow-up: *What's the actual production
  scenario where this most commonly bites teams that "worked fine in testing"?* Serializing a JPA
  entity directly to JSON from a `@RestController` — Jackson walks the entire object graph,
  including lazy associations, **after** the `@Transactional` service method has already returned
  and closed the session; it works in a quick manual test if you happen to only touch already-
  initialized fields, then throws in production the first time a response needs a field that wasn't
  already loaded.
- [Intermediate] What is "Open Session In View" (OSIV), and why is it controversial? → OSIV
  (`spring.jpa.open-in-view`, **`true` by default** in Spring Boot) keeps the Hibernate
  session/persistence context open for the **entire HTTP request**, not just the
  `@Transactional` service method — so lazy associations can still be initialized later in the
  request (e.g. during JSON serialization in the controller layer) without throwing
  `LazyInitializationException`. It's controversial because it papers over the architectural
  problem above rather than fixing it: it makes N+1 queries **invisible in the service layer**
  (they silently fire later, during serialization, outside your transaction boundary and outside
  your mental model of "the DB work happens in the `@Transactional` method"), keeps a DB
  connection checked out from the pool for the full request duration including view
  rendering/serialization time (reducing effective pool capacity under load), and generally hides
  bugs that would otherwise surface clearly and early. Many teams explicitly disable it
  (`spring.jpa.open-in-view=false`) and instead fix the root cause — fetch everything needed within
  the transactional service method, using DTOs/projections at the service boundary so entities never
  need to leave the transaction at all. → Follow-up: *What's the immediate consequence of setting
  `open-in-view=false` in an existing codebase that relies on it?* Every place that lazily accesses
  an association outside the original `@Transactional` method (in a controller, in JSON
  serialization, in a view template) will start throwing `LazyInitializationException` — which is
  the point (surfacing the previously-hidden problem), but it means a real migration effort:
  auditing and fixing each such access site with `JOIN FETCH`/`@EntityGraph`/DTOs before flipping
  the flag in production.
- [Advanced] You have `@ManyToOne(fetch = FetchType.LAZY) private Customer customer;` — is it
  actually lazy at the bytecode level, and what's the mechanism? → Yes, but it requires **bytecode
  enhancement** (or, historically, a runtime proxy) to actually defer the load — Hibernate generates
  a dynamic **proxy** subclass of `Customer` at runtime (via ByteBuddy) that's assigned to the field
  instead of a real `Customer` instance; the proxy holds only the entity's ID and intercepts every
  method call, triggering the real `SELECT` on **first non-identifier method access**
  (`customer.getName()` triggers it; `customer.getId()` does **not**, since the ID is already known
  without hitting the DB — a subtle but useful fact: you can check "does this order have a
  customer" and get the ID without ever firing the lazy load). → Follow-up: *What breaks if you
  call `instanceof Customer` or try to cast a lazily-loaded reference obtained this way in certain
  contexts?* A Hibernate proxy's runtime class is a **generated subclass** (e.g.
  `Customer$HibernateProxy$xyz`), not `Customer.class` itself — `object.getClass() == Customer.class`
  returns `false` for a proxy even though `instanceof Customer` still returns `true` (subclass
  relationship is preserved); code that does exact-class equality checks or reflection assuming the
  literal runtime class instead of using `instanceof`/`Hibernate.getClass()` can behave unexpectedly
  on lazy-loaded, still-uninitialized proxies.

## @Transactional Propagation & Isolation

- [Basic] What does `@Transactional` actually do mechanically — walk through what happens when you
  call a `@Transactional` method. → Because `@Transactional` is implemented via a
  `BeanPostProcessor`-installed AOP proxy (see `core-spring.md`), calling `someBean.method()` from
  **outside** the bean actually calls the proxy first: the proxy starts a new DB transaction (or
  joins an existing one, per propagation — see below) before invoking the real method, then either
  commits the transaction if the method returns normally, or rolls it back if the method throws an
  exception matching the configured rollback rules (unchecked exceptions by default), then returns
  control to the caller. → Follow-up: *Why does calling a `@Transactional` method from another
  method in the same class not start a transaction?* Self-invocation (`this.method()`) bypasses the
  proxy entirely — Java doesn't route an internal method call through the proxy wrapper, so the AOP
  advice (transaction start/commit/rollback) never triggers; the call runs on the raw object
  directly with no transaction management at all.
- [Intermediate] What's the difference between `REQUIRED`, `REQUIRES_NEW`, and `NESTED`
  propagation, and give a scenario where each matters? → **`REQUIRED`** (default) — join the
  existing transaction if one is active, or start a new one if not; a rollback anywhere in the
  chain rolls back the whole thing. **`REQUIRES_NEW`** — always suspend any existing transaction
  and start a brand-new, fully independent one; the outer transaction is paused (not part of the
  inner one at all) and resumes after the inner one commits/rolls back independently. Scenario:
  writing an **audit log** that must persist even if the main business operation later fails and
  rolls back — wrap the audit-log write in `REQUIRES_NEW` so its commit is durable regardless of
  what the outer transaction ultimately does. **`NESTED`** — starts a true database **savepoint**
  within the *same* physical transaction/connection (not a separate transaction) — if the nested
  portion fails, only the work since that savepoint rolls back, while the outer transaction can
  continue and still commit its other work; unlike `REQUIRES_NEW`, the outer transaction's rollback
  still rolls back the nested savepoint's changes too (it's not independent — it's a sub-scope of
  the same transaction). Scenario: processing a batch of 100 items where a failure on item 47
  shouldn't roll back items 1-46 — wrap each item's processing in `NESTED` so failures are scoped
  per-item. → Follow-up: *Does `NESTED` work with every JPA provider/database?* No — it requires
  JDBC savepoint support, and not every combination supports it cleanly with JPA (Hibernate support
  for `NESTED` has historically had rough edges); it's less universally reliable than `REQUIRED`/
  `REQUIRES_NEW`, so many teams avoid it in favor of explicit per-item error handling with
  `REQUIRES_NEW` or manual try/catch around individual saves instead.
- [Advanced] You call a `REQUIRED` method from inside another `REQUIRED` method, and the inner one
  throws an exception that you catch and swallow in the outer method. What actually happens at
  commit time, and why? → Because both methods join the **same physical transaction**
  (`REQUIRED` propagation), the moment the inner method throws an exception matching Spring's
  default rollback rule (any unchecked exception), the proxy around the *inner* call marks the
  entire physical transaction as **rollback-only** (`setRollbackOnly()`) immediately, even though
  the exception itself is then caught and swallowed by the outer method's code. When the outer
  method later returns normally (no exception propagates out of it) and its own proxy tries to
  **commit**, Spring sees the transaction is already marked rollback-only and throws
  `UnexpectedRollbackException` instead of committing — the whole transaction, including the outer
  method's otherwise-successful work, rolls back. This is one of the most commonly hit
  `@Transactional` production bugs: "I caught the exception, why did everything still roll back?" —
  because catching the exception in your code doesn't un-mark a transaction that's already been
  flagged rollback-only at the framework level. → Follow-up: *What's the correct fix if you
  genuinely want the inner operation's failure to be swallowed without affecting the outer
  transaction's other work?* Call the inner operation with `REQUIRES_NEW` propagation instead, so
  it runs in a fully independent physical transaction — its rollback then only affects its own
  work, leaving the outer transaction free to commit normally; simply catching the exception
  without changing propagation does not achieve this.
- [Intermediate] Explain the four standard isolation levels and the concrete anomaly each one
  prevents that the level below it doesn't. → **`READ_UNCOMMITTED`** — prevents nothing; allows
  **dirty reads** (reading another transaction's uncommitted, possibly-to-be-rolled-back changes).
  **`READ_COMMITTED`** — prevents dirty reads (you only ever see committed data), but allows
  **non-repeatable reads** (re-reading the same row twice in one transaction can see different
  values if another transaction committed a change in between). **`REPEATABLE_READ`** — prevents
  non-repeatable reads (the same row read twice within a transaction returns the same value), but
  can still allow **phantom reads** (a range query re-run in the same transaction can return
  additional/fewer *rows* if another transaction inserted/deleted matching rows in between — the
  individual rows you already read stay stable, but the *set* can grow). **`SERIALIZABLE`** —
  prevents phantom reads too; transactions behave as if executed one at a time in some serial
  order, the strongest (and slowest, most lock/contention-heavy) guarantee. → Follow-up: *What
  isolation level does MySQL (InnoDB) default to, and how does that differ from most other
  databases?* `REPEATABLE_READ` (InnoDB's default, notably preventing most phantom reads too via
  its gap-locking/MVCC snapshot mechanism, more than the SQL standard strictly requires at that
  level) — most other major databases (PostgreSQL, Oracle, SQL Server) default to
  `READ_COMMITTED`, so assuming MySQL's isolation behavior transfers unchanged to another database
  is a common, subtle portability bug.

## First-Level vs Second-Level Cache

- [Basic] What is the first-level (L1) cache, and can it be disabled? → The L1 cache is the
  **persistence context itself** — within a single Hibernate session/transaction, loading the same
  entity by ID twice returns the *same in-memory object instance* on the second call without
  hitting the DB again (Hibernate tracks loaded entities by ID within the session). It's mandatory
  and session-scoped — you can't disable it, but it's automatically cleared/discarded when the
  session/transaction ends (it does not persist across requests or transactions). →
  Follow-up: *Does the L1 cache help with the N+1 problem?* No — N+1's extra queries are for
  **different** entities (each order's *different* customer, mostly), so there's nothing for the L1
  cache to reuse; L1 caching only helps when you load the exact same entity by ID multiple times
  within one session.
- [Intermediate] What is the second-level (L2) cache, how does it differ from L1, and what's a
  concrete risk of enabling it carelessly? → L2 cache (e.g. Ehcache, Caffeine, Redis via a Hibernate
  second-level cache provider) is **shared across sessions/transactions**, potentially across the
  whole application (or even across app instances, if backed by a distributed cache like Redis) —
  unlike L1, it survives beyond a single transaction. Enabled per-entity (`@Cacheable` +
  a cache concurrency strategy like `READ_WRITE` or `NONSTRICT_READ_WRITE`). Concrete risk:
  **staleness** — if another process/instance updates the underlying row directly (a batch job, a
  raw SQL script, or even the same app running on multiple instances without a shared/coordinated
  cache), the L2 cache can keep serving stale data until it's invalidated, and getting cache
  invalidation genuinely correct across a distributed multi-instance deployment is nontrivial (it's
  why many teams stick to a well-tested distributed cache provider rather than the default local
  in-JVM implementation for any multi-instance deployment). → Follow-up: *For what kind of data is
  L2 caching actually a good fit, and for what kind is it dangerous?* Good fit: mostly-static,
  read-heavy reference data (a list of countries, product categories, configuration lookups) that
  rarely changes and where slight staleness is tolerable. Dangerous: frequently-updated
  transactional data (account balances, inventory counts, order status) where staleness directly
  causes incorrect business decisions — caching those requires very careful, deliberate invalidation
  design, not a blanket `@Cacheable` applied everywhere.

## Optimistic vs Pessimistic Locking

- [Basic] What's the difference between optimistic and pessimistic locking, and what does
  `@Version` do? → **Optimistic locking** assumes conflicts are rare — it doesn't lock the row in
  the DB at all during the read; instead, a `@Version` column (an integer/timestamp Hibernate
  auto-increments on every update) is checked at `UPDATE` time: the generated SQL is
  `UPDATE ... SET version = version+1, ... WHERE id = ? AND version = <version read earlier>` — if
  another transaction updated the row in between (bumping the version), the `WHERE` clause matches
  zero rows, and Hibernate throws `OptimisticLockException`/`ObjectOptimisticLockingFailureException`,
  telling the application the data changed underneath it. **Pessimistic locking**
  (`@Lock(LockModeType.PESSIMISTIC_WRITE)`, translating to `SELECT ... FOR UPDATE`) actually locks
  the row at the database level the moment it's read, blocking any other transaction from
  reading-for-update or writing that row until the lock-holding transaction commits/rolls back. →
  Follow-up: *Which is the better default choice for a typical web app, and why?* Optimistic —
  most web app conflicts are actually rare (two users editing the exact same row at the exact same
  moment is uncommon for most domains), and optimistic locking doesn't hold DB locks across the
  (often much longer, user-think-time-inclusive) duration of a web request, avoiding the throughput
  and deadlock risk of holding real row locks; pessimistic locking is reserved for genuinely
  high-contention, must-not-race scenarios.
- [Advanced] Give a concrete production scenario where pessimistic locking is the right choice over
  optimistic, and explain why optimistic would fail there. → A **seat-booking** or
  **inventory-decrement** system under genuinely high contention on the *same* row — e.g. the last
  remaining ticket for a popular event, where dozens of concurrent requests race to decrement the
  same `available_seats` row within milliseconds of each other. With optimistic locking, most of
  those concurrent requests would all read the same version, all attempt to update, and **all but
  one would fail** with `OptimisticLockException` — technically correct (no lost update), but it
  pushes the burden of retry logic onto the application for what could be a very high failure rate
  under this specific hot-row contention pattern, and a naive retry-in-a-loop can itself become a
  thundering herd. Pessimistic locking instead **serializes** access to that specific row directly
  at the DB — each request queues briefly for the row lock rather than racing and failing, which is
  simpler to reason about and avoids the retry-storm risk, at the cost of some added latency (waiting
  for the lock) and increased deadlock risk if lock ordering across multiple rows isn't disciplined.
  → Follow-up: *What's a third alternative to both, specifically for a simple "decrement a
  counter safely" case like available seats?* An atomic, conditional SQL update issued directly —
  `UPDATE seats SET available = available - 1 WHERE event_id = ? AND available > 0`, checking the
  affected row count afterward — avoids both a held row lock and optimistic-retry storms for this
  narrow but very common "decrement if available" pattern, at the cost of it being a hand-rolled
  query rather than something JPA's locking annotations express directly.

## Projections / DTOs vs Entities

- [Basic] Why prefer a DTO/projection over returning a full JPA entity from a service method that
  only needs a couple of fields? → Loading a full entity pulls every mapped column (and can trigger
  lazy-association proxies, cascades, dirty-checking overhead) even if the caller only needs, say,
  `id` and `name` — wasteful for both DB bandwidth and JVM memory at scale. A DTO/interface
  projection lets Hibernate generate a `SELECT id, name FROM ...` that fetches **only** the needed
  columns, with no entity/proxy machinery, no lazy-loading traps, and (since it's not a managed
  entity) no accidental dirty-checking side effects from mutating it later in the request. →
  Follow-up: *What's a risk of returning managed entities directly from a `@RestController`, beyond
  the performance cost?* Accidentally exposing internal-only fields (a password hash, an internal
  audit column) to the API response if Jackson serializes the whole entity by default, and coupling
  your public API contract tightly to your internal DB schema — a column rename/refactor in the
  entity directly breaks the API response shape unless every field is deliberately reviewed for
  `@JsonIgnore`.
- [Intermediate] What's the difference between an interface-based projection and a
  class-based (DTO) projection in Spring Data JPA? → **Interface-based** projection
  (`interface UserSummary { String getName(); String getEmail(); }`, used as the repository
  method's return type) lets Spring Data generate a dynamic proxy at runtime backed by the query
  results — concise, no extra class body needed, but limited to simple getter-shaped mappings and
  can be less obvious what's actually fetched at a glance. **Class-based** projection (a DTO with a
  constructor matching the JPQL constructor expression,
  `SELECT new com.example.UserSummaryDto(u.name, u.email) FROM User u`) is more explicit, supports
  custom logic in the constructor, is easier to unit test as a plain POJO, and its exact shape is
  visible directly in the DTO class rather than inferred from an interface's getters — most teams
  prefer class-based DTOs for anything beyond the most trivial read projections. →
  Follow-up: *Can interface projections be "closed" or "open," and what's the difference?* A
  **closed** projection's getter names map directly to entity property names (fully derived by
  Spring Data, most efficient — becomes a real column-limited SQL projection); an **open**
  projection uses a SpEL expression in `@Value` on a getter to compute a derived value (e.g.
  concatenating first/last name) — but this forces Hibernate to fetch the **full entity** first and
  then apply the SpEL expression in memory, losing the "only fetch needed columns" performance
  benefit that's the whole point of projections in the first place.
- [Intermediate] When would you deliberately keep using full entities instead of switching
  everything to DTO projections? → When the code path actually needs to **mutate and persist**
  changes — DTOs/projections are read-only views with no dirty-checking or `save()` semantics; any
  write path (update an entity's fields, let Hibernate's automatic dirty-checking flush changes at
  transaction commit) genuinely needs a managed entity. Also, for small, simple aggregates
  accessed as a whole (not just 2 of 15 columns), the overhead difference is negligible and the
  extra DTO class is arguably needless boilerplate — the projection optimization earns its keep
  specifically on wide entities, read-heavy list endpoints, and any hot path where profiling shows
  the extra columns/entity overhead actually matters. → Follow-up: *Is it reasonable to use entities
  internally within the service/repository layer but always map to DTOs at the API boundary?* Yes —
  that's a common, reasonable default: keep entities for the write/business-logic layer where
  JPA's managed-entity behavior earns its keep, but never let a raw entity cross the API boundary
  directly, mapping to a purpose-built response DTO at the controller layer (via a mapper, MapStruct,
  or manual construction) regardless of whether the underlying query used a projection or a full
  entity fetch.

---

**File question count: 16** ([Basic] 4, [Intermediate] 8, [Advanced] 4)
