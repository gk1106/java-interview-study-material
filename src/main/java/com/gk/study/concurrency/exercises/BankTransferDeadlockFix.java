package com.gk.study.concurrency.exercises;

/**
 * E06 [Medium] Fix a classic two-lock deadlock: a bank transfer that locks {@code from} then
 * {@code to} deadlocks when two threads concurrently transfer in opposite directions
 * (transfer(A, B) and transfer(B, A) at the same time -- each thread ends up holding the lock the
 * other one wants). Fix it by acquiring the two accounts' locks in a CONSISTENT global order
 * (e.g. by account id), independent of which account is {@code from} and which is {@code to}.
 * Input:  two accounts A (id=1) and B (id=2), each starting with a balance; thread 1 repeatedly
 *         calls transfer(A, B, 1); thread 2 concurrently repeatedly calls transfer(B, A, 1).
 * Output: both threads complete within a bounded time (no deadlock); the combined balance of A
 *         and B is unchanged at the end (every transfer is conserved -- no lost/duplicated money).
 * Constraint: must never deadlock, proven by completing 10,000 opposite-direction transfers per
 * thread within a 5-second bound; the actual debit/credit logic must still use the original
 * `from`/`to` accounts (only the LOCK ACQUISITION order changes, not the transfer's semantics).
 * Pattern: consistent lock ordering (deadlock avoidance via breaking circular wait)
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md.
 */
public class BankTransferDeadlockFix {

    /** A simple bank account with a stable id, usable as a lock-ordering key. */
    public static final class Account {
        private final int id;
        private int balance;

        public Account(int id, int balance) {
            this.id = id;
            this.balance = balance;
        }

        public int getId() {
            return id;
        }

        public synchronized int getBalance() {
            return balance;
        }

        void debit(int amount) {
            balance -= amount;
        }

        void credit(int amount) {
            balance += amount;
        }
    }

    /**
     * Transfers {@code amount} from {@code from} to {@code to}, safely under concurrent calls in
     * either direction between the same two accounts -- must never deadlock.
     */
    public static void transfer(Account from, Account to, int amount) {
        // TODO: implement. Do NOT simply do `synchronized (from) { synchronized (to) { ... } }` --
        // that deadlocks under opposite-direction concurrent calls. Instead, always acquire the
        // two accounts' monitors in a fixed order determined by a stable key (e.g. getId()),
        // regardless of which one is `from` and which is `to`; perform the actual debit/credit
        // using the original `from`/`to` references once both locks are held.
        throw new UnsupportedOperationException("TODO");
    }
}
