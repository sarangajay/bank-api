package com.bank.banking.accounts.error;


public class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -8696509168909378287L;

  /**
   * Resource not found thrown back.
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
