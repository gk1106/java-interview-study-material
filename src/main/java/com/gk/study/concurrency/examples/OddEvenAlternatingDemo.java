package com.gk.study.concurrency.examples;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Two threads strictly alternate printing odd and even numbers so the combined sequence is exactly
 * 1, 2, 3, ..., N in order -- a direct, minimal application of the wait/notify "shared turn"
 * pattern from topic 2. Correctness here is a property of the synchronization itself (strict
 * alternation is enforced regardless of scheduling), not of timing luck, so the resulting sequence
 * is deterministic every run.
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md
 */
public final class OddEvenAlternatingDemo {

    private OddEvenAlternatingDemo() {
    }

    private static final class Alternator {
        private final Object lock = new Object();
        private boolean oddsTurn = true;
        private final int max;
        private final List<Integer> sequence = new CopyOnWriteArrayList<>();

        Alternator(int max) {
            this.max = max;
        }

        void printOdd() throws InterruptedException {
            synchronized (lock) {
                for (int i = 1; i <= max; i += 2) {
                    while (!oddsTurn) {
                        lock.wait();
                    }
                    sequence.add(i);
                    oddsTurn = false;
                    lock.notifyAll();
                }
            }
        }

        void printEven() throws InterruptedException {
            synchronized (lock) {
                for (int i = 2; i <= max; i += 2) {
                    while (oddsTurn) {
                        lock.wait();
                    }
                    sequence.add(i);
                    oddsTurn = true;
                    lock.notifyAll();
                }
            }
        }

        List<Integer> getSequence() {
            return sequence;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("--- Print odd/even alternately with two threads ---");
        int max = 20;
        Alternator alternator = new Alternator(max);

        Thread oddThread = new Thread(() -> {
            try {
                alternator.printOdd();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "odd-thread");
        Thread evenThread = new Thread(() -> {
            try {
                alternator.printEven();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "even-thread");

        oddThread.start();
        evenThread.start();
        oddThread.join(TimeUnit.SECONDS.toMillis(5));
        evenThread.join(TimeUnit.SECONDS.toMillis(5));

        List<Integer> sequence = alternator.getSequence();
        List<Integer> expected = java.util.stream.IntStream.rangeClosed(1, max).boxed().toList();
        System.out.println("combined output = " + sequence);
        System.out.println("strictly ascending 1.." + max + " = " + sequence.equals(expected));
        System.out.println("(sanity check with Collections.max/min: min=" + Collections.min(sequence)
                + ", max=" + Collections.max(sequence) + ")");
    }
}
