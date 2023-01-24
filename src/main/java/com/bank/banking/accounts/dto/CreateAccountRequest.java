package com.bank.banking.accounts.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class CreateAccountRequest {

    private Double balance;
    private String name;
    private String accountType;
}
