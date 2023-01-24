package com.bank.banking.accounts.error;

public class InternalServerErrorException extends RuntimeException {

    private static final long serialVersionUID = -8807538241137866537L;

    public InternalServerErrorException(String message) {
        super(message);
    }

}

