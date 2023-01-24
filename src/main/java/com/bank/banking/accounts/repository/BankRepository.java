package com.bank.banking.accounts.repository;


import com.bank.banking.accounts.domain.Account;
import com.bank.banking.accounts.domain.AccountNo;
import com.bank.banking.accounts.domain.Amount;
import com.bank.banking.accounts.error.InsufficientFoundsException;
import com.bank.banking.accounts.error.InvalidAccountNumberException;
import com.bank.banking.accounts.error.InvalidAmountException;
import com.bank.banking.accounts.error.ResourceNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class BankRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap();

    public AccountNo createAccount(Account account) throws InvalidAccountNumberException {
        AccountNo accountNo = account.accountNo();
        accounts.put(accountNo.toString(), account);
        return accountNo;
    }

    public synchronized AccountNo doDeposit(String accountId, Double amount) throws InvalidAccountNumberException, InvalidAmountException {
         AccountNo accountNo = new AccountNo(accountId);
         Account account = getAccount(accountNo);
         if(account == null) {
             throw new ResourceNotFoundException(String.format("Account not found for %s ", accountId));
         }
        Amount depositAmount = new Amount(amount);
        Amount newAmount = account.getBalance().plus(depositAmount);
        account.setBalance(newAmount);
        accounts.put(accountNo.toString(), account);
        return accountNo;
    }

    public synchronized AccountNo doWithdraw(String accountId, Double amount) throws InvalidAccountNumberException, InvalidAmountException, InsufficientFoundsException {
        AccountNo accountNo = new AccountNo(accountId);
        Account account = getAccount(accountNo);
        if(account == null) {
            throw new ResourceNotFoundException(String.format("Account not found for %s ", accountId));
        }

        if(account.getBalance().getAmount() < amount) {
            throw new InsufficientFoundsException(String.format("Insufficient founds for requested withdrawal %s from accountId", amount, accountId));
        }

        Amount withdrawingAmount = new Amount(amount);
        Amount newAmount = account.getBalance().minus(withdrawingAmount);
        account.setBalance(newAmount);
        accounts.put(accountNo.toString(), account);
        return accountNo;
    }

    private final Map<String, List<AccountTransaction>> accountTransactions = new HashMap();

    public List<AccountTransaction> getTransactions(AccountNo accountNumber, int limit) {
        List<AccountTransaction> transactionList = accountTransactions.get(accountNumber.toString());
        if(transactionList == null) {
            return Collections.emptyList();
        } else {
            Collections.sort(transactionList, new TransactionComparator());
            return transactionList.stream().limit(limit).collect(Collectors.toList());
        }
    }

    class TransactionComparator implements Comparator<AccountTransaction> {
        @Override
        public int compare(AccountTransaction o1, AccountTransaction o2) {
            return o1.getTransactionDate().compareTo(o2.getTransactionDate());
        }
    }

    public void setAccountTransaction(AccountNo accountNo, AccountTransaction transaction) {
        accountTransactions.computeIfAbsent(accountNo.toString(),
                e -> new ArrayList<>()).add(transaction);
    }

    public Account getAccount(AccountNo accountNumber) {
        return accounts.get(accountNumber.toString());
    }
}
