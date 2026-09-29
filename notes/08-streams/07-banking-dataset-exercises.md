# Real-world exercises: the banking dataset

## 1. What it is

Every topic in this module has covered one stream feature in isolation; this topic ties them
together on one realistic, shared, fully deterministic dataset — `SampleBankingData` — to practice
composing `filter`/`groupingBy`/`reducing`/`sorted`/`Predicate` chains the way you actually would
in production reporting code (account totals, top-N spenders, monthly statements, fraud flags,
average-transaction ranking).

## 2. How it works internally

### The dataset

`src/main/java/com/gk/study/streams/model/SampleBankingData.java` provides three hand-verified,
literal (non-random) lists, shared by every demo/exercise/solution in this module:

```
5 customers  (C1..C5, one PREMIUM/RETAIL/CORPORATE segment each)
6 accounts   (A1..A6 — customer C1 owns two accounts: A1 savings + A2 current; everyone else owns one)
48 transactions (8 per account, spread across July and August 2026, mixed DEBIT/CREDIT,
                 with a handful of large-amount / odd-hour transactions deliberately seeded
                 in for the fraud-flag exercise)
```
Because every value is a literal (not `Math.random()`), every aggregation in this topic has one
fixed, correct answer — the tests assert exact expected numbers, not approximate/statistical ones.

### The join pattern: id -> id -> id via lookup maps

Transactions reference `accountId`; accounts reference `customerId`; there's no single object with
"customer name + transaction amount" together. The standard pattern, used throughout
`BankingReportsDemo` and the B01-B05 solutions, is to build small lookup maps once with
`Collectors.toMap`, then join through them inside a later stream:
```java
Map<String, String> accountToCustomer = accounts.stream()
        .collect(Collectors.toMap(Account::id, Account::customerId));
Map<String, String> customerIdToName = customers.stream()
        .collect(Collectors.toMap(Customer::id, Customer::name));

Map<String, BigDecimal> spendByCustomer = transactions.stream()
        .filter(t -> t.type() == TransactionType.DEBIT)
        .collect(Collectors.groupingBy(
                t -> accountToCustomer.get(t.accountId()),   // "join" via the lookup map
                Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
```
This is the same shape as a SQL `JOIN` + `GROUP BY` + `SUM`, expressed declaratively with streams
instead of a query planner — build the lookup maps first (each O(n)), then a single grouped
aggregation pass (O(n)) over the largest table (transactions).

### Why `BigDecimal`, not `double`, for money

Every amount in the dataset is a `BigDecimal`. Binary floating point (`double`) cannot represent
most decimal fractions exactly (`0.1 + 0.2 != 0.3` in IEEE 754 double arithmetic), which is
unacceptable for money — small rounding errors compound across thousands of transactions.
`BigDecimal::add` and `BigDecimal.compareTo` (never `.equals()`, which is scale-sensitive:
`new BigDecimal("8000").equals(new BigDecimal("8000.00"))` is `false`) are used throughout this
topic's solutions for exactly this reason — see `notes/01-java-foundations-for-dsa` for the
general `equals`/`compareTo` contract discussion.

### `Predicate` composition for multi-condition filters

```java
Predicate<Transaction> isLargeAmount = t -> t.amount().compareTo(largeAmountThreshold) > 0;
Predicate<Transaction> isOddHour = t -> {
    int hour = t.timestamp().getHour();
    return hour < 6 || hour >= 23;
};
Predicate<Transaction> isSuspicious = isLargeAmount.and(isOddHour);
```
Composing two small, independently-named, independently-testable predicates with `Predicate.and`
reads clearer than one large inline lambda, and either half can be reused or unit-tested alone —
the same idea as small, composable functions anywhere else in the codebase.

### Nested `groupingBy` for a two-level report

```java
Map<String, Map<YearMonth, Long>> txCountByAccountAndMonth = transactions.stream()
        .collect(Collectors.groupingBy(
                Transaction::accountId,
                Collectors.groupingBy(t -> YearMonth.from(t.timestamp()), Collectors.counting())));
```
The outer `groupingBy` buckets by account; its **downstream collector** is itself another
`groupingBy` (by month), whose downstream is `counting()`. This is the standard way to build an
N-level breakdown (account -> month -> count) in one single pass over the transaction list —
`MonthlyStatementSummarySolution` builds on exactly this nested-grouping shape.

## 3. Complexity

