package com.sc.interbank.service;

import com.sc.interbank.dto.FundTransferRequest;
import com.sc.interbank.repository.Account;
import com.sc.interbank.repository.AccountTransaction;

import java.util.List;

public interface BankService {
    void doTransfer(FundTransferRequest fundTransferRequest);

    Account getAccount(int accountNumber);

    List<AccountTransaction> getTransactions(int accountNumber, int limit);
}
