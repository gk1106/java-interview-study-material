# CompletableFuture

## 1. What it is

`CompletableFuture<T>` (Java 8+) is a `Future<T>` that can also be **composed**: chained,
combined with other futures, and completed manually — turning callback-style/blocking async code
into readable pipelines. Where a plain `Future` only lets you block-and-wait via `get()`,
`CompletableFuture` lets you say "when this finishes, then do that," without ever blocking a
thread just to wait.

## 2. How it works internally

### The core problem with `Future`

A plain `ExecutorService`-returned `Future<T>` has exactly one useful operation for reacting to
completion: `get()`, which **blocks the calling thread**. There's no way to register "run this
callback when done" without polling `isDone()` in a loop or blocking a thread on `get()` — which
defeats the purpose of async execution if you then immediately block on the result.

### Creating a `CompletableFuture`

- `CompletableFuture.supplyAsync(Supplier<T>)` — runs the supplier asynchronously (on
  `ForkJoinPool.commonPool()` by default, or a supplied `Executor`), returns a future for its
  result.
- `CompletableFuture.runAsync(Runnable)` — same, but for a task with no return value
  (`CompletableFuture<Void>`).
- `new CompletableFuture<T>()` + `complete(value)`/`completeExceptionally(ex)` — manually
  controlled completion, useful for bridging a callback-based API (e.g., a legacy async client) into
  the `CompletableFuture` world.

**Default executor gotcha**: `supplyAsync`/`thenApply`/etc. without an explicit `Executor` argument
run on the JVM-wide `ForkJoinPool.commonPool()`, shared by parallel streams and any other code using
the same default pool. A single slow/blocking task submitted this way can starve unrelated code
elsewhere in the same JVM that also relies on the common pool. Always pass an explicit `Executor`
(your own bounded `ThreadPoolExecutor`, topic 6) for real I/O-bound or blocking work in production
code.

### Chaining — `thenApply` / `thenCompose` / `thenAccept` / `thenRun`

| Method | Input | Output | Use for |
|--------|-------|--------|---------|
| `thenApply(fn)` | `T -> U` | `CompletableFuture<U>` | pure transformation of the result |
| `thenCompose(fn)` | `T -> CompletableFuture<U>` | `CompletableFuture<U>` (flattened) | chaining another **async** step (avoids `CompletableFuture<CompletableFuture<U>>`) |
| `thenAccept(consumer)` | `T -> void` | `CompletableFuture<Void>` | side effect using the result, no new value |
| `thenRun(runnable)` | `() -> void` | `CompletableFuture<Void>` | side effect ignoring the result entirely |

`thenApply` vs `thenCompose` is exactly the `map` vs `flatMap` distinction from Streams/`Optional`:
use `thenApply` when your function returns a plain value; use `thenCompose` when your function
itself returns another `CompletableFuture` (e.g., calling a second async service using the first
result) — using `thenApply` there would nest futures (`CompletableFuture<CompletableFuture<U>>`),
which is almost never what you want.

```java
CompletableFuture<User> userFuture = fetchUserAsync(id);           // CompletableFuture<User>
CompletableFuture<Address> addrFuture = userFuture
        .thenCompose(user -> fetchAddressAsync(user.getId()));     // flattened, not nested
CompletableFuture<String> cityFuture = addrFuture
        .thenApply(Address::getCity);                              // pure transform, no new async call
```

### `*Async` variants and thread hand-off

Every chaining method has a plain form (`thenApply`) and two `*Async` forms
(`thenApplyAsync(fn)`, `thenApplyAsync(fn, executor)`). The plain form runs the callback on
**whichever thread completes the previous stage** (which could be the calling thread itself, if
the previous stage was already done when you chained — or the async worker thread that computed
it). The `*Async` (no explicit executor) form submits the callback to `ForkJoinPool.commonPool()`
regardless of which thread completed the prior stage; the `*Async(fn, executor)` form submits it
to your chosen executor. This matters when a callback does anything non-trivial (I/O, CPU work) —
you generally want to control exactly which pool runs it, not "whatever thread happened to finish
first."

### Combining multiple futures

- **`thenCombine(other, biFn)`** — waits for *both* this future and `other` (independent,
  concurrently running), combines their two results with `biFn`. Use when you have two unrelated
  async calls whose results you need together (e.g., fetch price AND fetch inventory concurrently,
  then combine into a quote).
