package com.bank.banking.accounts.domain.base;


import java.util.UUID;

public abstract class EntityBase<T extends EntityBase<T>> {

    private String id;

    public String getId() {
        return UUID.randomUUID().toString();
    }
}
