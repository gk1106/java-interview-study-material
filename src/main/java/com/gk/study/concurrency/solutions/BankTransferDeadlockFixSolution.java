package com.gk.study.concurrency.solutions;

/**
 * Reference solution for {@code BankTransferDeadlockFix}: always locks the lower-id account first,
 * regardless of the caller-supplied {@code from}/{@code to} order -- this makes the circular wait
 * that causes deadlock structurally impossible, since every thread that wants both locks always
 * asks for the same one first. See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md.
 */
public class BankTransferDeadlockFixSolution {

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

    public static void transfer(Account from, Account to, int amount) {
        Account first = from.getId() < to.getId() ? from : to;
        Account second = from.getId() < to.getId() ? to : from;
        synchronized (first) {
            synchronized (second) {
                from.debit(amount);
                to.credit(amount);
            }
        }
    }
}