- **`CompletableFuture.allOf(futures...)`** — returns `CompletableFuture<Void>` that completes once
  **every** given future completes (success or failure of any doesn't short-circuit the others —
  they all still run to completion). Getting individual results afterward requires calling
  `.join()`/`.get()` on each original future (they're already done by the time `allOf` completes).
- **`CompletableFuture.anyOf(futures...)`** — completes as soon as **any one** of the given futures
  completes, with that one's result (typed `Object`, since the futures can have different result
  types) — useful for "race N sources, take whichever answers first" (hedged requests).

```java
CompletableFuture<Void> all = CompletableFuture.allOf(f1, f2, f3);
all.join();   // waits for all three
List<String> results = Stream.of(f1, f2, f3).map(CompletableFuture::join).toList();  // all done, no blocking wait here
```

### Error handling

| Method | Runs when | Can change the outcome type |
|--------|-----------|------------------------------|
| `exceptionally(fn)` | only if the stage completed **exceptionally** | `Throwable -> T` — recovers with a fallback value |
| `handle(biFn)` | **always** (success or failure), given both `(result, throwable)` (one is always `null`) | `(T, Throwable) -> U` — can transform either path |
| `whenComplete(biConsumer)` | **always**, side-effect only (logging, metrics) | none — passes the original result/exception through unchanged |

An exception thrown inside any stage of the chain short-circuits every subsequent `thenApply`/
`thenCompose`/`thenAccept` (they are skipped entirely — the exception just propagates down the
chain) until it hits an `exceptionally`/`handle` stage, which can recover it back into a normal
value; anything chained *after* that recovery runs normally again.

```java
fetchUserAsync(id)
    .thenApply(User::getEmail)
    .exceptionally(ex -> "unknown@example.com")   // recovers -- chain continues normally after this
    .thenAccept(email -> System.out.println("email: " + email));
```

### `join()` vs `get()`

`get()` (inherited from `Future`) throws checked `InterruptedException`/`ExecutionException`.
`join()` is `CompletableFuture`-specific and throws the **unchecked** `CompletionException`
wrapping the original cause — more convenient inside lambdas (e.g., inside a `Stream` pipeline)
where checked exceptions can't be declared.

### ASCII diagram — a chained + combined pipeline

```
supplyAsync(fetchPrice) ---thenCombine(with fetchInventoryAsync)---> combined quote
                                                                          |
                                                                    thenApply(format)
                                                                          |
                                                            exceptionally(fallback on failure)
                                                                          |
                                                                thenAccept(print/send)
```

## 3. Complexity

| Operation | Time | Notes |
|-----------|------|-------|
| `supplyAsync`/`runAsync` submission | O(1) | enqueues to the executor, like any task submission |
| `thenApply`/`thenCompose`/etc. chaining | O(1) to register | callback runs later, asynchronously or on the completing thread |
| `join()`/`get()` on an already-done future | O(1) | returns immediately, no blocking |
| `join()`/`get()` on a pending future | blocks until completion (or timeout with `get(timeout, unit)`) | |
| `allOf(...)` | O(n) to register, completes when the slowest of n futures completes | |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/CompletableFutureDemo.java` —
  demonstrates `thenApply` vs `thenCompose`, a `thenCombine` of two independently-running futures,
  `allOf`/`anyOf`, and both `exceptionally` and `handle` recovering from a failed stage, all bounded
  by `join()`/`get(timeout, unit)` so the demo can never hang.

```java
CompletableFuture<Integer> price = CompletableFuture.supplyAsync(() -> fetchPrice(), pool);
CompletableFuture<Integer> stock = CompletableFuture.supplyAsync(() -> fetchStock(), pool);
CompletableFuture<String> quote = price.thenCombine(stock, (p, s) -> "price=" + p + ", stock=" + s);
System.out.println(quote.get(2, TimeUnit.SECONDS));
```
Expected console output (abbreviated):
```
thenApply vs thenCompose: flattened result (no nested future) = LONDON
thenCombine: price=100, stock=42
allOf: all 3 tasks completed, results=[10, 20, 30]
anyOf: fastest of 3 tasks completed first, result=fast-task
exceptionally: recovered from failure with fallback value = unknown@example.com
handle: transformed both success and failure paths uniformly
```

## 5. When to use / when NOT to use

- Use `CompletableFuture` whenever you need to compose multiple async steps (sequential
  dependencies via `thenCompose`, independent steps combined via `thenCombine`/`allOf`) without
  blocking a thread at every step.
- Use `exceptionally`/`handle` to build resilient pipelines with fallbacks, instead of a bare
  `try/catch` around a blocking `get()`.
- Always pass an explicit bounded `Executor` for real work — don't leave production code depending
  on the shared `ForkJoinPool.commonPool()` default, which other unrelated code (parallel streams,
  other libraries) also uses.
- For simple "fire one async task, block once at the very end" cases, a plain `ExecutorService` +
  `Future` (topic 6) is simpler and sufficient — reach for `CompletableFuture` once you have
  multiple dependent/combined async steps to express.
- Since Java 21, consider whether **virtual threads** (topic 11) running simple blocking,
  sequential code might be simpler to read and reason about than a deeply chained
  `CompletableFuture` pipeline for I/O-bound workloads — `CompletableFuture` remains essential for
  genuinely combining/racing multiple independent async results, a pattern virtual threads alone
  don't replace.

## 6. Common pitfalls & gotchas

**Using `thenApply` where `thenCompose` was needed — nested futures**:
```java
CompletableFuture<CompletableFuture<Address>> nested = userFuture
        .thenApply(user -> fetchAddressAsync(user.getId()));   // BUG: fetchAddressAsync returns a
                                                                 // CompletableFuture<Address> itself
