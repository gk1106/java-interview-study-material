package com.gk.study.concurrency.exercises;

import java.util.List;

/**
 * E03 [Easy] Given two threads -- one that only prints odd numbers, one that only prints even
 * numbers -- make them strictly alternate so the combined sequence is exactly 1, 2, 3, ..., n in
 * order. Must use proper synchronization (a shared "whose turn is it" signal), not sleep-based
 * timing guesses.
 * Input:  run(6)
 * Output: [1, 2, 3, 4, 5, 6]  (always exactly this, regardless of scheduling)
 * Constraint: must complete within a bounded time (no deadlock); correctness must not depend on
 * thread scheduling luck.
 * Pattern: shared-turn wait/notify handshake
 *
 * See notes/09-multithreading-concurrency/02-synchronized-intrinsic-locks-wait-notify.md and
 * notes/09-multithreading-concurrency/10-classic-concurrency-problems.md.
 */
public class PrintOddEven {

    // TODO: add fields for a shared lock object and a "whose turn" flag (e.g. a boolean
    // oddsTurn), guarded by that lock.

    /**
     * Runs two threads that alternately contribute odd and even numbers from 1 to {@code n}
     * (inclusive), waits for both to finish (bounded), and returns the combined sequence in the
     * order it was produced -- which must always be exactly ascending 1..n.
     *
     * @param n the upper bound (inclusive); may be 0 (empty result)
     * @return the combined sequence, strictly ascending from 1 to n
     */
    public List<Integer> run(int n) {
        // TODO: implement using two threads (one printing/collecting odds, one evens) that
        // synchronize via wait/notify on a shared lock so they strictly alternate turns.
        throw new UnsupportedOperationException("TODO");
    }
}
