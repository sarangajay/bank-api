package com.sc.interbank.service.impl;

import com.sc.interbank.repository.Account;
import com.sc.interbank.repository.AccountTransaction;
import com.sc.interbank.repository.BankRepository;
import com.sc.interbank.dto.FundTransferRequest;
import com.sc.interbank.error.ResourceNotFoundException;
import com.sc.interbank.repository.TransactionType;
import com.sc.interbank.service.BankService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BankServiceImpl implements BankService {

    private static final Logger LOG = LoggerFactory.getLogger(BankServiceImpl.class);

    @Autowired
    private BankRepository bankRepository;

    @Override
    public synchronized void doTransfer(FundTransferRequest fundTransferRequest) throws ResourceNotFoundException {
        final int fromAccountNumber = fundTransferRequest.getFromAccountNumber();
        final double transactionAmount = fundTransferRequest.getAmount();

        final Account fromAccount = bankRepository.getAccount(fromAccountNumber);
        if (fromAccount == null) {
            throw new ResourceNotFoundException("Invalid [FROM] account number");
        }
        final int toAccountNumber = fundTransferRequest.getToAccountNumber();
        final Account toAccount = bankRepository.getAccount(toAccountNumber);
        if (toAccount == null) {
            throw new ResourceNotFoundException("Invalid [TO] account number");
        }


        double fromAccountBalance = fromAccount.getAmount();

        LOG.info("From Account Number " + fromAccountNumber + " balance is " + fromAccountBalance);
        LOG.info("Transaction amount is " + transactionAmount);

        if (fromAccountBalance < transactionAmount) {
            throw new IllegalArgumentException("Insufficient funds available");
        }

        double toAccountBalance = toAccount.getAmount();
        LOG.info("To Account Number " + toAccountNumber + " balance is " + toAccountBalance);


        fromAccountBalance = fromAccountBalance - transactionAmount;
        bankRepository.setAccountBalance(fromAccountNumber, fromAccountBalance);
        setTransaction(fromAccountNumber, TransactionType.DEBIT);

        toAccountBalance = toAccountBalance + transactionAmount;
        bankRepository.setAccountBalance(toAccountNumber, toAccountBalance);
        setTransaction(toAccountNumber, TransactionType.CREDIT);
    }

    @Override
    public Account getAccount(int accountNumber) throws ResourceNotFoundException {
        Account account = bankRepository.getAccount(accountNumber);
        if (account == null) {
            throw new ResourceNotFoundException(String.format("Account not found for %d", accountNumber));
        }
        return account;
    }

    @Override
    public List<AccountTransaction> getTransactions(int accountNumber, int limit) throws ResourceNotFoundException {
        final List<AccountTransaction> transactionList = bankRepository.getTransactions(accountNumber, limit);
        if (transactionList == null || transactionList.isEmpty())
            throw new ResourceNotFoundException(String.format("Invalid account number %s", accountNumber));

        return transactionList;
    }

    private void setTransaction(int accountNumber, TransactionType type) {
        final Account account = bankRepository.getAccount(accountNumber);
        final AccountTransaction accountTransaction = AccountTransaction.builder()
                .accountId(account.getAccountNumber())
                .amount(account.getAmount())
                .currency(account.getCurrency())
                .type(type)
                .transactionDate(LocalDateTime.now())
                .build();
        bankRepository.setAccountTransaction(accountNumber, accountTransaction);
    }

}
