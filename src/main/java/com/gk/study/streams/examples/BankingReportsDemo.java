package com.gk.study.streams.examples;

import com.gk.study.streams.model.Account;
import com.gk.study.streams.model.Customer;
import com.gk.study.streams.model.SampleBankingData;
import com.gk.study.streams.model.Transaction;
import com.gk.study.streams.model.TransactionType;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Realistic reporting queries over the shared banking dataset ({@link SampleBankingData}), the
 * same shape of problem the topic-7 exercises ask you to solve on your own.
 */
public class BankingReportsDemo {

    public static void main(String[] args) {
        List<Customer> customers = SampleBankingData.customers();
        List<Account> accounts = SampleBankingData.accounts();
        List<Transaction> transactions = SampleBankingData.transactions();

        Map<String, String> accountToCustomer = accounts.stream()
                .collect(Collectors.toMap(Account::id, Account::customerId));
        Map<String, String> customerIdToName = customers.stream()
                .collect(Collectors.toMap(Customer::id, Customer::name));

        // 1) Group transactions by account, sum debit vs credit.
        Map<String, BigDecimal> debitByAccount = transactions.stream()
                .filter(t -> t.type() == TransactionType.DEBIT)
                .collect(Collectors.groupingBy(
                        Transaction::accountId, Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
        System.out.println("debit total by account = " + debitByAccount);

        // 2) Top-3 customers by total spend (sum of DEBIT amounts across all their accounts).
        Map<String, BigDecimal> spendByCustomer = transactions.stream()
                .filter(t -> t.type() == TransactionType.DEBIT)
                .collect(Collectors.groupingBy(
                        t -> accountToCustomer.get(t.accountId()),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
        List<String> top3 = spendByCustomer.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .map(e -> customerIdToName.get(e.getKey()) + "=" + e.getValue())
                .toList();
        System.out.println("top 3 customers by spend = " + top3);

        // 3) Monthly statement: per account, per month, total debit/credit + count.
        Map<String, Map<YearMonth, Long>> txCountByAccountAndMonth = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::accountId,
                        Collectors.groupingBy(t -> YearMonth.from(t.timestamp()), Collectors.counting())));
        System.out.println("A1 transaction count by month = " + txCountByAccountAndMonth.get("A1"));

        // 4) Fraud-flag filter: large amount AND posted at an odd hour, via a composed Predicate chain.
        Predicate<Transaction> isLargeAmount = t -> t.amount().compareTo(BigDecimal.valueOf(8000)) > 0;
        Predicate<Transaction> isOddHour = t -> {
            int hour = t.timestamp().getHour();
            return hour < 6 || hour >= 23;
        };
        Predicate<Transaction> isSuspicious = isLargeAmount.and(isOddHour);
        List<String> flagged = transactions.stream()
                .filter(isSuspicious)
                .map(Transaction::id)
                .toList();
        System.out.println("fraud-flagged transaction ids = " + flagged);

        // 5) Account with the highest average transaction amount.
        Map<String, Double> avgByAccount = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::accountId,
                        Collectors.averagingDouble(t -> t.amount().doubleValue())));
        String highestAvgAccount = avgByAccount.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow();
        System.out.println("account with highest average transaction = " + highestAvgAccount
                + " (" + avgByAccount.get(highestAvgAccount) + ")");
    }
}
