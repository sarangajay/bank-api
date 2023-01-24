package com.bank.banking.accounts.domain;


import com.bank.banking.accounts.domain.base.EntityBase;
import com.bank.banking.accounts.error.InvalidAccountNumberException;
import com.bank.banking.accounts.error.InvalidAmountException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
@Builder
@AllArgsConstructor
public class Account extends EntityBase<Account> {
    private Amount balance;
    private final String name;
    private AccountType accountType;

    public Account(final String name) throws InvalidAmountException {
        this.name = name;
        this.balance = new Amount(0.00);
        this.accountType = AccountType.SAVINGS;
    }

    public Account(String name, AccountType accountType) throws InvalidAmountException {
        this(name);
        this.accountType = accountType;
    }

    public AccountNo accountNo() throws InvalidAccountNumberException {
        final String id = super.getId();
        return new AccountNo(id);
    }

    public String getName() {
        return name;
    }

    public Amount getBalance() {
        return balance;
    }

    public void setBalance(final Amount amount) {
        this.balance = amount;
    }

}