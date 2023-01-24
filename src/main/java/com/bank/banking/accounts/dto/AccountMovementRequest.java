package com.bank.banking.accounts.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
@Builder
public class AccountMovementRequest {

    String accountId;
    private Double amount;
    private String accountMovementType;
}
