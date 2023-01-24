package com.bank.banking.accounts.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class FundTransferRequest {

    private int fromAccountNumber;
    private int toAccountNumber;
    private double amount;

}
