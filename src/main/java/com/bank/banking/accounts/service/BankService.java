package com.bank.banking.accounts.service;

import com.bank.banking.accounts.domain.AccountNo;
import com.bank.banking.accounts.dto.AccountMovementRequest;
import com.bank.banking.accounts.dto.CreateAccountRequest;
import com.bank.banking.accounts.dto.FundTransferRequest;
import com.bank.banking.accounts.domain.Account;
import com.bank.banking.accounts.error.InsufficientFoundsException;
import com.bank.banking.accounts.error.InvalidAccountNumberException;
import com.bank.banking.accounts.error.InvalidAmountException;
import com.bank.banking.accounts.error.RequiredFieldException;
import com.bank.banking.accounts.repository.AccountTransaction;

import java.util.List;
import java.util.UUID;

public interface BankService {

    AccountNo createAccount(CreateAccountRequest createAccountRequest) throws RequiredFieldException, InvalidAmountException, InvalidAccountNumberException;

    AccountNo doDeposit(AccountMovementRequest movementRequest) throws RequiredFieldException, InvalidAccountNumberException, InvalidAmountException;

    AccountNo doWithdraw(AccountMovementRequest movementRequest) throws RequiredFieldException, InvalidAmountException, InvalidAccountNumberException, InsufficientFoundsException;
    
    Account getAccount(String accountNumber) throws InvalidAccountNumberException;

    List<AccountTransaction> getTransactions(String accountId, int limit) throws InvalidAccountNumberException;
}
