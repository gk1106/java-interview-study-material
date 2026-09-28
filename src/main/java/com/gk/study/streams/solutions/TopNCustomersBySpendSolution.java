package com.gk.study.streams.solutions;

import com.gk.study.streams.model.Account;
import com.gk.study.streams.model.Customer;
import com.gk.study.streams.model.CustomerSpend;
import com.gk.study.streams.model.Transaction;
import com.gk.study.streams.model.TransactionType;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Solution for {@link com.gk.study.streams.exercises.TopNCustomersBySpend}.
 *
 * <p>Joins transaction -> account -> customer via lookup maps built once with
 * {@code Collectors.toMap}, sums DEBIT amounts per customer (defaulting every customer to ZERO
 * first so a customer with no debits still appears), then sorts descending and takes the top N.
 * O(n log n) time (dominated by the sort over customers), O(n) space.
 */
public final class TopNCustomersBySpendSolution {

    private TopNCustomersBySpendSolution() {
    }

    public static List<CustomerSpend> solve(
            List<Customer> customers, List<Account> accounts, List<Transaction> transactions, int topN) {
        Map<String, String> accountToCustomer =
                accounts.stream().collect(Collectors.toMap(Account::id, Account::customerId));
        Map<String, String> customerIdToName =
                customers.stream().collect(Collectors.toMap(Customer::id, Customer::name));

        Map<String, BigDecimal> spendFromTransactions = transactions.stream()
                .filter(t -> t.type() == TransactionType.DEBIT)
                .collect(Collectors.groupingBy(
                        t -> accountToCustomer.get(t.accountId()),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));

        Map<String, BigDecimal> spendByCustomer = customers.stream()
                .collect(Collectors.toMap(
                        Customer::id, c -> spendFromTransactions.getOrDefault(c.id(), BigDecimal.ZERO)));

        return spendByCustomer.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(topN)
                .map(e -> new CustomerSpend(e.getKey(), customerIdToName.get(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }
}
