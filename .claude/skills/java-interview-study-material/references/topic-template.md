# Topic template (use for every topic notes file)

File: `notes/<NN-module>/<NN-topic>.md`

```markdown
# <Topic name>

## 1. What it is (2–4 lines, plain English)

## 2. How it works internally
- Step-by-step explanation + ASCII diagram
- Relevant JDK source behaviour (fields, key methods), paraphrased

## 3. Complexity
| Operation | Time | Space | Notes |
|-----------|------|-------|-------|

## 4. Example code
- Link to runnable class: `src/main/java/com/gk/study/<module>/examples/<Name>Demo.java`
- Short inline snippet of the key part + the expected console output

## 5. When to use / when NOT to use

## 6. Common pitfalls & gotchas (with small code showing the bug and the fix)

## 7. Interview questions
- [Basic] Q … → Model answer … → Likely follow-up …
- [Intermediate] …
- [Advanced] …

## 8. Exercises
| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
- Each exercise: problem statement, input/output examples, constraints,
  hint (collapsed with <details>), target complexity.
- Solutions are in the `solutions` package — say so, but do NOT show them here.

## 9. Quick recap (5 bullet points to revise in 2 minutes)
```

## Exercise file format

```java
package com.gk.study.list.exercises;

/**
 * E03 [Medium] Remove duplicates from a sorted list in-place.
 * Input:  [1,1,2,3,3]  → Output: [1,2,3]
 * Constraint: O(n) time, O(1) extra space.
 * Pattern: two pointers
 */
public class RemoveDuplicatesSorted {
    public static int solve(java.util.List<Integer> list) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
```

Each exercise gets a JUnit 5 test with normal cases, edge cases (empty, single
element, nulls where relevant) and one larger input.
