package com.gk.study.streams.exercises;

import com.gk.study.streams.model.Account;
import com.gk.study.streams.model.Customer;
import com.gk.study.streams.model.CustomerSpend;
import com.gk.study.streams.model.Transaction;
import java.util.List;

/**
 * B02 [Medium] Banking dataset. Rank customers by total spend — the sum of DEBIT transaction
 * amounts across every account they own — and return the top {@code topN}, highest spend first.
 * Input:  {@link com.gk.study.streams.model.SampleBankingData} customers/accounts/transactions, topN = 3
 * Output: [CustomerSpend[customerId=C3,...], CustomerSpend[customerId=C1,...], CustomerSpend[customerId=C2,...]]
 *         (Chitra Rao's corporate account has by far the largest spend in the sample dataset)
 * Constraint: a customer with zero DEBIT transactions still has an entry with totalSpend = 0;
 * ties may be broken in any order; if topN exceeds the customer count, return all of them.
 * Pattern: join transactions -> account -> customer, groupingBy + reducing, sort + limit
 */
public class TopNCustomersBySpend {

    /**
     * @param customers    all customers
     * @param accounts     all accounts (each references its owning customer via customerId)
     * @param transactions all transactions (each references its account via accountId)
     * @param topN         how many top spenders to return
     * @return up to {@code topN} customers ordered by total spend, descending
     */
    public static List<CustomerSpend> solve(
            List<Customer> customers, List<Account> accounts, List<Transaction> transactions, int topN) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
