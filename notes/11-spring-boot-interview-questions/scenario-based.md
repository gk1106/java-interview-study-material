# Scenario-Based Questions — Production Debugging & Operations

These are the questions that actually separate candidates who've operated a real production
service from those who've only built features in a sandbox. Interviewers ask them precisely
because a memorized definition of `@Transactional` propagation doesn't tell them whether you'd
know what to check first when paged at 2am. Each answer below is structured the way you should
actually answer it in an interview: **what you'd check first, second, third** — not a one-line
guess — because that structured triage process is exactly what's being evaluated, more than
whether you land on the "correct" root cause immediately.

- [Advanced] **Your API's p99 latency has tripled over the last hour with no deployment. What do
  you check, in order?** →
  1. **Check dashboards for the blast radius first**: is it one endpoint or all of them? One
     instance or all instances? This immediately narrows "shared infrastructure problem" (DB,
     downstream, network) versus "code path problem" (one endpoint doing something newly
     expensive) versus "one bad instance" (a stuck GC, a leaked connection on that instance alone).
  2. **Check what actually changed around the time it started** — even with "no deployment," check
     for: a scheduled batch job that kicked off and is now competing for DB/CPU resources, a
     traffic pattern change (a marketing push, a bot/scraper spike) visible in request-rate
     dashboards, an upstream/downstream service that *did* deploy and is now responding slower, or
     a slow-building resource leak (connection pool exhaustion, memory pressure) that finally
     crossed a threshold rather than a sudden step change.
  3. **Check resource saturation** — CPU, memory/GC pause time, thread pool utilization
     (`/actuator/threaddump` or metrics), DB connection pool (`hikaricp.connections.pending`/
     `.active`) — a saturated connection pool with otherwise-idle CPU strongly points at a
     downstream/DB bottleneck rather than application code.
  4. **Pull a thread dump** if threads are saturated — see what the bulk of busy threads are
     actually blocked on (a slow downstream call, a lock, a DB query) rather than guessing.
  5. **Check downstream/DB query latency directly** — a slow query that recently started scanning
     more rows (data growth crossing a point where an index stopped being selective, or a new query
     pattern introduced without an index) is one of the most common silent causes of a latency
     creep with no deploy.
  6. **Check for a slow memory leak causing GC pressure** — rising GC pause time/frequency over the
     preceding hours, visible in heap usage sawtooth patterns not returning to baseline, can
     gradually degrade every request's latency without any single obvious trigger. →
  Follow-up: *If step 1 shows it's affecting exactly one of five identical instances, what does
  that change about your investigation?* It points strongly at something instance-local rather than
  a shared dependency — a stuck GC on that JVM, a connection leak specific to that instance's
  runtime state, or a hung thread holding a lock that instance's own requests are queuing behind;
  the fix is often just restarting/replacing that instance to restore service while investigating
  the root cause offline from its logs/thread dumps, rather than treating it as a systemic issue.

