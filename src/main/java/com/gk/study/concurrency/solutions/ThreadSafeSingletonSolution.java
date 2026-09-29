package com.gk.study.concurrency.solutions;

/**
 * Reference solution for {@code ThreadSafeSingleton}: double-checked locking. The {@code volatile}
 * modifier on {@code instance} is essential -- see
 * notes/09-multithreading-concurrency/03-volatile-and-java-memory-model.md for exactly why: without
 * it, the JIT/CPU could reorder the constructor's field writes to happen after the reference is
 * published, letting another thread's unsynchronized first check observe a non-null but
 * partially-constructed object.
 */
public final class ThreadSafeSingletonSolution {

    private static volatile ThreadSafeSingletonSolution instance;

    private ThreadSafeSingletonSolution() {
    }

    public static ThreadSafeSingletonSolution getInstance() {
        ThreadSafeSingletonSolution result = instance;
        if (result == null) {
            synchronized (ThreadSafeSingletonSolution.class) {
                result = instance;
                if (result == null) {
                    instance = result = new ThreadSafeSingletonSolution();
                }
            }
        }
        return result;
    }
}
