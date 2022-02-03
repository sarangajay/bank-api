package com.sc.interbank.repository;


import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class BankRepository {

    private final Map<Integer, Account> accounts = new ConcurrentHashMap() {{
        put(111, new Account(111, 50.00, "SJA", "DKK"));
        put(222, new Account(222, 100.00, "ABC", "DKK"));
    }};

    private final Map<Integer, List<AccountTransaction>> accountTransactions = new HashMap();

    public List<AccountTransaction> getTransactions(int accountNumber, int limit) {
        List<AccountTransaction> transactionList = accountTransactions.get(accountNumber);
        return transactionList == null
                ? Collections.emptyList()
                : transactionList.stream().limit(limit).collect(Collectors.toList());
    }

    public void setAccountTransaction(int accountNumber, AccountTransaction transaction) {
        accountTransactions.computeIfAbsent(accountNumber,
                e -> new ArrayList<>()).add(transaction);
    }

    public void setAccountBalance(int accountNumber, double amount) {
        getAccount(accountNumber).setAmount(amount);
    }

    public Account getAccount(int accountNumber) {
        return accounts.get(accountNumber);
    }
}
