package com.gk.study.collectionsoverview.exercises;

import java.util.Iterator;

/**
 * E03 [Medium] Implement a custom {@code Iterable<Integer>} that lazily
 * yields Fibonacci numbers (starting 0, 1, 1, 2, 3, 5, ...) up to and
 * including a given bound, so it can be used directly in a for-each loop.
 * Demonstrates that {@code Iterable} — not any concrete collection — is the
 * true root of what "can be iterated".
 *
 * Input:  bound (inclusive upper limit), e.g. 10
 * Output: iterating yields 0, 1, 1, 2, 3, 5, 8
 *
 * Example:
 *   for (int x : new FibonacciRange(10)) System.out.print(x + " ");
 *   -&gt; prints: 0 1 1 2 3 5 8
 *
 *   new FibonacciRange(0) -&gt; yields only: 0
 *   negative bound        -&gt; yields nothing (empty iteration)
 *
 * Constraint: O(1) space beyond the two running values — do NOT precompute
 * a backing list; compute the next Fibonacci number on demand inside the
 * iterator's next().
 * Pattern: custom Iterable / lazy iterator
 */
public class FibonacciRange implements Iterable<Integer> {

    private final int bound;

    public FibonacciRange(int bound) {
        this.bound = bound;
    }

    @Override
    public Iterator<Integer> iterator() {
        // TODO: return an Iterator<Integer> whose hasNext()/next() compute
        // the next Fibonacci number lazily, stopping once it would exceed bound.
        throw new UnsupportedOperationException("TODO");
    }
}
