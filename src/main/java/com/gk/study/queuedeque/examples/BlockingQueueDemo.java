package com.gk.study.queuedeque.examples;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates each member of the BlockingQueue family with small, bounded, fast operations (no
 * unbounded sleeps) so the demo runs deterministically in well under a second.
 *
 * See notes/04-queue-deque/04-blockingqueue-family.md.
 */
public final class BlockingQueueDemo {

    private BlockingQueueDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        arrayBlockingQueueProducerConsumer();
        System.out.println();
        linkedBlockingQueueQuickDemo();
        System.out.println();
        priorityBlockingQueueOrdering();
        System.out.println();
        delayQueueDemo();
        System.out.println();
        synchronousQueueHandoff();
    }

    /** Bounded ArrayBlockingQueue: producer blocks on put() once the queue (capacity 2) is full. */
    private static void arrayBlockingQueueProducerConsumer() throws InterruptedException {
        System.out.println("== ArrayBlockingQueue: bounded producer/consumer (capacity 2) ==");
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2);
        int itemCount = 5;

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= itemCount; i++) {
                    queue.put(i); // blocks here once the queue is full -- applies backpressure
                    System.out.println("  producer put " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= itemCount; i++) {
                    int value = queue.take(); // blocks here if the queue is empty
                    System.out.println("  consumer took " + value);
                    Thread.sleep(2); // small, bounded delay so the producer visibly blocks at least once
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "consumer");

        producer.start();
        consumer.start();
        producer.join(5_000);
        consumer.join(5_000);
    }

    /** LinkedBlockingQueue: optionally bounded, node-based, uses separate put/take locks
     *  internally so producers and consumers don't contend on the same lock. */
    private static void linkedBlockingQueueQuickDemo() throws InterruptedException {
        System.out.println("== LinkedBlockingQueue: default unbounded capacity (danger in prod!) ==");
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(); // capacity Integer.MAX_VALUE if unspecified
        queue.put("first");
        queue.put("second");
        System.out.println("  drained: " + queue.take() + ", " + queue.take());
        System.out.println("  ALWAYS prefer new LinkedBlockingQueue<>(boundedCapacity) in production");
        System.out.println("  to avoid unbounded memory growth if consumers fall behind producers.");
    }

    /** PriorityBlockingQueue: unbounded heap-ordered blocking queue -- take() blocks when empty,
     *  put() never blocks (no capacity limit). */
    private static void priorityBlockingQueueOrdering() throws InterruptedException {
        System.out.println("== PriorityBlockingQueue: heap-ordered, unbounded ==");
        BlockingQueue<Integer> queue = new PriorityBlockingQueue<>();
        for (int v : List.of(5, 1, 4, 2, 3)) {
            queue.put(v);
        }
        StringBuilder drained = new StringBuilder();
        while (!queue.isEmpty()) {
            drained.append(queue.take()).append(' ');
        }
        System.out.println("  offered 5,1,4,2,3 -> drained in priority order: " + drained.toString().trim());
    }

    /** DelayQueue: elements only become available once their own delay has expired; backed by a
     *  PriorityQueue ordered by remaining delay. Delays are kept in the tens-of-milliseconds
     *  range so the demo stays fast. */
    private static void delayQueueDemo() throws InterruptedException {
        System.out.println("== DelayQueue: elements surface only after their delay expires ==");
        DelayQueue<DelayedTask> queue = new DelayQueue<>();
        long now = System.nanoTime();
        queue.put(new DelayedTask("slow(30ms)", now + TimeUnit.MILLISECONDS.toNanos(30)));
        queue.put(new DelayedTask("fast(5ms)", now + TimeUnit.MILLISECONDS.toNanos(5)));
        queue.put(new DelayedTask("medium(15ms)", now + TimeUnit.MILLISECONDS.toNanos(15)));

        for (int i = 0; i < 3; i++) {
            DelayedTask task = queue.take(); // blocks until the earliest remaining delay expires
            System.out.println("  took " + task.name() + "  (expected order: fast, medium, slow)");
        }
    }

    /** SynchronousQueue: zero capacity -- put() and take() must rendezvous directly. */
    private static void synchronousQueueHandoff() throws InterruptedException {
        System.out.println("== SynchronousQueue: zero-capacity direct hand-off ==");
        SynchronousQueue<String> queue = new SynchronousQueue<>();

        Thread taker = new Thread(() -> {
            try {
                String value = queue.take(); // blocks until a matching put() arrives
                System.out.println("  taker received: " + value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "taker");
        taker.start();

        Thread putter = new Thread(() -> {
            try {
                queue.put("hand-off"); // blocks until the taker is ready
                System.out.println("  putter finished put()");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "putter");
        putter.start();

        taker.join(5_000);
        putter.join(5_000);
    }

    private record DelayedTask(String name, long readyAtNanos) implements Delayed {
        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(readyAtNanos - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(this.getDelay(TimeUnit.NANOSECONDS), other.getDelay(TimeUnit.NANOSECONDS));
        }
    }
}
