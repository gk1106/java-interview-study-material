package com.gk.study.concurrency.exercises;

import java.util.List;

/**
 * E05 [Medium] Implement a producer-consumer pipeline using {@code java.util.concurrent.BlockingQueue}
 * (e.g. {@code ArrayBlockingQueue}) with MULTIPLE producers and MULTIPLE consumers running
 * concurrently, instead of the hand-rolled wait/notify version from E04.
 * Input:  run(totalItems=1000, capacity=16, producerCount=4, consumerCount=4) -- totalItems is
 *         evenly divisible by producerCount; each producer produces its own contiguous partition
 *         of the integer range [0, totalItems).
 * Output: a list containing every value in [0, totalItems) exactly once (order not guaranteed,
 *         since multiple producers/consumers race) -- no item lost, no item duplicated.
 * Constraint: must use a bounded BlockingQueue internally (a real capacity, not unbounded); must
 * terminate cleanly once all items are produced and consumed (no consumer thread left blocked
 * forever waiting for more work) -- use a "poison pill" sentinel, one per consumer, enqueued only
 * after every producer has finished.
 * Pattern: BlockingQueue put/take + poison-pill shutdown signal
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md and
 * notes/04-queue-deque/04-blockingqueue-family.md.
 */
public class ProducerConsumerBlockingQueue {

    /**
     * Runs {@code producerCount} producer threads (each producing its own contiguous slice of
     * {@code [0, totalItems)}) and {@code consumerCount} consumer threads draining a shared
     * bounded {@code BlockingQueue}, and returns every consumed item once all producers and
     * consumers have finished.
     *
     * @param totalItems     total number of items to produce; must be evenly divisible by producerCount
     * @param capacity       the bounded queue's capacity
     * @param producerCount  number of concurrent producer threads
     * @param consumerCount  number of concurrent consumer threads
     * @return every produced item, each appearing exactly once (order not guaranteed)
     */
    public List<Integer> run(int totalItems, int capacity, int producerCount, int consumerCount)
            throws InterruptedException {
        // TODO: implement using java.util.concurrent.ArrayBlockingQueue<Integer>:
        //   1. start `producerCount` threads, each put()-ing its own slice of [0, totalItems)
        //   2. start `consumerCount` threads that take() until they see a poison-pill sentinel
        //   3. wait for all producers to finish, then put() one poison pill per consumer
        //   4. wait for all consumers to finish, then return everything they collected
        throw new UnsupportedOperationException("TODO");
    }
}