| Query | Time | Space | Notes |
|-------|------|-------|-------|
| Build a lookup map (`Account::id -> customerId`, etc.) | O(n) | O(n) | one-time cost, reused by every downstream query in the same report |
| Group transactions by account, sum debit/credit | O(t) (t = transaction count) | O(a) (a = distinct accounts) | one pass with `groupingBy` + `reducing`/`summingBigDecimal`-style downstream |
| Top-N customers by spend | O(t + c log c) (c = customer count) | O(c) | grouping is O(t); ranking is a sort over customers, not transactions |
| Monthly statement (nested groupingBy) | O(t) | O(a * months) | one pass; result size bounded by distinct (account, month) pairs actually present |
| Fraud-flag filter | O(t log t) | O(t) | filter is O(t); the final `sorted()` by id dominates |
| Highest-average-transaction account | O(t) | O(a) | `groupingBy` + `averagingDouble` in one pass, then a single `max` over a-sized entry set |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/streams/examples/BankingReportsDemo.java`

```java
List<String> top3 = spendByCustomer.entrySet().stream()
        .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
        .limit(3)
        .map(e -> customerIdToName.get(e.getKey()) + "=" + e.getValue())
        .toList();
System.out.println("top 3 customers by spend = " + top3);
```
Expected console output (verified against this module's test suite):
```
debit total by account = {A1=20300, A2=6300, A3=9650, A4=95000, A5=10000, A6=6900}
top 3 customers by spend = [Chitra Rao=95000, Alice Johnson=26600, Brian Lee=10150]
A1 transaction count by month = {2026-07=5, 2026-08=3}
fraud-flagged transaction ids = [T0005, T0021, T0031, T0040]
account with highest average transaction = A4 (29375.0)
```
(HashMap-backed intermediate results like `debitByAccount`/`txCountByAccountAndMonth` iterate in
an unspecified bucket order — the *values* shown are exact and test-verified, but don't rely on
the printed key order.)

## 5. When to use / when NOT to use

- This id-lookup-map-then-join-then-group pattern is the right shape whenever you're aggregating
  normalized, relational-style in-memory data (the same shape a SQL query would express with
  `JOIN`/`GROUP BY`) — it stays O(n)-ish overall as long as each lookup map build and each grouped
  pass is itself linear.
- For very large datasets that don't fit comfortably in memory, or where the "join" would need to
  hit a database per lookup, prefer doing the join/aggregation in the database (or a proper
  batch/stream-processing system) rather than pulling everything into Java streams — these
  patterns are for in-memory, already-loaded data, not a substitute for query optimization at
  scale.
- Always use `BigDecimal` (never `double`) for money aggregation, and always `compareTo`
  (never `equals`) when comparing `BigDecimal` amounts for a threshold check — `equals` also
  compares scale, not just numeric value.

## 6. Common pitfalls & gotchas

**Using `BigDecimal.equals()` instead of `compareTo()` for a threshold/amount comparison:**
```java
// BUG: BigDecimal.equals() is scale-sensitive — "8000" and "8000.00" are NOT equals()
if (transaction.amount().equals(BigDecimal.valueOf(8000))) { ... }

// FIX: compareTo() compares numeric value only, ignoring scale
if (transaction.amount().compareTo(BigDecimal.valueOf(8000)) > 0) { ... }
```

**Converting money to `double` for arithmetic — introduces rounding error:**
```java
// BUG: doubleValue() loses exactness; summing thousands of these compounds the error
double total = transactions.stream().mapToDouble(t -> t.amount().doubleValue()).sum();