- [Advanced] **A service is throwing intermittent 500s under load, but works fine at low traffic
  and passes all your tests. How do you debug it?** →
  1. **Reproduce the failure pattern first** — get the actual exception/stack trace from logs for
     the failing requests (correlate by trace ID if tracing is set up); "intermittent under load"
     without the actual error is just a guess. Common categories once you see the real exception:
     connection pool exhaustion (`Unable to acquire JDBC Connection`), a race condition/thread-
     safety bug (a shared mutable field on a singleton bean that isn't actually thread-safe, only
     exposed once concurrent requests interleave), a timeout against a downstream that's also
     struggling under the same load, or resource exhaustion (file descriptors, thread pool
     saturation triggering a rejection).
  2. **Check whether it correlates with pool/resource exhaustion** — if it's connection-pool related,
     it explains "works at low traffic" perfectly: below the pool's capacity, every request gets a
     connection quickly; above it, requests start timing out waiting for one, and those timeouts
     surface as 500s. Check `HikariCP` metrics for `connections.pending` spikes correlating with
     the 500 spikes.
  3. **Check for shared mutable state in a singleton bean** — a classic bug that only manifests
     under real concurrency: a `@Service` (singleton by default) with a non-thread-safe instance
     field (a plain `HashMap`, a mutable counter, a `SimpleDateFormat` instance reused across
     threads) that works "by luck" at low concurrency (requests rarely truly overlap) and corrupts/
     throws under real concurrent load. Review recently-changed service classes for instance
     fields that aren't either immutable or explicitly thread-safe.
  4. **Check downstream call timeouts and their configured values** — under load, a downstream that
     itself slows down can start exceeding your client's timeout, and if that's not handled
     gracefully (caught and mapped to a clean error, or protected by a circuit breaker), it
     surfaces as an uncaught exception → 500 instead of a clean, expected error response.
  5. **Try to reproduce under controlled load** (a load test in staging, or even a tight
     concurrent-request loop locally against a test instance) — since it doesn't show up in normal
     tests, a deliberate concurrency/load test is often the only way to reliably reproduce and
     confirm the fix before it ships back to production. → Follow-up: *Why would a race condition
     bug like this pass code review and unit tests but only surface in production under load?*
     Unit tests typically exercise a class single-threaded, sequentially — a shared mutable field
     bug requires **genuine concurrent access** to manifest (two threads interleaving on the same
     unsynchronized state at the same moment), which single-threaded tests structurally cannot
     trigger regardless of how thorough they are; it takes either a deliberate concurrency test or
     real production load to expose it.

