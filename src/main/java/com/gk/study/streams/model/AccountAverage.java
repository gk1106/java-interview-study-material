package com.gk.study.streams.model;

/**
 * An account's average transaction amount (across all transactions, both DEBIT and CREDIT).
 *
 * @param accountId       the account this average belongs to
 * @param averageAmount   mean of {@code amount} across every transaction on this account
 */
public record AccountAverage(String accountId, double averageAmount) {
}
