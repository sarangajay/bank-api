package com.sc.interbank.repository;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountTransaction {
    private int accountId;
    private double amount;
    private String currency;
    private TransactionType type;
    private LocalDateTime transactionDate;
}
