package com.gk.study.streams.model;

import java.math.BigDecimal;

/**
 * A bank account owned by exactly one customer. A customer may own more than one account.
 *
 * @param id         unique account id, e.g. "A1"
 * @param customerId the owning customer's id
 * @param balance    current balance
 * @param type       SAVINGS or CURRENT
 */
public record Account(String id, String customerId, BigDecimal balance, AccountType type) {
}
