package com.bank.banking.accounts.error;

public class RequiredFieldException extends Exception {

    public RequiredFieldException(String msg) {
        super(msg);
    }

    public RequiredFieldException(String message, Throwable cause) {
        super(message, cause);
    }
}