// FIX: stay in BigDecimal arithmetic throughout
BigDecimal total2 = transactions.stream().map(Transaction::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
```
(An *average*, like `HighestAverageTransactionAccountSolution`'s use of `averagingDouble`, is a
legitimate, documented exception — an average is inherently not an exact monetary amount, so a
`double` result is acceptable there specifically, while still summing/comparing raw amounts in
`BigDecimal`.)

**Forgetting a customer/account with zero matching transactions — silently dropping them from a
report instead of showing a zero:**
```java
// BUG: a customer with no DEBIT transactions has no entry in spendByCustomer at all
Map<String, BigDecimal> spendByCustomer = transactions.stream()
        .filter(t -> t.type() == TransactionType.DEBIT)
        .collect(Collectors.groupingBy(t -> accountToCustomer.get(t.accountId()), ...));

// FIX (as TopNCustomersBySpendSolution does): start from the full customer list and default
// missing entries to ZERO, rather than starting from the transaction stream's grouped keys
Map<String, BigDecimal> spendByCustomer2 = customers.stream()
        .collect(Collectors.toMap(Customer::id, c -> spendFromTransactions.getOrDefault(c.id(), BigDecimal.ZERO)));
```

**Composing a fraud predicate as one large lambda instead of named, testable pieces** — harder to
unit-test the amount check independently of the odd-hour check, and harder to read at a glance;
prefer `Predicate.and`/`or`/`negate` composition of small, named predicates (see section 2 above).

## 7. Interview questions

- [Basic] Why does this dataset use `BigDecimal` for every amount field instead of `double`? →
  `double` (binary floating point) cannot exactly represent most decimal fractions, so repeated
  addition/subtraction of money values accumulates rounding error; `BigDecimal` represents decimal
  values exactly and is the standard choice for monetary arithmetic in Java. → Follow-up: *Is it
  ever acceptable to use `double` for a money-adjacent value here?* Yes for a genuinely
  non-exact derived statistic like an *average* transaction amount (see
  `HighestAverageTransactionAccountSolution`'s `averagingDouble`) — the average itself isn't a
  settleable monetary amount, so the precision loss is acceptable there, while raw sums/thresholds
  stay in `BigDecimal`.
- [Basic] Why is `BigDecimal.compareTo()` used instead of `.equals()` for threshold checks in the
  fraud-flag exercise? → `equals()` on `BigDecimal` considers scale as well as value — `8000` and
  `8000.00` are numerically equal but not `.equals()` — while `compareTo()` compares numeric value
  only, which is what a ">" threshold check actually needs. → Follow-up: *Where else in the JDK
  does this equals-vs-compareTo scale trap show up?* `BigDecimal` is the main one; it's a commonly
  cited example when explaining why `equals()`/`compareTo()` aren't always interchangeable even for
  `Comparable` types, unlike most classes where they're expected to agree.
- [Basic] What's the purpose of building `accountToCustomer`/`customerIdToName` lookup maps before
  the main aggregation query? → Transactions only carry an `accountId`, and accounts only carry a
  `customerId` — there's no single record with both a transaction amount and a customer name
  together, so a small `Collectors.toMap` lookup is built once per relationship and then used
  inside the main stream to "join" across ids, the same way a SQL query would join two tables. →
  Follow-up: *Is building these lookup maps itself O(n) or worse?* O(n) each — one pass over
  accounts, one over customers — cheap relative to the transaction-level aggregation that follows.
- [Intermediate] Walk through how `Collectors.groupingBy(Transaction::accountId, Collectors
  .groupingBy(month, Collectors.counting()))` produces a two-level map. → The outer `groupingBy`
  buckets every transaction by `accountId` into `Map<String, List<Transaction>>` conceptually;
  its *downstream* collector, instead of the default `toList()`, is itself another `groupingBy`
  that further buckets each account's transactions by `YearMonth`, whose own downstream is
  `counting()` — so each account's bucket becomes a `Map<YearMonth, Long>` instead of a flat list,
  all computed in one single pass over the transaction stream (not two separate passes). →
  Follow-up: *How would you extend this to a three-level breakdown, e.g. account -> month ->
  category?* Nest a third `groupingBy(Transaction::category, ...)` as the innermost downstream —
  the pattern composes arbitrarily deep, still in one pass.
- [Intermediate] In `TopNCustomersBySpendSolution`, why does the solution start from `customers
  .stream()` rather than from the grouped spend map when building the final `spendByCustomer`? →
  Grouping directly from the DEBIT transaction stream only produces entries for customers who
  actually *have* at least one DEBIT transaction — a customer with zero debits would be silently
  missing from the report instead of showing a `ZERO` spend, which violates the exercise's stated
  constraint ("a customer with zero DEBIT transactions still has an entry with totalSpend = 0").
  Starting from the full customer list and defaulting via `getOrDefault(id, BigDecimal.ZERO)`
  guarantees every customer is represented. → Follow-up: *What's the general lesson here?* When
  an aggregation must include "zero" entries for keys with no matching data, always drive the
  final map's *keys* from the complete reference set (all customers/accounts/categories), not from
  whatever keys happen to survive a filter on the transactional data.
- [Advanced] The fraud-flag exercise composes `isLargeAmount.and(isOddHour)` from two separately
  defined predicates. What would change, behaviorally and in terms of testability, if this were
  instead written as one inline lambda `t -> t.amount().compareTo(threshold) > 0 && (t.timestamp()
  .getHour() < 6 || t.timestamp().getHour() >= 23)`? → Behaviorally nothing — both produce
  identical short-circuiting boolean logic (`Predicate.and`'s default implementation is itself
  short-circuiting, equivalent to `&&`). The difference is purely structural: the composed version
  gives each condition a name and lets you unit-test `isLargeAmount` and `isOddHour`
  independently, reuse either one in a different composed predicate elsewhere (e.g. "all large
  transactions regardless of hour" reusing just `isLargeAmount`), and reads its intent at the call
  site (`isSuspicious`) without re-parsing the boolean expression. → Follow-up: *Is there a
  meaningful performance difference between the two forms?* No — both compile down to essentially
  the same short-circuiting boolean evaluation per element; the composed predicates are a design/
  readability/testability choice, not a performance one.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| B01 | Medium | Group transactions by account, sum total DEBIT and total CREDIT | `groupingBy` + downstream reduce/sum per transaction type | `exercises/AccountTransactionTotals.java` |
| B02 | Medium | Rank customers by total spend (sum of DEBIT across all their accounts), return top N | join transactions -> account -> customer, `groupingBy` + `reducing`, sort + limit | `exercises/TopNCustomersBySpend.java` |
| B03 | Medium | Monthly statement per account: total debit/credit + count, per (account, month) | nested `groupingBy` (accountId -> month) + downstream reduction to `MonthlyStatement` | `exercises/MonthlyStatementSummary.java` |
| B04 | Hard | Flag transactions above an amount threshold posted at an "odd hour" | composed `Predicate` chain (`Predicate.and`) + filter + sorted | `exercises/FraudFlaggedTransactions.java` |
| B05 | Hard | Find the account with the highest average transaction amount | `groupingBy` + `averagingDouble`, then `max` by value | `exercises/HighestAverageTransactionAccount.java` |

- <details><summary>Hint (B01)</summary>Group by `accountId` first (`groupingBy(Transaction::
  accountId)`), then for each account's transaction list, sum DEBIT and CREDIT amounts separately
  with `filter(type) + map(amount) + reduce(ZERO, BigDecimal::add)`.</details>
- <details><summary>Hint (B02)</summary>Build `accountId -> customerId` and `customerId -> name`
  lookup maps first; group DEBIT transactions by the *customer* (via the account lookup), sum with
  `Collectors.reducing`; default every customer to `ZERO` spend before sorting descending and
  taking `limit(topN)`.</details>
- <details><summary>Hint (B03)</summary>`groupingBy(Transaction::accountId, groupingBy(t ->
  YearMonth.from(t.timestamp())))` gives a two-level bucketing of transaction lists; reduce each
  innermost list to one `MonthlyStatement` (debit sum, credit sum, count).</details>
- <details><summary>Hint (B04)</summary>Two small named `Predicate<Transaction>`s (amount > 
  threshold via `BigDecimal.compareTo`; hour < 6 or hour >= 23) combined with `.and(...)`, then
  `filter` + `map(id)` + `sorted()`.</details>
- <details><summary>Hint (B05)</summary>`groupingBy(Transaction::accountId, averagingDouble(t ->
  t.amount().doubleValue()))` computes every account's average in one pass; then
  `entrySet().stream().max(Map.Entry.comparingByValue())` picks the winner.</details>

Solutions are in `src/main/java/com/gk/study/streams/solutions/` (`AccountTransactionTotalsSolution`,
`TopNCustomersBySpendSolution`, `MonthlyStatementSummarySolution`, `FraudFlaggedTransactionsSolution`,
`HighestAverageTransactionAccountSolution`) — attempt the stubs in `exercises/` first.

## 9. Quick recap

- The banking dataset (`SampleBankingData`) is fully deterministic (no randomness), so every
  aggregation has one exact, test-verified answer.
- Cross-entity queries follow a join pattern: build small `Collectors.toMap` lookup maps once
  (transaction -> account -> customer), then use them inside a later grouped aggregation pass.
- Always use `BigDecimal` for money, `compareTo()` (never `equals()`) for amount comparisons, and
  reserve `double`/`averagingDouble` for genuinely non-exact derived statistics like an average.
- Nested `groupingBy` calls build multi-level breakdowns (account -> month -> count) in a single
  pass over the source data, not one pass per level.
- When a report must include zero-value entries for keys with no matching data, drive the final
  map's keys from the complete reference list and default missing values, rather than deriving
  keys purely from whatever survives a filter on the transactional data.
