package com.gk.study.concurrency.solutions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Reference solution for {@code PrintOddEven}: a shared "whose turn" boolean, guarded by a
 * monitor, with each thread waiting on its own turn via wait/notifyAll -- see
 * notes/09-multithreading-concurrency/02-synchronized-intrinsic-locks-wait-notify.md. Correctness
 * does not depend on which thread happens to start first or how the scheduler interleaves them:
 * whichever thread acquires the lock first either proceeds (if it's already its turn) or waits
 * (releasing the lock) until the other thread signals it.
 */
public class PrintOddEvenSolution {

    private final Object lock = new Object();
    private boolean oddsTurn = true;

    public List<Integer> run(int n) {
        List<Integer> sequence = new CopyOnWriteArrayList<>();
        if (n <= 0) {
            return sequence;
        }
        Thread oddThread = new Thread(() -> printOdd(n, sequence), "odd-thread");
        Thread evenThread = new Thread(() -> printEven(n, sequence), "even-thread");
        oddThread.start();
        evenThread.start();
        joinBounded(oddThread);
        joinBounded(evenThread);
        return sequence;
    }

    private void printOdd(int n, List<Integer> sequence) {
        synchronized (lock) {
            for (int i = 1; i <= n; i += 2) {
                while (!oddsTurn) {
                    waitQuietly();
                }
                sequence.add(i);
                oddsTurn = false;
                lock.notifyAll();
            }
        }
    }

    private void printEven(int n, List<Integer> sequence) {
        synchronized (lock) {
            for (int i = 2; i <= n; i += 2) {
                while (oddsTurn) {
                    waitQuietly();
                }
                sequence.add(i);
                oddsTurn = true;
                lock.notifyAll();
            }
        }
    }

    private void waitQuietly() {
        try {
            lock.wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void joinBounded(Thread t) {
        try {
            t.join(TimeUnit.SECONDS.toMillis(5));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
