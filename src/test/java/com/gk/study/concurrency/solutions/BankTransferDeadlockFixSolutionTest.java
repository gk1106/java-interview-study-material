package com.gk.study.concurrency.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.concurrency.solutions.BankTransferDeadlockFixSolution.Account;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class BankTransferDeadlockFixSolutionTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void singleThreadedTransferMovesTheExactAmount() {
        Account a = new Account(1, 1000);
        Account b = new Account(2, 500);
        BankTransferDeadlockFixSolution.transfer(a, b, 200);
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
                    BankTransferDeadlockFixSolution.transfer(accountA, accountB, 1);
                }
            });
            Future<?> bToA = pool.submit(() -> {
                for (int i = 0; i < transfersPerThread; i++) {
                    BankTransferDeadlockFixSolution.transfer(accountB, accountA, 1);
                }
            });

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
