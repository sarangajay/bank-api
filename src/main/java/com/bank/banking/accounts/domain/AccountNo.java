package com.bank.banking.accounts.domain;

import com.bank.banking.accounts.error.InvalidAccountNumberException;
import java.util.Objects;
import java.util.UUID;


/**
 * Value object the uniquely identify an Account.
 */
public final class AccountNo {

    private String id;

    public AccountNo(final String id) throws InvalidAccountNumberException {
        if (id == null) {
            throw new InvalidAccountNumberException(String.format("Invalid Account number %s", "null"));
        }
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AccountNo other = (AccountNo) obj;
        return id == other.id;
    }
}