// fix:
CompletableFuture<Address> flat = userFuture
        .thenCompose(user -> fetchAddressAsync(user.getId()));
```

**Swallowing exceptions by never adding `exceptionally`/`handle` and never calling `get()`/`join()`**:
```java
CompletableFuture.supplyAsync(() -> { throw new RuntimeException("boom"); });
// BUG: nobody ever observes the failure -- it's silently absorbed unless something later calls
// get()/join() on this exact future or chains exceptionally/handle onto it
```

**Relying on the shared `ForkJoinPool.commonPool()` for blocking I/O work**:
```java
CompletableFuture.supplyAsync(() -> blockingHttpCall());  // BUG: ties up a commonPool thread,
// starving other unrelated code (e.g., parallel streams) sharing the same JVM-wide pool
// fix:
CompletableFuture.supplyAsync(() -> blockingHttpCall(), myDedicatedExecutor);
```

**Forgetting `allOf(...)` returns `Void`, not a list of results**:
```java
CompletableFuture<Void> all = CompletableFuture.allOf(f1, f2, f3);
List<Integer> results = all.join();   // BUG: does not compile / join() returns Void here
// fix:
all.join();
List<Integer> results = Stream.of(f1, f2, f3).map(CompletableFuture::join).toList();
```

## 7. Interview questions

- [Basic] What problem does `CompletableFuture` solve that plain `Future` doesn't? → Plain
  `Future.get()` is the only way to observe a result, and it blocks the calling thread — there's no
  way to register a callback that runs automatically on completion. `CompletableFuture` lets you
  chain (`thenApply`/`thenCompose`), combine (`thenCombine`/`allOf`), and react to failures
  (`exceptionally`/`handle`) without ever blocking a thread just to wait. → Follow-up: *Can you
  still block on a CompletableFuture if you want to?* Yes — `get()`/`join()` work exactly like on a
  regular `Future`, useful at the boundary where you finally need the result synchronously.
- [Basic] What's the difference between `thenApply` and `thenCompose`? → `thenApply` is for a
  function that returns a plain value (like `map`); `thenCompose` is for a function that itself
  returns another `CompletableFuture` (like `flatMap`) — using `thenApply` there would produce a
  nested `CompletableFuture<CompletableFuture<U>>` instead of a flat one. → Follow-up: *What's the
  Streams/Optional analogy?* `thenApply` ~ `map`, `thenCompose` ~ `flatMap`.
- [Basic] What does `CompletableFuture.allOf(...)` return, and how do you get the individual
  results? → It returns `CompletableFuture<Void>` that completes once every given future has
  completed; to get individual results, call `.join()`/`.get()` on each original future after
  `allOf` completes — they're all already done by then, so those calls return immediately. →
  Follow-up: *What does anyOf(...) return instead?* `CompletableFuture<Object>`, completing with
  the result of whichever future finishes first (typed `Object` since the inputs can have different
  result types).
- [Intermediate] Explain the difference between `thenApply(fn)`, `thenApplyAsync(fn)`, and
  `thenApplyAsync(fn, executor)`. → `thenApply` runs the callback on whichever thread completes the
  previous stage (could be the caller's thread, or whichever worker thread finished the async
  work); `thenApplyAsync(fn)` submits the callback to `ForkJoinPool.commonPool()` regardless;
  `thenApplyAsync(fn, executor)` submits it to your explicitly chosen executor. → Follow-up: *Why
  would you always want the explicit-executor form in production?* To avoid an unpredictable mix of
  "runs on the caller's thread sometimes, runs on the shared common pool other times," and to avoid
  starving the JVM-wide common pool that other unrelated code also depends on.
- [Intermediate] How do `exceptionally`, `handle`, and `whenComplete` differ? → `exceptionally`
  only runs on the failure path and can supply a fallback value, recovering the chain back to
  normal; `handle` always runs (success or failure) and receives both the result and the exception
  (one is null), letting it transform either outcome into a new value; `whenComplete` also always
  runs but is side-effect-only — it can't change the outcome, just observe it (e.g., logging) and
  passes the original result/exception through unchanged. → Follow-up: *If an exception occurs
  before a chain reaches thenApply, what happens to that stage?* It's skipped entirely — the
  exception propagates straight through every intermediate `thenApply`/`thenCompose`/`thenAccept`
  stage until it reaches an `exceptionally`/`handle` stage, or surfaces at a final `get()`/`join()`.
- [Intermediate] Why is `join()` often preferred over `get()` inside a Stream pipeline or lambda? →
  `get()` declares checked `InterruptedException`/`ExecutionException`, which can't be thrown from
  inside most functional interfaces (like `Function`) without a wrapping try/catch; `join()` throws
  the unchecked `CompletionException` instead, so it can be used directly inside lambdas (e.g.,
  `.map(CompletableFuture::join)`) without extra boilerplate. → Follow-up: *Does join() lose any
  information compared to get()'s ExecutionException?* No — `CompletionException.getCause()` gives
  you the original exception, same as `ExecutionException.getCause()`.
- [Advanced] Design a pipeline that fetches a user, then concurrently fetches that user's orders
  and their loyalty tier, then combines both into a summary, falling back to a default summary on
  any failure. → `fetchUserAsync(id).thenCompose(user -> { CompletableFuture<Orders> ordersF =
  fetchOrdersAsync(user.getId(), executor); CompletableFuture<Tier> tierF =
  fetchTierAsync(user.getId(), executor); return ordersF.thenCombine(tierF, (orders, tier) ->
  buildSummary(user, orders, tier)); }).exceptionally(ex -> defaultSummary());` — the outer
  `thenCompose` chains the dependent step (need the user first), the inner `thenCombine` runs the
  two independent lookups concurrently once the user is known, and a single `exceptionally` at the
  end catches a failure from any stage in the whole chain. → Follow-up: *Why is thenCombine, not
  thenCompose, correct for combining orders and tier?* Both `fetchOrdersAsync` and `fetchTierAsync`
  are independent of each other (only depend on the already-known user) and should run
  concurrently; `thenCompose` would force them into a sequential dependency chain instead of
  letting them run in parallel.
- [Advanced] What's the risk of every `CompletableFuture` chain in a large application defaulting
  to `ForkJoinPool.commonPool()`, and how would you detect it in production? → The common pool has
  a fixed size (by default, `#CPUs - 1`) and is shared across the entire JVM — parallel streams,
  any library using `CompletableFuture` without an explicit executor, and your own code all compete
  for the same limited threads; one component submitting blocking I/O work to it can starve
  unrelated CPU-bound work (like parallel streams) elsewhere in the same process, causing seemingly
  unrelated latency spikes. Detection: thread dumps showing many `ForkJoinPool.commonPool-worker-*`
  threads blocked in I/O, or unexplained latency in parallel-stream-heavy code correlating with load
  on some other async component. → Follow-up: *What's the fix once you've found this?* Pass an
  explicit, appropriately-sized (and appropriately named, for future debugging) `Executor` to every
  `*Async` call that does real I/O or CPU-heavy work, reserving the common pool for genuinely
  short, non-blocking callbacks only.

## 8. Exercises

No dedicated exercises for this topic — the executor-sizing and composition ideas here build
directly on the Executor framework (topic 6) exercises referenced in
`notes/09-multithreading-concurrency/10-classic-concurrency-problems.md`.

## 9. Quick recap

- `CompletableFuture` adds composability over plain `Future`: chain with `thenApply`/`thenCompose`,
  combine with `thenCombine`/`allOf`/`anyOf`, recover with `exceptionally`/`handle`, all without
  blocking a thread mid-pipeline.
- `thenApply` is `map`-like (plain return value); `thenCompose` is `flatMap`-like (function returns
  another `CompletableFuture` — avoids nested futures).
- Plain chaining methods run on whichever thread completed the prior stage; `*Async` variants
  submit to `ForkJoinPool.commonPool()` (or your explicit executor) instead — always pass an
  explicit executor for real work in production.
- An exception short-circuits every subsequent `thenApply`/`thenCompose`/`thenAccept` until an
  `exceptionally`/`handle` stage recovers it; `whenComplete` observes but never changes the outcome.
- `allOf(...)` returns `Void` — fetch individual results by calling `join()`/`get()` on the
  original futures afterward, which return immediately since they're already complete.
