package com.bank.banking.accounts.repository;

import com.bank.banking.accounts.domain.Amount;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountTransaction {
    private String accountId;
    private Amount amount;
    private TransactionType type;
    private LocalDateTime transactionDate;
}
