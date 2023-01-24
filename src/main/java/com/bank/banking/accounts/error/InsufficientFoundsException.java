package com.bank.banking.accounts.error;

public class InsufficientFoundsException extends Exception {

    public InsufficientFoundsException(String msg) {
        super(msg);
    }

    public InsufficientFoundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
