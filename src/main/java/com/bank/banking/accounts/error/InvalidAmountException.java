package com.bank.banking.accounts.error;

public class InvalidAmountException extends Exception {

    public InvalidAmountException(String msg) {
        super(msg);
    }

    public InvalidAmountException(String message, Throwable cause) {
        super(message, cause);
    }

}
