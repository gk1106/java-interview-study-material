package com.gk.study.streams.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A small, fully deterministic in-memory banking dataset shared by every topic-7 demo, exercise
 * and solution in this module. Nothing here is random — every amount and timestamp is a literal
 * value, so the same dataset always produces the same aggregation results, and the numbers below
 * are hand-verified (see the module notes for the worked totals).
 *
 * <p>Shape of the data:
 * <ul>
 *   <li>5 customers ({@code C1}..{@code C5})</li>
 *   <li>6 accounts ({@code A1}..{@code A6}) — customer {@code C1} owns two accounts
 *       ({@code A1} savings, {@code A2} current), everyone else owns one</li>
 *   <li>48 transactions (8 per account), spread across July and August 2026, with a mix of
 *       categories, and a handful of large-amount / odd-hour transactions deliberately seeded in
 *       for the fraud-flag exercise (topic 7)</li>
 * </ul>
 */
public final class SampleBankingData {

    private SampleBankingData() {
    }

    public static List<Customer> customers() {
        return List.of(
                new Customer("C1", "Alice Johnson", "Mumbai", CustomerSegment.PREMIUM),
                new Customer("C2", "Brian Lee", "Bengaluru", CustomerSegment.RETAIL),
                new Customer("C3", "Chitra Rao", "Chennai", CustomerSegment.CORPORATE),
                new Customer("C4", "David Kim", "Delhi", CustomerSegment.RETAIL),
                new Customer("C5", "Esha Verma", "Pune", CustomerSegment.PREMIUM));
    }

    public static List<Account> accounts() {
        return List.of(
                new Account("A1", "C1", BigDecimal.valueOf(150_000), AccountType.SAVINGS),
                new Account("A2", "C1", BigDecimal.valueOf(50_000), AccountType.CURRENT),
                new Account("A3", "C2", BigDecimal.valueOf(20_000), AccountType.SAVINGS),
                new Account("A4", "C3", BigDecimal.valueOf(300_000), AccountType.CURRENT),
                new Account("A5", "C4", BigDecimal.valueOf(15_000), AccountType.SAVINGS),
                new Account("A6", "C5", BigDecimal.valueOf(80_000), AccountType.SAVINGS));
    }

