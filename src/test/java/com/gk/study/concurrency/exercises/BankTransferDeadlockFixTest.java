package com.gk.study.concurrency.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.concurrency.exercises.BankTransferDeadlockFix.Account;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for the {@link BankTransferDeadlockFix} exercise stub. EXPECTED TO FAIL until implemented.
 *
 * <p>The deadlock-avoidance proof is a bounded-completion test: two threads transfer in opposite
 * directions between the same two accounts, 10,000 times each, and both {@code Future.get} calls
 * are given a generous but finite timeout -- if the fix is wrong (naive from-then-to locking), this
 * test times out (fails fast) instead of hanging the whole suite, backed further by a JUnit 5
 * {@code @Timeout} on the test method itself.
 */
class BankTransferDeadlockFixTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void singleThreadedTransferMovesTheExactAmount() {
        Account a = new Account(1, 1000);
        Account b = new Account(2, 500);
        BankTransferDeadlockFix.transfer(a, b, 200);
        assertThat(a.getBalance()).isEqualTo(800);
        assertThat(b.getBalance()).isEqualTo(700);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void concurrentOppositeDirectionTransfersNeverDeadlockAndConserveTotalBalance() throws Exception {
        Account accountA = new Account(1, 100_000);
        Account accountB = new Account(2, 100_000);
        int transfersPerThread = 10_000;

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> aToB = pool.submit(() -> {
                for (int i = 0; i < transfersPerThread; i++) {
                    BankTransferDeadlockFix.transfer(accountA, accountB, 1);
                }
            });
            Future<?> bToA = pool.submit(() -> {
                for (int i = 0; i < transfersPerThread; i++) {
                    BankTransferDeadlockFix.transfer(accountB, accountA, 1);
                }
            });

            // A generous but FINITE bound: a real deadlock would hang here forever without it.
            aToB.get(5, TimeUnit.SECONDS);
            bToA.get(5, TimeUnit.SECONDS);
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }

        int totalBalance = accountA.getBalance() + accountB.getBalance();
        assertThat(totalBalance).isEqualTo(200_000);
    }
}
