package com.gk.study.streams.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A single posted transaction on an account.
 *
 * @param id          unique transaction id, e.g. "T0007"
 * @param accountId   the account this transaction posted against
 * @param amount      always positive; direction is carried by {@link #type()}
 * @param type        DEBIT (money out) or CREDIT (money in)
 * @param timestamp   when the transaction was posted
 * @param category    a free-text merchant/purpose category, e.g. "GROCERY", "SALARY"
 */
public record Transaction(
        String id,
        String accountId,
        BigDecimal amount,
        TransactionType type,
        LocalDateTime timestamp,
        String category) {
}