    public static List<Transaction> transactions() {
        return List.of(
                // Account A1 (Alice, savings) — July + August 2026
                tx("T0001", "A1", 20000, TransactionType.CREDIT, "2026-07-01T09:15", "SALARY"),
                tx("T0002", "A1", 1500, TransactionType.DEBIT, "2026-07-03T18:20", "GROCERY"),
                tx("T0003", "A1", 2500, TransactionType.DEBIT, "2026-07-10T20:05", "UTILITY"),
                tx("T0004", "A1", 500, TransactionType.DEBIT, "2026-07-15T13:40", "DINING"),
                tx("T0005", "A1", 12000, TransactionType.DEBIT, "2026-07-22T02:10", "ONLINE_TRANSFER"),
                tx("T0006", "A1", 20000, TransactionType.CREDIT, "2026-08-01T09:10", "SALARY"),
                tx("T0007", "A1", 800, TransactionType.DEBIT, "2026-08-05T11:00", "FUEL"),
                tx("T0008", "A1", 3000, TransactionType.DEBIT, "2026-08-18T16:30", "GROCERY"),

                // Account A2 (Alice, current)
                tx("T0009", "A2", 1000, TransactionType.DEBIT, "2026-07-02T10:00", "POS"),
                tx("T0010", "A2", 2000, TransactionType.DEBIT, "2026-07-08T14:20", "POS"),
                tx("T0011", "A2", 10000, TransactionType.CREDIT, "2026-07-12T09:00", "TRANSFER_IN"),
                tx("T0012", "A2", 500, TransactionType.DEBIT, "2026-07-19T19:45", "DINING"),
                tx("T0013", "A2", 700, TransactionType.DEBIT, "2026-07-27T21:15", "GROCERY"),
                tx("T0014", "A2", 900, TransactionType.DEBIT, "2026-08-04T12:30", "FUEL"),
                tx("T0015", "A2", 3000, TransactionType.CREDIT, "2026-08-14T09:00", "TRANSFER_IN"),
                tx("T0016", "A2", 1200, TransactionType.DEBIT, "2026-08-25T23:50", "ONLINE_TRANSFER"),

                // Account A3 (Brian, savings)
                tx("T0017", "A3", 200, TransactionType.DEBIT, "2026-07-01T08:30", "GROCERY"),
                tx("T0018", "A3", 300, TransactionType.DEBIT, "2026-07-09T17:10", "DINING"),
                tx("T0019", "A3", 8000, TransactionType.CREDIT, "2026-07-14T09:00", "SALARY"),
                tx("T0020", "A3", 150, TransactionType.DEBIT, "2026-07-21T13:00", "FUEL"),
                tx("T0021", "A3", 9000, TransactionType.DEBIT, "2026-07-30T03:20", "ATM_WITHDRAWAL"),
                tx("T0022", "A3", 100, TransactionType.DEBIT, "2026-08-06T10:00", "POS"),
                tx("T0023", "A3", 8000, TransactionType.CREDIT, "2026-08-14T09:00", "SALARY"),
                tx("T0024", "A3", 400, TransactionType.DEBIT, "2026-08-20T15:45", "GROCERY"),

                // Account A4 (Chitra, current — corporate, larger amounts)
                tx("T0025", "A4", 50000, TransactionType.DEBIT, "2026-07-05T14:00", "VENDOR_PAYMENT"),
                tx("T0026", "A4", 100000, TransactionType.CREDIT, "2026-07-01T09:00", "INVOICE_RECEIPT"),
                tx("T0027", "A4", 20000, TransactionType.DEBIT, "2026-07-11T11:30", "PAYROLL"),
                tx("T0028", "A4", 5000, TransactionType.DEBIT, "2026-07-17T16:00", "UTILITY"),
                tx("T0029", "A4", 3000, TransactionType.DEBIT, "2026-07-24T10:15", "OFFICE_SUPPLIES"),
                tx("T0030", "A4", 40000, TransactionType.CREDIT, "2026-08-01T09:00", "INVOICE_RECEIPT"),
                tx("T0031", "A4", 15000, TransactionType.DEBIT, "2026-08-09T04:45", "VENDOR_PAYMENT"),
                tx("T0032", "A4", 2000, TransactionType.DEBIT, "2026-08-19T12:00", "OFFICE_SUPPLIES"),

                // Account A5 (David, savings)
                tx("T0033", "A5", 100, TransactionType.DEBIT, "2026-07-02T09:00", "GROCERY"),
                tx("T0034", "A5", 250, TransactionType.DEBIT, "2026-07-10T18:30", "DINING"),
                tx("T0035", "A5", 5000, TransactionType.CREDIT, "2026-07-15T09:00", "SALARY"),
                tx("T0036", "A5", 300, TransactionType.DEBIT, "2026-07-23T13:20", "FUEL"),
                tx("T0037", "A5", 150, TransactionType.DEBIT, "2026-07-28T20:00", "POS"),
                tx("T0038", "A5", 5000, TransactionType.CREDIT, "2026-08-15T09:00", "SALARY"),
                tx("T0039", "A5", 200, TransactionType.DEBIT, "2026-08-21T14:10", "GROCERY"),
                tx("T0040", "A5", 9000, TransactionType.DEBIT, "2026-08-29T01:30", "ATM_WITHDRAWAL"),

                // Account A6 (Esha, savings)
                tx("T0041", "A6", 700, TransactionType.DEBIT, "2026-07-03T10:30", "GROCERY"),
                tx("T0042", "A6", 1300, TransactionType.DEBIT, "2026-07-12T19:00", "DINING"),
                tx("T0043", "A6", 15000, TransactionType.CREDIT, "2026-07-16T09:00", "SALARY"),
                tx("T0044", "A6", 2200, TransactionType.DEBIT, "2026-07-25T15:30", "ONLINE_TRANSFER"),
                tx("T0045", "A6", 900, TransactionType.DEBIT, "2026-08-02T11:45", "FUEL"),
                tx("T0046", "A6", 15000, TransactionType.CREDIT, "2026-08-16T09:00", "SALARY"),
                tx("T0047", "A6", 1800, TransactionType.DEBIT, "2026-08-22T17:20", "POS"),
                tx("T0048", "A6", 600, TransactionType.DEBIT, "2026-08-27T22:40", "DINING"));
    }

    private static Transaction tx(
            String id, String accountId, long amount, TransactionType type, String isoDateTime, String category) {
        return new Transaction(
                id, accountId, BigDecimal.valueOf(amount), type, LocalDateTime.parse(isoDateTime), category);
    }
}
