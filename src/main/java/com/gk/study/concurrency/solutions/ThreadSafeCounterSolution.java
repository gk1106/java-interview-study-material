package com.gk.study.concurrency.solutions;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reference solution for {@code ThreadSafeCounter} (see
 * notes/09-multithreading-concurrency/05-atomics-cas.md): a lock-free counter backed by
 * {@link AtomicInteger}, whose {@code incrementAndGet()} internally performs a CAS retry loop --
 * no update is ever lost under concurrent calls, and no thread ever blocks.
 */
public class ThreadSafeCounterSolution {

    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet();
    }

    public int get() {
        return count.get();
    }
}
