package com.bank.banking.accounts.error;

public class InvalidAccountNumberException extends Exception {

    public InvalidAccountNumberException(String msg) {
        super(msg);
    }

    public InvalidAccountNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
