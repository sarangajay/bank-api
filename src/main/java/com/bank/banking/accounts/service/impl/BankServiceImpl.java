package com.bank.banking.accounts.service.impl;

import com.bank.banking.accounts.domain.*;
import com.bank.banking.accounts.dto.AccountMovementRequest;
import com.bank.banking.accounts.dto.CreateAccountRequest;
import com.bank.banking.accounts.dto.FundTransferRequest;
import com.bank.banking.accounts.error.*;
import com.bank.banking.accounts.repository.AccountTransaction;
import com.bank.banking.accounts.repository.BankRepository;
import com.bank.banking.accounts.repository.TransactionType;
import com.bank.banking.accounts.service.BankService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BankServiceImpl implements BankService {

    private static final Logger LOG = LoggerFactory.getLogger(BankServiceImpl.class);

    @Autowired
    private BankRepository bankRepository;

    @Override
    public AccountNo createAccount(CreateAccountRequest createAccountRequest) throws RequiredFieldException, InvalidAmountException, InvalidAccountNumberException {

        if (StringUtils.isEmpty(createAccountRequest.getName())) {
            throw new RequiredFieldException(String.format("Name is required for creation of a new account"));
        }

        AccountType accountType;
        if (StringUtils.isEmpty(createAccountRequest.getAccountType()) || AccountType.valueOf(createAccountRequest.getAccountType()) == null) {
            accountType = AccountType.SAVINGS;
        } else {
            accountType = AccountType.valueOf(createAccountRequest.getAccountType());
        }

        Account account = new Account(createAccountRequest.getName(), accountType);

        if (createAccountRequest.getBalance() == null) {
            account.setBalance(new Amount(0.00));
        } else {
            account.setBalance(new Amount(createAccountRequest.getBalance()));
        }

        return bankRepository.createAccount(account);
    }

    @Override
    public AccountNo doDeposit(AccountMovementRequest movementRequest) throws RequiredFieldException, InvalidAccountNumberException, InvalidAmountException {

        if (StringUtils.isEmpty(movementRequest.getAccountMovementType()) || AccountMovementType.valueOf(movementRequest.getAccountMovementType()) == null) {
            throw new RequiredFieldException(String.format("Account movement type is missing"));
        }

        if (AccountMovementType.valueOf(movementRequest.getAccountMovementType()) != AccountMovementType.DEPOSIT) {
            throw new RequiredFieldException(String.format("Account movement type %S is expected ", AccountMovementType.DEPOSIT.name()));
        }

        if (movementRequest.getAmount() < 0) {
            throw new RequiredFieldException(String.format("Depositing amount should be a positive value %s", movementRequest.getAmount()));
        }

        AccountNo accountNo = bankRepository.doDeposit(movementRequest.getAccountId(), movementRequest.getAmount());
        setTransaction(movementRequest.getAccountId(), TransactionType.CREDIT, new Amount(movementRequest.getAmount()));
        return accountNo;
    }

    @Override
    public AccountNo doWithdraw(AccountMovementRequest movementRequest) throws RequiredFieldException, InvalidAmountException, InvalidAccountNumberException, InsufficientFoundsException {
        if (StringUtils.isEmpty(movementRequest.getAccountMovementType()) || AccountMovementType.valueOf(movementRequest.getAccountMovementType()) == null) {
            throw new RequiredFieldException(String.format("Account movement type is missing"));
        }

        if (AccountMovementType.valueOf(movementRequest.getAccountMovementType()) != AccountMovementType.WITHDRAW) {
            throw new RequiredFieldException(String.format("Account Movement Type %s is expected", AccountMovementType.WITHDRAW.name()));
        }

        if (movementRequest.getAmount() < 0) {
            throw new RequiredFieldException(String.format("Withdrawing amount should be a positive value %s", movementRequest.getAmount()));
        }

        AccountNo accountNo = bankRepository.doWithdraw(movementRequest.getAccountId(), movementRequest.getAmount());
        setTransaction(movementRequest.getAccountId(), TransactionType.DEBIT, new Amount(movementRequest.getAmount()));
        return accountNo;
    }

/*
    @Override
    public synchronized void doTransfer(FundTransferRequest fundTransferRequest) throws ResourceNotFoundException {
        final int fromAccountNumber = fundTransferRequest.getFromAccountNumber();
        final double transactionAmount = fundTransferRequest.getAmount();

        final Account fromAccount = bankRepository.getAccount(fromAccountNumber);
        if (fromAccount == null) {
            throw new ResourceNotFoundException("Invalid [FROM] account number", accountId);
        }
        final int toAccountNumber = fundTransferRequest.getToAccountNumber();
        final Account toAccount = bankRepository.getAccount(toAccountNumber);
        if (toAccount == null) {
            throw new ResourceNotFoundException("Invalid [TO] account number", accountId);
        }


        double fromAccountBalance = fromAccount.getBalance();

        LOG.info("From Account Number " + fromAccountNumber + " balance is " + fromAccountBalance);
        LOG.info("Transaction amount is " + transactionAmount);

        if (fromAccountBalance < transactionAmount) {
            throw new IllegalArgumentException("Insufficient funds available");
        }

        double toAccountBalance = toAccount.getBalance();
        LOG.info("To Account Number " + toAccountNumber + " balance is " + toAccountBalance);


        fromAccountBalance = fromAccountBalance - transactionAmount;
        bankRepository.setAccountBalance(fromAccountNumber, fromAccountBalance);
        setTransaction(fromAccountNumber, TransactionType.DEBIT);

        toAccountBalance = toAccountBalance + transactionAmount;
        bankRepository.setAccountBalance(toAccountNumber, toAccountBalance);
        setTransaction(toAccountNumber, TransactionType.CREDIT);
    }*/

    @Override
    public Account getAccount(String accountNumber) throws ResourceNotFoundException, InvalidAccountNumberException {
        AccountNo accountNo = new AccountNo(accountNumber);
        Account account = bankRepository.getAccount(accountNo);
        if (account == null) {
            throw new ResourceNotFoundException(String.format("Account not found for %s", accountNumber));
        }
        return account;
    }

    @Override
    public List<AccountTransaction> getTransactions(String accountId, int limit) throws ResourceNotFoundException, InvalidAccountNumberException {
        final List<AccountTransaction> transactionList = bankRepository.getTransactions(new AccountNo(accountId), limit);
        return transactionList;
    }

    private void setTransaction(String accountId, TransactionType type, Amount amount) throws InvalidAccountNumberException {
        AccountNo accountNo = new AccountNo(accountId);

        final AccountTransaction accountTransaction = AccountTransaction.builder()
                .accountId(accountId)
                .amount(amount)
                .type(type)
                .transactionDate(LocalDateTime.now())
                .build();
        bankRepository.setAccountTransaction(accountNo, accountTransaction);
    }

}