- [Advanced] **How do you roll out a schema migration (e.g. renaming a column, adding a NOT NULL
  constraint) with zero downtime on a live production database?** →
  1. **Never do a breaking schema change in one step** — the core principle is: old code and new
     code must both be able to run correctly against the database at every intermediate point during
     a rolling deployment, since a rolling deploy means old and new application instances run
     **simultaneously** against the same database for some window.
  2. **For a column rename**: (a) add the **new** column alongside the old one (additive, safe —
     old code ignores it, new code doesn't need it yet); (b) deploy application code that writes to
     **both** columns (dual-write) while still reading from the old one; (c) backfill the new
     column for existing rows (in batches, to avoid a long-held lock/large transaction on a live
     table); (d) deploy application code that reads from the **new** column (writes can stay dual
     or move fully to new, depending on rollback safety needs); (e) once fully migrated and verified
     stable, deploy a final version that stops writing to the old column; (f) only **then**, in a
     later, separate deployment, drop the old column — never in the same deploy as the read/write
     cutover, so you retain a fallback if something's wrong.
  3. **For adding a `NOT NULL` constraint**: add the column as **nullable** first, backfill a
     default/computed value for all existing rows (batched), deploy application code that always
     populates it going forward, verify no nulls remain, **then** add the `NOT NULL` constraint in
     a separate migration step — adding `NOT NULL` directly on a large live table in one step can
     also take a long lock depending on the database engine.
  4. **Use an online schema migration tool for large tables** (e.g. `gh-ost`/`pt-online-schema-
     change` for MySQL, or database-native online DDL where supported) if the table is large enough
     that even an additive `ALTER TABLE` would hold a problematic lock — these tools perform the
     change via a shadow table and a cutover, avoiding a long blocking lock on the live table.
  5. **Coordinate migration ordering with deployment ordering** — migrations should generally run
     **before** the application code that depends on them is deployed (so the new column exists
     before new code tries to write to it), and destructive migrations (dropping a column) should
     run well **after** the code that stopped using it is fully rolled out and stable, with enough
     of a gap to be confident a rollback of the app code won't need the dropped column back. →
  Follow-up: *What goes wrong if you add a `NOT NULL` column with no default directly, in one step,
  during a rolling deployment?* Old application instances (still running, mid-rollout) don't know
  about the new required column and will fail every insert with a constraint violation the moment
  the migration lands — the rolling deployment window where old and new code coexist is exactly
  where this breaks, even though it might look fine in a single-instance dev/test environment where
  there's no such window at all.

- [Advanced] **How do you debug a memory leak in a running Spring Boot application in production?**
  →
  1. **Confirm it's actually a leak, not just normal GC sawtooth or a legitimately larger working
     set** — look at heap usage over hours/days: normal behavior sawtooths (grows, GC reclaims,
     drops) but returns to roughly the same baseline after each major GC; a real leak shows the
     **post-GC baseline itself climbing steadily over time** even after full/major collections —
     that's the actual signal, not just "memory usage went up."
  2. **Capture a heap dump** from the affected instance (`jmap -dump` or configure
     `-XX:+HeapDumpOnOutOfMemoryError` ahead of time so a crash automatically captures one) —
     ideally capture **two** dumps some time apart under the same load conditions, so you can diff
     object counts and find what's specifically growing rather than analyzing one static snapshot.
  3. **Analyze the heap dump** with a tool (Eclipse MAT, VisualVM, or a cloud APM's heap analysis)
     — look at the "dominator tree"/retained-size view for what's consuming the most memory, and
     specifically what's **growing between the two dumps**; common real culprits: an
     unbounded in-memory cache/`HashMap` with no eviction policy that keeps accumulating entries, a
     listener/callback registered but never deregistered (each request adds one, nothing ever
     removes it), `ThreadLocal` values not cleaned up on thread-pool threads that get reused
     indefinitely (the `ThreadLocal` value outlives the request it was meant for, and since pool
     threads are long-lived and reused, it accumulates), or an entity/collection held onto
     unexpectedly long via a static field or an improperly-scoped bean.
  4. **Correlate with recent code changes** — check what changed around when the leak's growth
     rate started (a new cache added, a new listener registration, a new static collection) — this
     is often faster than heap-dump forensics alone if the timing lines up with a specific
     deployment.
  5. **Mitigate immediately in production while investigating** — a rolling restart of affected
     instances (on a schedule, or triggered by a memory threshold) buys time without fixing the
     root cause, but is a reasonable stopgap while the actual fix is being diagnosed and tested, as
     long as it's treated explicitly as a temporary mitigation, not a resolution. → Follow-up:
     *Why is `ThreadLocal` misuse a particularly sneaky category of leak in a Spring Boot app
     specifically, more so than in a short-lived-thread program?* Spring Boot's request handling
     runs on **pooled, long-lived** worker threads (the Tomcat thread pool) that are reused across
     many requests rather than created fresh per request — a `ThreadLocal` set during one request
     and not explicitly cleared at the end of that request **persists on that pooled thread** and
     can be silently visible to (or simply retained by) a completely unrelated later request handled
     by the same reused thread, making it both a memory leak risk and, worse, a potential data-
     leakage/correctness bug across requests, not just a memory concern.

- [Intermediate] **A downstream service your API depends on starts responding slowly (not down,
  just slow — 5+ second responses instead of 200ms). What's your triage, and how should your
  service have been designed to handle this gracefully?** →
  1. **Immediate triage**: check whether your service's own health/latency is degrading as a
     direct consequence (are your threads piling up waiting on that downstream, as described in the
     thread-pool-saturation scenario above) — if so, this is actively at risk of becoming a
     cascading outage of your own service, not just "one dependency is having a bad day."
  2. **Check whether a circuit breaker/timeout is actually configured and firing** — if requests to
     the slow downstream have no bounded timeout at all, every calling thread can be stuck waiting
     the full 5+ seconds (or longer, indefinitely, with no timeout configured at all) — this alone
     can exhaust your thread pool from what should be an isolated, contained problem.
  3. **How it should have been designed**: a bounded timeout on every downstream call (never rely
     on defaults, which are sometimes unbounded depending on the HTTP client), a circuit breaker
     that trips to fail-fast once the downstream is clearly degraded (stopping the pile-up
     immediately rather than continuing to attempt and wait on every request), a bulkhead limiting
     how many concurrent calls/threads can be tied up on this specific downstream so it can't
     starve calls to other, healthy dependencies sharing the same service, and — if the downstream
     is non-critical to the request's core function — a fallback (cached/default data) so the
     overall request still succeeds in degraded form instead of failing outright.
  4. **Communicate/escalate** — if it's a shared internal dependency, this is likely affecting other
     consuming services too; flagging it to the owning team (and checking if it's already a known,
     tracked incident) is as important as your own service's defensive handling. →
  Follow-up: *If your service had no circuit breaker configured for this dependency at the time of
  the incident, what's the fastest safe mitigation while a proper fix is deployed?* Reduce the
  configured timeout for that specific downstream call to something much tighter (fail fast rather
  than wait the full original timeout) as an emergency config change if it's externalized (not
  hard-coded), and/or scale up your own service's thread pool/instance count temporarily to absorb
  the extra held-thread time without exhausting capacity — both are stopgaps, not substitutes for
  actually adding a circuit breaker afterward.

- [Intermediate] **You need to deploy a breaking change to a REST API that mobile app clients
  (which you don't control the release cadence of) depend on. How do you do it without breaking
  users on old app versions?** →
  1. **Never break the existing contract in place** — old app versions will keep calling the
     existing endpoint/shape indefinitely (mobile app updates roll out slowly and unevenly; you
     cannot assume every client upgrades promptly, some never will).
  2. **Version the change** — introduce the breaking change as a **new** version (URI versioning
     `/api/v2/...` is simplest for mobile clients to reason about), keeping the old version fully
     functional and unchanged for existing clients.
  3. **Have both versions coexist**, often by having the new version's controller/service
     internally reuse shared business logic with the old one (avoid literally duplicating logic —
     duplicate the API-shape-specific translation layer, not the underlying domain logic) so a bug
     fix doesn't need to be applied in two places.
  4. **Set an explicit deprecation policy and timeline** for the old version — communicate to
     mobile team/product roughly when v1 will actually be retired (tied to something measurable,
     like "when v1 traffic drops below X% for N consecutive weeks" or a hard app-store-enforced
     minimum-version policy the mobile team can push), rather than leaving it running forever by
     default with no plan.
  5. **Monitor actual traffic to the old version** post-launch of the new one, so you know when it's
     genuinely safe to sunset rather than guessing. → Follow-up: *What's the danger of instead
     trying to make the *same* endpoint handle both old and new client expectations via optional
     fields and conditional logic, rather than a real version split?* It couples the two shapes
     together indefinitely in one code path, growing increasingly tangled conditional logic as more
     changes accumulate over time ("if this is an old client, do X; if new, do Y" scattered
     throughout), makes it hard to ever cleanly retire the old behavior since it's interwoven rather
     than isolated, and risks a change intended only for new clients accidentally affecting old
     ones (or vice versa) since they share the same code path rather than being cleanly separated.

- [Intermediate] **Your CI/CD pipeline deployed a bad change to production. Traffic is currently
  erroring for a subset of users. What's your immediate response, in order?** →
  1. **Stop the bleeding first, understand later** — the immediate priority is restoring service,
     not root-causing; **roll back** the deployment (redeploy the last known-good artifact/image)
     rather than trying to hot-fix forward under pressure, unless a rollback is somehow riskier
     than fixing forward (rare, but possible if the bad change included an irreversible data
     migration — see below).
  2. **Confirm the rollback actually resolves the symptom** — watch error rates/latency dashboards
     immediately after rollback to confirm recovery, rather than assuming it worked; if a
     concurrent schema migration was part of the same deploy, a simple app-code rollback might not
     be sufficient (see the zero-downtime-migration scenario — this is exactly why migrations should
     be backward-compatible with the previous app version).
  3. **Communicate status** — update whatever incident channel/status page process exists, even
     briefly, so stakeholders know it's being actively handled and roughly what's affected.
  4. **Only after service is restored**, do the actual root-cause investigation — pull logs/traces
     from the bad deployment window, understand exactly what broke and why it wasn't caught by
     CI/tests/staging.
  5. **Write it up** (a blameless postmortem) — what broke, why existing safeguards (tests, staging,
     canary/gradual rollout if you have one) didn't catch it, and concrete follow-up actions
     (a missing test case, a gap in the deployment process, a monitoring alert that should have
     fired sooner but didn't) to reduce the chance of a repeat. → Follow-up: *Why is "roll back
     first, investigate after" almost always the right order, even if you're fairly confident you
     know what's wrong and could fix it forward quickly?* Under incident pressure, "I think I know
     the fix" has a real chance of being wrong or incomplete, and a forward-fix attempt that also
     fails extends the outage further while you now also have to reason about *two* bad
     deployments; a rollback to a version that was already proven good in production is the fastest
     path to a known-safe state, and root-causing is strictly better done calmly, without the outage
     clock still running.

- [Intermediate] **You're asked to investigate why a specific customer's requests are consistently
  slower than everyone else's, while overall system metrics look completely normal.** →
  1. **Check if it's actually data-shape-dependent, not infrastructure-dependent** — a
     customer-specific slowdown with normal aggregate system metrics strongly suggests something
     about **that customer's specific data** is the cause, not a systemic infrastructure issue
     (which would show in aggregate metrics too). Common causes: that customer has a
     disproportionately large dataset (many more rows in a table filtered by their customer ID,
     hitting a query that isn't properly indexed or degrades non-linearly with row count), a
     pathological data shape (e.g. an unusually deep hierarchy, an unusually large JSON blob
     field), or a legitimately different usage pattern (calling an expensive endpoint far more
     frequently than typical customers).
  2. **Check query plans specifically for that customer's data** — run the actual slow query with
     that customer's ID and `EXPLAIN ANALYZE` it; a missing or ineffective index that only becomes a
     real problem past a certain row count per customer is a very common finding here (works fine
     for the median customer's few hundred rows, degrades badly for one customer's few million).
  3. **Check for N+1 patterns that scale with that customer's data volume specifically** — a
     collection-size-dependent N+1 (looping over that customer's order list, say) would be
     invisible for typical customers with few orders but very visible for one large customer with
     thousands.
  4. **Check for tenant-specific configuration/feature flags** — if the system has per-customer
     feature flags or configuration, confirm nothing customer-specific (a debug flag left on, a
     different code path enabled) is actively causing the slowdown, separate from pure data volume.
  5. **Correlate with when it started** for that customer — did their data volume simply cross a
     threshold over time (organic growth into a query's non-linear degradation point), or did
     something change abruptly (a bulk import, a new usage pattern)? → Follow-up: *Why is this
     kind of issue notoriously hard to catch in normal load testing and staging environments?*
     Load tests and staging data are typically uniform/synthetic and rarely replicate one real
     customer's actual, organically-grown, skewed data distribution — a query that's `O(n)` or
     worse in "number of rows for this customer" only becomes visibly slow once a real customer's
     data has actually grown large enough to expose it, which synthetic, evenly-distributed test
     data usually never does.

- [Intermediate] **A `@Scheduled` batch job that used to run in 5 minutes now takes 45 minutes and
  is starting to overlap with its next scheduled run. How do you approach fixing this?** →
  1. **Confirm it's not just data volume growth** — many batch jobs process "all rows since last
     run" or similar, and if the underlying table has simply grown 9x since the job was first
     written, a linear-or-worse-scaling job naturally gets slower over time without any code change
     — check row counts/data volume trend against the job's runtime trend first, since the fix
     differs completely if this is "expected growth requiring an algorithmic/batching fix" versus
     "something regressed."
  2. **Check for overlapping-run risk immediately, independent of root cause** — if the job isn't
     already guarded against concurrent execution (a distributed lock, or
     `@Scheduled(fixedDelay = ...)` instead of `fixedRate` so the next run is scheduled relative to
     the *previous run's completion* rather than a fixed wall-clock interval regardless of whether
     the last run finished), overlapping runs can compound the problem further (two instances of the
     job now competing for the same DB/CPU resources) or, worse, cause duplicate processing/data
     corruption if the job isn't idempotent — this needs an immediate guard regardless of the
     performance root cause.
  3. **Profile what the job is actually spending time on** — a query that's begun doing a full
     table scan (lost an index, or the query planner's chosen plan changed as data grew past a
     statistics threshold), unbatched processing (loading the entire dataset into memory instead of
     processing in pages/chunks), or N+1-style per-row processing that scales linearly with row
     count when it should be a set-based operation.
  4. **Consider batching/chunking the work** if it's currently processing everything in one large
     transaction/query — breaking it into smaller batches (page through results, commit
     periodically) both reduces peak memory/lock duration and makes partial progress resilient to a
     mid-run failure (resumable, rather than restarting the entire 45-minute job from scratch).
  5. **Consider whether the job even needs to process the full dataset every run**, versus an
     incremental approach (only rows changed/added since the last successful run, tracked via a
     watermark/timestamp) if it's currently doing full reprocessing every time unnecessarily. →
  Follow-up: *Why is a distributed lock (not just "assume only one instance runs it") necessary for
  a `@Scheduled` job in a horizontally-scaled Spring Boot deployment?* If the service runs as
  multiple instances (which it should, for availability), `@Scheduled` fires **independently on
  every instance** by default — without an explicit distributed lock (e.g. via ShedLock, a DB-row-
  based lock, or a Quartz cluster mode), the same job runs concurrently on every instance
  simultaneously, multiplying resource usage and risking duplicate processing/corruption if the job
  isn't idempotent; this is a very commonly missed detail when a job that worked fine as a single
  instance gets horizontally scaled later.

- [Intermediate] **Your team wants to introduce caching to reduce database load on a read-heavy
  endpoint. Walk through the design decisions.** →
  1. **Confirm caching is actually the right fix first** — check whether the DB load is from a
     genuinely expensive/slow query that should be optimized (a missing index, an N+1) rather than
     papering over a fixable inefficiency with a cache; caching a query that's slow because it's
     badly written just hides the problem and adds staleness risk for no real architectural gain
     over just fixing the query.
  2. **Decide what can tolerate staleness, and for how long** — caching only makes sense for data
     where slightly-stale reads are acceptable; the acceptable staleness window (seconds? minutes?)
     directly determines the TTL, and different data on the same endpoint might have very different
     tolerances (product catalog data can tolerate minutes of staleness; a user's current account
     balance usually can't).
  3. **Choose local (in-JVM, e.g. Caffeine) vs distributed (Redis) cache** — local cache is fastest
     (no network hop) but is **per-instance** (each of N instances has its own copy, meaning N times
     the memory, and a write on one instance doesn't invalidate the cache on the others, so
     staleness windows differ per instance too); distributed cache (Redis) is shared across all
     instances (consistent view, single invalidation point, no duplicated memory) at the cost of a
     network round-trip per cache access (though still far cheaper than the DB query it's replacing).
  4. **Design cache invalidation explicitly, not just TTL-based expiry** — if the underlying data
     can be updated by the application itself, invalidate/update the cache entry actively on write
     (via `@CacheEvict`/`@CachePut`, or an explicit cache delete in the update path) rather than
     relying purely on TTL expiry, which means writes are visible only after the TTL lapses — often
     an unacceptable staleness window for data the same system just modified.
  5. **Plan for cache-related failure modes**: a **cache stampede** (many concurrent requests all
     miss the cache simultaneously — e.g. right after an entry expires under high traffic — and all
     hit the DB at once, potentially overloading it worse than having no cache at all; mitigated by
     request coalescing/locking so only one request repopulates the cache while others wait for
     that result) and **cache unavailability** (if Redis itself is down, does the application
     gracefully fall through to the DB, or does it hard-fail? — it should degrade gracefully to
     direct DB reads, treating the cache as a performance optimization, not a hard dependency). →
  Follow-up: *Why can introducing a cache sometimes make an incident WORSE rather than better, if
  designed carelessly?* A cache stampede on a cold cache (a deploy that clears the cache, or a
  cache node restart) under real production traffic can hit the DB with a sudden burst of
  previously-cached-away load all at once, potentially overwhelming a DB that was only ever sized
  for the reduced, cached traffic pattern — the DB effectively loses the protection the cache had
  been quietly providing, right at the worst possible moment; this is exactly why cache warming
  strategies and stampede protection (request coalescing) matter, not just "add a cache and move
  on."

---

**File question count: 10** scenario-based questions, all [Intermediate]/[Advanced], each with a
structured "check first / second / third" model answer plus a follow-up.
