package com.bank.banking.accounts.domain;

import com.bank.banking.accounts.error.InvalidAmountException;

/**
 * Value object to represent the amount.
 */
public class Amount {

    private Double amount;

    public Double getAmount() {
        return amount;
    }

    public static double maxValue() {
        return Double.MAX_VALUE;
    }

    public static double minValue() {
        return -maxValue();
    }

    public Amount(final Double amount) throws InvalidAmountException {
        if (Double.isNaN(amount) || amount < minValue() || amount > maxValue()) {
            throw new InvalidAmountException(String.format("Invalid amount entered for account creation %s", "null"));
        }
        this.amount = amount;
    }

    public Amount plus(final Amount other) throws InvalidAmountException {
        final double doubleResult = this.getAmount() + other.getAmount();
        final Amount result = new Amount(doubleResult);
        return result;
    }

    public Amount minus(final Amount other) throws InvalidAmountException {
        final double result = this.getAmount() - other.getAmount();
        return new Amount(result);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Amount))
            return false;
        final Amount otherAmount = (Amount) other;
        return getAmount() == otherAmount.getAmount();
    }
}
