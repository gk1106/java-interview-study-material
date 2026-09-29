package com.gk.study.concurrency.examples;

/**
 * Demonstrates the Java Memory Model's visibility guarantee (or lack of it): a non-volatile stop
 * flag that a busy-spinning worker thread is NOT guaranteed to ever observe, a volatile stop flag
 * that reliably stops the worker, and the safe-publication pattern where a plain field is made
 * safely visible to another thread transitively through a single volatile write/read pair.
 *
 * <p>The non-volatile worker thread is started as a DAEMON specifically so this demo can never
 * hang the JVM even in the (legally permitted) case where it spins forever without ever observing
 * the stop request -- every wait in this class is bounded.
 *
 * See notes/09-multithreading-concurrency/03-volatile-and-java-memory-model.md
 */
public final class VolatileMemoryModelDemo {

    private VolatileMemoryModelDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        nonVolatileVisibilityDemo();
        volatileVisibilityDemo();
        safePublicationDemo();
    }

    /** Deliberately NOT volatile -- visibility of requestStop() to run() is legally not guaranteed. */
    private static final class NonVolatileFlagWorker implements Runnable {
        private boolean stopRequested = false;

        void requestStop() {
            stopRequested = true;
        }

        @Override
        public void run() {
            while (!stopRequested) {
                // deliberately empty: a tight spin the JIT is free to hoist the read out of
            }
        }
    }

    private static void nonVolatileVisibilityDemo() throws InterruptedException {
        System.out.println("--- non-volatile stop flag: visibility is NOT guaranteed by the JMM ---");
        NonVolatileFlagWorker worker = new NonVolatileFlagWorker();
        Thread t = new Thread(worker, "non-volatile-worker");
        t.setDaemon(true); // never blocks JVM exit, even if this thread spins forever
        t.start();
        Thread.sleep(50); // let the worker actually enter the spin loop
        worker.requestStop();
        boolean stoppedInTime = joinBounded(t, 1000);
        System.out.println("non-volatile stop flag: worker observed the stop within a 1s bound = "
                + stoppedInTime + (stoppedInTime
                        ? " (this JVM/run happened to see it -- the JMM still does not guarantee it)"
                        : " (risk demonstrated: the JMM permits this loop to never terminate)"));
        System.out.println();
    }

    /** volatile -- the JMM guarantees requestStop()'s write is visible to run()'s next read. */
    private static final class VolatileFlagWorker implements Runnable {
        private volatile boolean stopRequested = false;

        void requestStop() {
            stopRequested = true;
        }

        @Override
        public void run() {
            while (!stopRequested) {
                // spins until the volatile write becomes visible -- guaranteed to happen
            }
        }
    }

    private static void volatileVisibilityDemo() throws InterruptedException {
        System.out.println("--- volatile stop flag: visibility IS guaranteed ---");
        VolatileFlagWorker worker = new VolatileFlagWorker();
        Thread t = new Thread(worker, "volatile-worker");
        t.setDaemon(true);
        t.start();
        Thread.sleep(50);
        worker.requestStop();
        boolean stoppedInTime = joinBounded(t, 2000);
        System.out.println("volatile stop flag: worker observed the stop flag and exited cleanly = "
                + stoppedInTime);
        System.out.println();
    }

    /** payload is a PLAIN field, made safely visible via the volatile `ready` write/read pair. */
    private static final class SafePublication {
        private int payload;                  // plain field -- not volatile itself
        private volatile boolean ready = false;

        void publish(int value) {
            payload = value;   // (1) plain write -- happens-before (2) in program order
            ready = true;       // (2) volatile write
        }

        Integer consumeIfReady() {
            if (ready) {          // (3) volatile read
                return payload;   // guaranteed to see the fully published value, never a stale default
            }
            return null;
        }
    }

    private static void safePublicationDemo() throws InterruptedException {
        System.out.println("--- safe publication: plain field visible through a volatile flag ---");
        SafePublication sp = new SafePublication();
        Thread publisher = new Thread(() -> sp.publish(42), "publisher");
        publisher.start();
        publisher.join(2000);

        Integer observed = null;
        long deadline = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < deadline) {
            observed = sp.consumeIfReady();
            if (observed != null) {
                break;
            }
        }
        System.out.println("safe publication via volatile: payload seen after ready==true = " + observed
                + " (never 0)");
    }

    private static boolean joinBounded(Thread t, long millis) throws InterruptedException {
        t.join(millis);
        return !t.isAlive();
    }
}
